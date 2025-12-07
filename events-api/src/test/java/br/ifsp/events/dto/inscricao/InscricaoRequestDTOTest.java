package br.ifsp.events.dto.inscricao;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InscricaoRequestDTOTest {

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
    void validTimeId_hasNoViolations() {
        InscricaoRequestDTO dto = new InscricaoRequestDTO(1L);

        Set<ConstraintViolation<InscricaoRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullTimeId_producesViolation() {
        InscricaoRequestDTO dto = new InscricaoRequestDTO(null);

        Set<ConstraintViolation<InscricaoRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void setter_updatesTimeId() {
        InscricaoRequestDTO dto = new InscricaoRequestDTO();

        dto.setTimeId(5L);

        assertEquals(5L, dto.getTimeId());
    }

    @Test
    void getter_returnsTimeId() {
        InscricaoRequestDTO dto = new InscricaoRequestDTO(10L);

        assertEquals(10L, dto.getTimeId());
    }
}
