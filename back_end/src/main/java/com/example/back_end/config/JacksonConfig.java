package com.example.back_end.config;

import java.math.BigInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

@Configuration
public class JacksonConfig {

    @Bean
    public JacksonModule longToStringModule() {
        SimpleModule module = new SimpleModule("LongToStringModule");
        module.addSerializer(Long.class, ToStringSerializer.instance)
                .addSerializer(Long.TYPE, ToStringSerializer.instance)
                .addSerializer(BigInteger.class, ToStringSerializer.instance);
        return module;
    }
}
