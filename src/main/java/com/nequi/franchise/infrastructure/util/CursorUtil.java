package com.nequi.franchise.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class CursorUtil {

    private static final Logger logger = LoggerFactory.getLogger(CursorUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String encodeCursor(Map<String, String> cursorData) {
        try {
            String json = objectMapper.writeValueAsString(cursorData);
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            logger.error("Error encoding cursor: {}", e.getMessage());
            return null;
        }
    }

    public static Map<String, String> decodeCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cursor);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.error("Error decoding cursor: {}", e.getMessage());
            return null;
        }
    }
}
