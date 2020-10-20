package com.rbkmoney.absolutely.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataDeserializer {

    private final ObjectMapper objectMapper;

    public Object deserialize(byte[] data) {
        try {
            return objectMapper.readValue(data, Map.class);
        } catch (Exception e) {
            log.error("Error when deserialize byte array. It must be json.");
            return null;
        }
    }
}
