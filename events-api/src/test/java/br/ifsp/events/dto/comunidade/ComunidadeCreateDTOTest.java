package br.ifsp.events.dto.comunidade;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComunidadeCreateDTOTest {

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
    void validComunidade_hasNoViolations() {
        ComunidadeCreateDTO dto = new ComunidadeCreateDTO();
        dto.setNome("Clube de Xadrez");
        dto.setDescricao("Espaço para fãs de xadrez.");

        Set<ConstraintViolation<ComunidadeCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankNome_producesViolation() {
        ComunidadeCreateDTO dto = new ComunidadeCreateDTO();
        dto.setNome("");
        dto.setDescricao("Descricao");

        Set<ConstraintViolation<ComunidadeCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void descricaoTooLong_producesViolation() {
        ComunidadeCreateDTO dto = new ComunidadeCreateDTO();
        dto.setNome("Nome");
        dto.setDescricao("A".repeat(300));

        Set<ConstraintViolation<ComunidadeCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
