package br.ifsp.events.dto.event;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventRequestDTOTest {

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
    void validEventRequest_hasNoViolations() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(10);
        Set<EventoModalidadeRequestDTO> modalidades = new HashSet<>();

        EventRequestDTO dto = new EventRequestDTO("Torneio 2025", "Descrição", start, end, modalidades);

        Set<ConstraintViolation<EventRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankNameProducesViolation() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(10);

        EventRequestDTO dto = new EventRequestDTO("", "Descrição", start, end, new HashSet<>());

        Set<ConstraintViolation<EventRequestDTO>> violations = validator.validate(dto);
        boolean nomeViolated = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome"));
        assertTrue(nomeViolated);
    }

    @Test
    void nullDateProducesViolation() {
        EventRequestDTO dto = new EventRequestDTO("Torneio", "Desc", null, null, new HashSet<>());

        Set<ConstraintViolation<EventRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void pastDateProducesViolation() {
        LocalDate pastDate = LocalDate.now().minusDays(5);

        EventRequestDTO dto = new EventRequestDTO("Torneio", "Desc", pastDate, LocalDate.now().plusDays(5), new HashSet<>());

        Set<ConstraintViolation<EventRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullModalidadesProducesViolation() {
        LocalDate start = LocalDate.now().plusDays(1);

        EventRequestDTO dto = new EventRequestDTO("Torneio", "Desc", start, start.plusDays(5), null);

        Set<ConstraintViolation<EventRequestDTO>> violations = validator.validate(dto);
        boolean modalidadesViolated = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("modalidades"));
        assertTrue(modalidadesViolated);
    }
}
