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

class UserLoginDTOTest {

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
    void validLogin_hasNoViolations() {
        UserLoginDTO dto = new UserLoginDTO("ana.silva@aluno.ifsp.edu.br", "senhaForte1");

        Set<ConstraintViolation<UserLoginDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validLoginWithIfspEmail_hasNoViolations() {
        UserLoginDTO dto = new UserLoginDTO("ana.silva@ifsp.edu.br", "senhaForte1");

        Set<ConstraintViolation<UserLoginDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidEmail_producesViolation() {
        UserLoginDTO dto = new UserLoginDTO("ana@gmail.com", "senhaForte1");

        Set<ConstraintViolation<UserLoginDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void blankEmail_producesViolation() {
        UserLoginDTO dto = new UserLoginDTO("", "senhaForte1");

        Set<ConstraintViolation<UserLoginDTO>> violations = validator.validate(dto);
        boolean emailViolated = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(emailViolated);
    }

    @Test
    void passwordTooShort_producesViolation() {
        UserLoginDTO dto = new UserLoginDTO("ana.silva@aluno.ifsp.edu.br", "123");

        Set<ConstraintViolation<UserLoginDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void blankPassword_producesViolation() {
        UserLoginDTO dto = new UserLoginDTO("ana.silva@aluno.ifsp.edu.br", "");

        Set<ConstraintViolation<UserLoginDTO>> violations = validator.validate(dto);
        boolean senhaViolated = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("senha"));
        assertTrue(senhaViolated);
    }
}
