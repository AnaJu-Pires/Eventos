package br.ifsp.events.dto.event;

import br.ifsp.events.model.FormatoEventoModalidade;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GerarChaveRequestDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @Test
    void whenFormatoIsNull_thenViolationIsRaised() {
        GerarChaveRequestDTO dto = new GerarChaveRequestDTO();
        assertNull(dto.getFormato());

        Set<ConstraintViolation<GerarChaveRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected a NotNull violation for formato");
    }

    @Test
    void whenFormatoIsSet_thenNoViolations() {
        GerarChaveRequestDTO dto = new GerarChaveRequestDTO();
        dto.setFormato(FormatoEventoModalidade.MATA_MATA);

        Set<ConstraintViolation<GerarChaveRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), () -> "Unexpected violations: " + violations);
        assertEquals(FormatoEventoModalidade.MATA_MATA, dto.getFormato());
    }
}
