package com.example.back_end.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class JacksonSerializer implements RedisSerializer<Object> {

    private final ObjectMapper mapper;

    public JacksonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        if (value == null) return new byte[0];
        try {
            ObjectNode wrapper = mapper.createObjectNode();
            wrapper.put("@type", value.getClass().getName());
            wrapper.set("@value", mapper.valueToTree(value));
            return mapper.writeValueAsBytes(wrapper);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) return null;
        try {
            JsonNode wrapper = mapper.readTree(bytes);
            String className = wrapper.get("@type").asText();
            Class<?> clazz = Class.forName(className);
            return mapper.treeToValue(wrapper.get("@value"), clazz);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize", e);
        }
    }
}
