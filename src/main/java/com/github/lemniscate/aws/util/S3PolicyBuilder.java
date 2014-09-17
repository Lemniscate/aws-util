package com.github.lemniscate.aws.util;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.Assert;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

public class S3PolicyBuilder {

    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private final Date expiration;
    private final List<Object> conditions = new ArrayList<Object>();
    private final ObjectMapper mapper;

    public S3PolicyBuilder(Date expiration) {
        this(expiration, DEFAULT_MAPPER);
    }

    public S3PolicyBuilder(Date expiration, ObjectMapper mapper) {
        this.expiration = new Date(expiration.getTime());
        this.mapper = mapper;
    }

    public S3PolicyBuilder equality(String input, Object value){
        Assert.hasLength(input, "Must specify an input to verify equality on");
        conditions.add(new KeyValueWrapper(input, value));
        return this;
    }

    public S3PolicyBuilder startsWith(String input, String value){
        Assert.hasLength(input, "Must specify an input to verify starts with");
        Assert.notNull(value, "Must specify a value to verify (can be empty)");
        if( !input.startsWith("$") ){
            input = "$" + input;
        }
        conditions.add(new Object[]{"starts-with", input, value});
        return this;
    }

    public S3PolicyBuilder contentLength(int min, int max){
        Assert.isTrue(max >= 0, "Max Content-Length must be >= 0");
        Assert.isTrue(max >= min, "Max Content-Length must be >= min");
        conditions.add(new Object[]{"content-length-range", min, max});
        return this;
    }

    public S3Policy build(){
        SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT);
        sd.setTimeZone(TimeZone.getTimeZone("GMT"));
        String exp =  sd.format(expiration);
        S3Policy result = new S3Policy(exp, new ArrayList<Object>(conditions));
        return result;
    }

    public String buildAndBase64Encode(){
        try{
            S3Policy policy = build();
            String json = policy.toJson();
            byte[] policyBytes = Base64.encode(json.getBytes("UTF-8"));
            String base64 = new String(policyBytes)
                    .replaceAll("\n", "")
                    .replaceAll("\r", "");

            return base64;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed getting bytes from json", e);
        }
    }

    public String buildSignature(String secretKey){
        try{
            String policy = buildAndBase64Encode();

            Mac hmac = Mac.getInstance("HmacSHA1");
            hmac.init(new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA1"));

            byte[] hmacdPolicy = hmac.doFinal(policy.getBytes("UTF-8"));
            byte[] sigBytes = Base64.encode( hmacdPolicy );

            String signature = new String(sigBytes)
                    .replaceAll("\n", "");

            return signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed building policy", e);
        }
    }



    public class S3Policy{
        private final String expiration;
        private final List<Object> conditions;

        public S3Policy(String expiration, List<Object> conditions) {
            this.expiration = expiration;
            this.conditions = conditions;
        }

        public String toJson(){
            try {
                return mapper.writer().writeValueAsString(this);
            } catch (IOException e) {
                throw new RuntimeException("Failed writing policy to JSON");
            }
        }

        public String getExpiration() {
            return expiration;
        }

        public List<Object> getConditions() {
            return conditions;
        }
    }

    // This is a really ugly work around to get key-value serialization
    private class KeyValueWrapper{

        private final Map<String, Object> map = Maps.newHashMap();

        public KeyValueWrapper(String input, Object value) {
            map.put(input, value);
        }

        @JsonAnyGetter
        private Map<String, Object> getMap() {
            return map;
        }
    }

}
