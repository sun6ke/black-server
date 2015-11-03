package com.example.experimental;

import com.example.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class JacksonTest {
    @Test
    public void write() throws Exception{
        User u = new User("12345678901");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(u));
    }
}
