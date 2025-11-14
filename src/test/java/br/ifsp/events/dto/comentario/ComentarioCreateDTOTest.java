package br.ifsp.events.dto.comentario;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComentarioCreateDTOTest {

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
    void validComentario_hasNoViolations() {
        ComentarioCreateDTO dto = new ComentarioCreateDTO();
        dto.setConteudo("Comentário válido");

        Set<ConstraintViolation<ComentarioCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankConteudo_producesViolation() {
        ComentarioCreateDTO dto = new ComentarioCreateDTO();
        dto.setConteudo("");

        Set<ConstraintViolation<ComentarioCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
