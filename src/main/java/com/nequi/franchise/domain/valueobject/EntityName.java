package com.nequi.franchise.domain.valueobject;

import com.nequi.franchise.domain.exception.InvalidDomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public class EntityName {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 200;
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.áéíóúÁÉÍÓÚñÑ]+$");
    
    private final String value;

    private EntityName(String value) {
        validate(value);
        this.value = value.trim();
    }

    public static EntityName of(String value) {
        return new EntityName(value);
    }

    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidDomainException("Name cannot be null or empty");
        }
        
        String trimmed = value.trim();
        
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new InvalidDomainException(
                String.format("Name must be between %d and %d characters", MIN_LENGTH, MAX_LENGTH)
            );
        }
        
        if (!VALID_NAME_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidDomainException(
                "Name can only contain letters, numbers, spaces, hyphens, underscores and dots"
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
