package com.nequi.franchise.infrastructure.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CursorUtilTest {

    @Test
    void encodeCursor_ShouldReturnBase64EncodedString() {
        Map<String, String> cursorData = new HashMap<>();
        cursorData.put("id", "franchise-123");

        String encoded = CursorUtil.encodeCursor(cursorData);

        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test
    void decodeCursor_ShouldReturnOriginalData() {
        Map<String, String> originalData = new HashMap<>();
        originalData.put("id", "franchise-123");

        String encoded = CursorUtil.encodeCursor(originalData);
        Map<String, String> decoded = CursorUtil.decodeCursor(encoded);

        assertNotNull(decoded);
        assertEquals("franchise-123", decoded.get("id"));
    }

    @Test
    void decodeCursor_ShouldReturnNull_WhenCursorIsNull() {
        Map<String, String> decoded = CursorUtil.decodeCursor(null);

        assertNull(decoded);
    }

    @Test
    void decodeCursor_ShouldReturnNull_WhenCursorIsEmpty() {
        Map<String, String> decoded = CursorUtil.decodeCursor("");

        assertNull(decoded);
    }

    @Test
    void decodeCursor_ShouldReturnNull_WhenCursorIsInvalid() {
        Map<String, String> decoded = CursorUtil.decodeCursor("invalid-cursor");

        assertNull(decoded);
    }

    @Test
    void encodeCursor_ShouldHandleMultipleFields() {
        Map<String, String> cursorData = new HashMap<>();
        cursorData.put("id", "franchise-123");
        cursorData.put("timestamp", "2024-01-21");

        String encoded = CursorUtil.encodeCursor(cursorData);
        Map<String, String> decoded = CursorUtil.decodeCursor(encoded);

        assertNotNull(decoded);
        assertEquals("franchise-123", decoded.get("id"));
        assertEquals("2024-01-21", decoded.get("timestamp"));
    }

    @Test
    void encodeCursor_ShouldReturnNull_WhenExceptionOccurs() {
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("circular", invalidData);

        String encoded = CursorUtil.encodeCursor((Map) invalidData);

        assertNull(encoded);
    }
}
