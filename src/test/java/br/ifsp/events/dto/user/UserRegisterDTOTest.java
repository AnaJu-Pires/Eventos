package br.ifsp.events.dto.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRegisterDTOTest {

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
    void validUserRegisterDTO_hasNoViolations() {
        UserRegisterDTO dto = new UserRegisterDTO("Ana Silva", "ana.silva@aluno.ifsp.edu.br", "senhaForte1");

        Set<ConstraintViolation<UserRegisterDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), () -> "Violations: " + violations);
    }

    @Test
    void invalidEmailProducesViolation() {
        UserRegisterDTO dto = new UserRegisterDTO("Ana Silva", "ana@gmail.com", "senhaForte1");

        Set<ConstraintViolation<UserRegisterDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        boolean emailViolated = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(emailViolated, "Expected a violation on email property");
    }

    @Test
    void blankFieldsProduceViolations() {
        UserRegisterDTO dto = new UserRegisterDTO("", "", "");

        Set<ConstraintViolation<UserRegisterDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        boolean nomeViolated = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome"));
        boolean senhaViolated = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("senha"));
        assertTrue(nomeViolated, "Expected a violation on nome property");
        assertTrue(senhaViolated, "Expected a violation on senha property");
    }
}
