package br.ifsp.events.dto.event;

import br.ifsp.events.model.FormatoEventoModalidade;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EventoModalidadePatchDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        EventoModalidadePatchDTO dto = new EventoModalidadePatchDTO();

        assertNull(dto.getModalidadeId());
        assertEquals(0, dto.getMaxTimes());
        assertNull(dto.getDataFimInscricao());
        assertNull(dto.getFormatoEventoModalidade());
    }

    @Test
    void allArgsConstructor_setsFields() {
        LocalDate data = LocalDate.now().plusDays(5);

        EventoModalidadePatchDTO dto = new EventoModalidadePatchDTO(
                2L, 12, data, FormatoEventoModalidade.PONTOS_CORRIDOS
        );

        assertEquals(2L, dto.getModalidadeId());
        assertEquals(12, dto.getMaxTimes());
        assertEquals(data, dto.getDataFimInscricao());
        assertEquals(FormatoEventoModalidade.PONTOS_CORRIDOS, dto.getFormatoEventoModalidade());
    }

    @Test
    void setters_updateValues() {
        EventoModalidadePatchDTO dto = new EventoModalidadePatchDTO();

        dto.setModalidadeId(3L);
        dto.setMaxTimes(6);
        dto.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);

        assertEquals(3L, dto.getModalidadeId());
        assertEquals(6, dto.getMaxTimes());
        assertEquals(FormatoEventoModalidade.MATA_MATA, dto.getFormatoEventoModalidade());
    }
}
