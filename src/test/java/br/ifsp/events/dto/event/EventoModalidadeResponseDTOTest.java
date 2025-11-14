package br.ifsp.events.dto.event;

import br.ifsp.events.model.FormatoEventoModalidade;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EventoModalidadeResponseDTOTest {

    @Test
    void builderSetsAllFields() {
        LocalDate data = LocalDate.now().plusDays(7);

        EventoModalidadeResponseDTO dto = EventoModalidadeResponseDTO.builder()
                .id(1L)
                .modalidadeNome("Futebol")
                .maxTimes(4)
                .dataFimInscricao(data)
                .formatoEventoModalidade(FormatoEventoModalidade.MATA_MATA)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Futebol", dto.getModalidadeNome());
        assertEquals(4, dto.getMaxTimes());
        assertEquals(data, dto.getDataFimInscricao());
        assertEquals(FormatoEventoModalidade.MATA_MATA, dto.getFormatoEventoModalidade());
    }

    @Test
    void noArgsConstructor_createsEmptyDto() {
        EventoModalidadeResponseDTO dto = new EventoModalidadeResponseDTO();

        assertNull(dto.getId());
        assertNull(dto.getModalidadeNome());
    }

    @Test
    void setters_updateValues() {
        EventoModalidadeResponseDTO dto = new EventoModalidadeResponseDTO();

        dto.setId(2L);
        dto.setModalidadeNome("Voleibol");
        dto.setMaxTimes(8);

        assertEquals(2L, dto.getId());
        assertEquals("Voleibol", dto.getModalidadeNome());
        assertEquals(8, dto.getMaxTimes());
    }
}
