package br.ifsp.events.dto.post;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PostCreateDTOTest {

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
    void validPostCreate_hasNoViolations() {
        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitulo("Título válido");
        dto.setConteudo("Conteúdo do post.");

        Set<ConstraintViolation<PostCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankTitle_producesViolation() {
        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitulo("");
        dto.setConteudo("Conteúdo");

        Set<ConstraintViolation<PostCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void blankContent_producesViolation() {
        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitulo("Titulo");
        dto.setConteudo("");

        Set<ConstraintViolation<PostCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
