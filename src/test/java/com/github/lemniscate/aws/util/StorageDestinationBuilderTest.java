package com.github.lemniscate.aws.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class StorageDestinationBuilderTest {

    private static final String ACCESS_KEY = System.getenv("AWS_ACCESS_KEY");
    private static final String SECRET_KEY = System.getenv("AWS_SECRET_KEY");

    @Test
    public void testBuilding() throws IOException, InterruptedException {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, 1);
        Date expiration = cal.getTime();

        StorageDestinationBuilder.StorageDestination destination = new StorageDestinationBuilder()
                .setKey("files/foobar/test.jpg")
                .policyBuilder(expiration)
                    .setBucket("supertemporarybucket")
                    .setKeyPrefix("files/foobar")
                    .setAcl("public-read")
                    .setContentType("image/")
                    .setRedirect("http://google.com?accessCode=5")
                    .done(ACCESS_KEY, SECRET_KEY)
                .build()
                ;

        // sure, print it out. why not
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, destination);

        System.out.flush();
        // While we're at it, let's copy the policy to our keyboard!
        String[] cmd = {
            "/bin/sh",
            "-c",
            String.format("echo %s | pbcopy", destination.getParams().get("policy").toString())
        };
        Runtime.getRuntime().exec(cmd);

    }
}
