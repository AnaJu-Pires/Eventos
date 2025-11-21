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

class CapitaoTransferDTOTest {

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
    void validCapitaoTransfer_hasNoViolations() {
        CapitaoTransferDTO dto = new CapitaoTransferDTO(5L);

        Set<ConstraintViolation<CapitaoTransferDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullNovoCapitaoId_producesViolation() {
        CapitaoTransferDTO dto = new CapitaoTransferDTO(null);

        Set<ConstraintViolation<CapitaoTransferDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void setter_updatesNovoCapitaoId() {
        CapitaoTransferDTO dto = new CapitaoTransferDTO();

        dto.setNovoCapitaoId(10L);

        assertEquals(10L, dto.getNovoCapitaoId());
    }
}
