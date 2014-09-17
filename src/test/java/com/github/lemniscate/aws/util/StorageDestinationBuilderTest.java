package com.github.lemniscate.aws.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class StorageDestinationBuilderTest {

    @Test
    public void testBuilding() throws IOException {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, 1);
        Date expiration = cal.getTime();

        StorageDestinationBuilder.StorageDestination destination = new StorageDestinationBuilder()
                .setKey("/files/foobar/jasper.pdf")
                .policyBuilder(expiration)
                    .setBucket("blah")
                    .setKeyPrefix("/files/foobar")
                    .setAcl("public-read")
                    .setContentType("image/jpg")
                    .setRedirect("http://google.com?accessCode=5")
                    .done("abc123", "super secret key")
                .build()
                ;


        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, destination);
        System.out.flush();

    }
}
