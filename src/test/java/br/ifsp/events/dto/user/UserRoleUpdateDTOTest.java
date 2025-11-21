package br.ifsp.events.dto.user;

import br.ifsp.events.model.PerfilUser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleUpdateDTOTest {

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
    void validRoleUpdate_hasNoViolations() {
        UserRoleUpdateDTO dto = new UserRoleUpdateDTO(PerfilUser.ADMIN);

        Set<ConstraintViolation<UserRoleUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullRole_producesViolation() {
        UserRoleUpdateDTO dto = new UserRoleUpdateDTO(null);

        Set<ConstraintViolation<UserRoleUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void differentRoles_areValidated() {
        UserRoleUpdateDTO dtoAluno = new UserRoleUpdateDTO(PerfilUser.ALUNO);
        UserRoleUpdateDTO dtoAdmin = new UserRoleUpdateDTO(PerfilUser.ADMIN);

        Set<ConstraintViolation<UserRoleUpdateDTO>> violations1 = validator.validate(dtoAluno);
        Set<ConstraintViolation<UserRoleUpdateDTO>> violations2 = validator.validate(dtoAdmin);

        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
        assertNotEquals(dtoAluno.getPerfilUser(), dtoAdmin.getPerfilUser());
    }
}
