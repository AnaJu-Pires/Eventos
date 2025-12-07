package br.ifsp.events.dto.convite;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConviteCreateDTOTest {

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
    void validEmail_hasNoViolations() {
        ConviteCreateDTO dto = new ConviteCreateDTO("membro@aluno.ifsp.edu.br");

        Set<ConstraintViolation<ConviteCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validEmailWithoutAluno_hasNoViolations() {
        ConviteCreateDTO dto = new ConviteCreateDTO("professor@ifsp.edu.br");

        Set<ConstraintViolation<ConviteCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidEmail_producesViolation() {
        ConviteCreateDTO dto = new ConviteCreateDTO("usuario@gmail.com");

        Set<ConstraintViolation<ConviteCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void blankEmail_producesViolation() {
        ConviteCreateDTO dto = new ConviteCreateDTO("");

        Set<ConstraintViolation<ConviteCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullEmail_producesViolation() {
        ConviteCreateDTO dto = new ConviteCreateDTO(null);

        Set<ConstraintViolation<ConviteCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void setter_updatesEmail() {
        ConviteCreateDTO dto = new ConviteCreateDTO();

        dto.setEmailUsuario("new@aluno.ifsp.edu.br");

        assertEquals("new@aluno.ifsp.edu.br", dto.getEmailUsuario());
    }
}
