package br.ifsp.events.dto.voto;

import br.ifsp.events.model.TipoVoto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VotoCreateDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void teardown() {
        factory.close();
    }

    @Test
    void validVoto_hasNoViolations() {
        VotoCreateDTO dto = new VotoCreateDTO();
        dto.setPostId(1L);
        dto.setTipoVoto(TipoVoto.UPVOTE);

        Set<ConstraintViolation<VotoCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullTipoVoto_producesViolation() {
        VotoCreateDTO dto = new VotoCreateDTO();
        dto.setPostId(1L);
        dto.setTipoVoto(null);

        Set<ConstraintViolation<VotoCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
