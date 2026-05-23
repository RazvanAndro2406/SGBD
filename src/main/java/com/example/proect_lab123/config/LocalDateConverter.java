// Create this new file
package com.example.proect_lab123.config;

import com.example.proect_lab123.repositoryORM.ActorRepositoryORM;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {

    private static final Logger logger = LogManager.getLogger(LocalDateConverter.class);
    @Override
    public String convertToDatabaseColumn(LocalDate date) {
        return date != null ? date.toString() : null; // "1974-11-11"
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return LocalDate.parse(dbData.length() > 10 ? dbData.substring(0, 10) : dbData);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format in database: '{}', using fallback date", dbData);
            return LocalDate.of(1900, 1, 1); // fallback for gibberish data
        }
    }
}