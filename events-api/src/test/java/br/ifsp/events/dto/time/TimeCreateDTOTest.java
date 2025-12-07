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

class TimeCreateDTOTest {

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
    void validTimeCreate_hasNoViolations() {
        TimeCreateDTO dto = new TimeCreateDTO("Time A", 1L);

        Set<ConstraintViolation<TimeCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankName_producesViolation() {
        TimeCreateDTO dto = new TimeCreateDTO("", 1L);

        Set<ConstraintViolation<TimeCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullModalidadeId_producesViolation() {
        TimeCreateDTO dto = new TimeCreateDTO("Time A", null);

        Set<ConstraintViolation<TimeCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void setters_updateValues() {
        TimeCreateDTO dto = new TimeCreateDTO();

        dto.setNome("Team B");
        dto.setModalidadeId(2L);

        assertEquals("Team B", dto.getNome());
        assertEquals(2L, dto.getModalidadeId());
    }
}
