package com.nequi.franchise.domain.valueobject;

import com.nequi.franchise.domain.exception.InvalidDomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityNameTest {

    @Test
    void of_ShouldCreateValidName() {
        EntityName name = EntityName.of("Valid Name");
        
        assertEquals("Valid Name", name.getValue());
    }

    @Test
    void of_ShouldTrimWhitespace() {
        EntityName name = EntityName.of("  Trimmed Name  ");
        
        assertEquals("Trimmed Name", name.getValue());
    }

    @Test
    void of_ShouldAcceptSpecialCharacters() {
        EntityName name = EntityName.of("Café-Restaurante_2024.5");
        
        assertEquals("Café-Restaurante_2024.5", name.getValue());
    }

    @Test
    void of_ShouldAcceptSpanishCharacters() {
        EntityName name = EntityName.of("Niño Español");
        
        assertEquals("Niño Español", name.getValue());
    }

    @Test
    void of_ShouldThrowException_WhenNull() {
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> EntityName.of(null)
        );
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void of_ShouldThrowException_WhenEmpty() {
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> EntityName.of("")
        );
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void of_ShouldThrowException_WhenOnlyWhitespace() {
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> EntityName.of("   ")
        );
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void of_ShouldThrowException_WhenTooLong() {
        String longName = "a".repeat(201);
        
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> EntityName.of(longName)
        );
        
        assertTrue(exception.getMessage().contains("between"));
    }

    @Test
    void of_ShouldThrowException_WhenContainsInvalidCharacters() {
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> EntityName.of("Invalid@Name#")
        );
        
        assertTrue(exception.getMessage().contains("can only contain"));
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameValue() {
        EntityName name1 = EntityName.of("Same Name");
        EntityName name2 = EntityName.of("Same Name");
        
        assertEquals(name1, name2);
    }

    @Test
    void equals_ShouldReturnFalse_WhenDifferentValue() {
        EntityName name1 = EntityName.of("Name 1");
        EntityName name2 = EntityName.of("Name 2");
        
        assertNotEquals(name1, name2);
    }

    @Test
    void toString_ShouldReturnValue() {
        EntityName name = EntityName.of("Test Name");
        
        assertEquals("Test Name", name.toString());
    }
}
