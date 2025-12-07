package br.ifsp.events.dto.event;

import br.ifsp.events.model.FormatoEventoModalidade;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventoModalidadeRequestDTOTest {

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
        LocalDate dataFim = LocalDate.now().plusDays(10);
        EventoModalidadeRequestDTO dto = new EventoModalidadeRequestDTO(
                1L, 8, dataFim, FormatoEventoModalidade.MATA_MATA
        );

        Set<ConstraintViolation<EventoModalidadeRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullModalidadeId_producesViolation() {
        EventoModalidadeRequestDTO dto = new EventoModalidadeRequestDTO(
                null, 4, LocalDate.now().plusDays(5), FormatoEventoModalidade.PONTOS_CORRIDOS
        );

        Set<ConstraintViolation<EventoModalidadeRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullDataFimInscricao_producesViolation() {
        EventoModalidadeRequestDTO dto = new EventoModalidadeRequestDTO(
                1L, 4, null, FormatoEventoModalidade.MATA_MATA
        );

        Set<ConstraintViolation<EventoModalidadeRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullFormato_producesViolation() {
        EventoModalidadeRequestDTO dto = new EventoModalidadeRequestDTO(
                1L, 4, LocalDate.now().plusDays(5), null
        );

        Set<ConstraintViolation<EventoModalidadeRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void setters_updateValues() {
        EventoModalidadeRequestDTO dto = new EventoModalidadeRequestDTO();

        dto.setModalidadeId(2L);
        dto.setMaxTimes(16);
        dto.setDataFimInscricao(LocalDate.now().plusDays(3));
        dto.setFormatoEventoModalidade(FormatoEventoModalidade.PONTOS_CORRIDOS);

        assertEquals(2L, dto.getModalidadeId());
        assertEquals(16, dto.getMaxTimes());
        assertEquals(FormatoEventoModalidade.PONTOS_CORRIDOS, dto.getFormatoEventoModalidade());
    }
}
