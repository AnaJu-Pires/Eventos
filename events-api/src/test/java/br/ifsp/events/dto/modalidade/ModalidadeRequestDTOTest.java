package br.ifsp.events.dto.modalidade;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModalidadeRequestDTOTest {

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
    void validModalidade_hasNoViolations() {
        ModalidadeRequestDTO dto = new ModalidadeRequestDTO("Voleibol de Praia", "Jogo disputado na areia por duas duplas.");

        Set<ConstraintViolation<ModalidadeRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankNome_producesViolation() {
        ModalidadeRequestDTO dto = new ModalidadeRequestDTO("", "Descrição");

        Set<ConstraintViolation<ModalidadeRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nomeTooShort_producesViolation() {
        ModalidadeRequestDTO dto = new ModalidadeRequestDTO("AB", "Descrição válida");

        Set<ConstraintViolation<ModalidadeRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nomeTooLong_producesViolation() {
        String longNome = "A".repeat(101);
        ModalidadeRequestDTO dto = new ModalidadeRequestDTO(longNome, "Descrição");

        Set<ConstraintViolation<ModalidadeRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void blankDescricao_producesViolation() {
        ModalidadeRequestDTO dto = new ModalidadeRequestDTO("Voleibol", "");

        Set<ConstraintViolation<ModalidadeRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void descricaoTooLong_producesViolation() {
        String longDescricao = "A".repeat(256);
        ModalidadeRequestDTO dto = new ModalidadeRequestDTO("Voleibol", longDescricao);

        Set<ConstraintViolation<ModalidadeRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
