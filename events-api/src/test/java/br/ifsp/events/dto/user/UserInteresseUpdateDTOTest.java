package br.ifsp.events.dto.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserInteresseUpdateDTOTest {

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
    void validModalidadeList_hasNoViolations() {
        List<Long> ids = Arrays.asList(1L, 5L, 12L);
        UserInteresseUpdateDTO dto = new UserInteresseUpdateDTO(ids);

        Set<ConstraintViolation<UserInteresseUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void emptyList_producesViolation() {
        UserInteresseUpdateDTO dto = new UserInteresseUpdateDTO(Arrays.asList());

        Set<ConstraintViolation<UserInteresseUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullList_producesViolation() {
        UserInteresseUpdateDTO dto = new UserInteresseUpdateDTO(null);

        Set<ConstraintViolation<UserInteresseUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void singleModalidade_isValid() {
        UserInteresseUpdateDTO dto = new UserInteresseUpdateDTO(Arrays.asList(99L));

        Set<ConstraintViolation<UserInteresseUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}
