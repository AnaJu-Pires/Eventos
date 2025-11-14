package br.ifsp.events.dto.time;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TimeUpdateDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    void validTimeUpdate_hasNoViolations() {
        TimeUpdateDTO dto = new TimeUpdateDTO("Time B");

        Set<ConstraintViolation<TimeUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankName_producesViolation() {
        TimeUpdateDTO dto = new TimeUpdateDTO("");

        Set<ConstraintViolation<TimeUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullName_producesViolation() {
        TimeUpdateDTO dto = new TimeUpdateDTO(null);

        Set<ConstraintViolation<TimeUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void setter_updatesName() {
        TimeUpdateDTO dto = new TimeUpdateDTO();

        dto.setNome("Updated Time");

        assertEquals("Updated Time", dto.getNome());
    }
}
