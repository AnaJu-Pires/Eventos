package br.ifsp.events.dto.event;

import br.ifsp.events.model.StatusEvento;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class EventResponseDTOTest {

    @Test
    void builderSetsAllFields() {
        LocalDate dataInicio = LocalDate.now();
        LocalDate dataFim = LocalDate.now().plusDays(5);

        EventResponseDTO dto = EventResponseDTO.builder()
                .id(1L)
                .nome("Torneio 2025")
                .descricao("Descrição do evento")
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .status(StatusEvento.PLANEJADO)
                .organizadorNome("Admin")
                .eventoModalidades(new HashSet<>())
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Torneio 2025", dto.getNome());
        assertEquals("Descrição do evento", dto.getDescricao());
        assertEquals(dataInicio, dto.getDataInicio());
        assertEquals(dataFim, dto.getDataFim());
        assertEquals(StatusEvento.PLANEJADO, dto.getStatus());
        assertEquals("Admin", dto.getOrganizadorNome());
    }

    @Test
    void noArgsConstructorCreatesEmptyDto() {
        EventResponseDTO dto = new EventResponseDTO();

        assertNull(dto.getId());
        assertNull(dto.getNome());
    }

    @Test
    void allArgsConstructorSetsFields() {
        LocalDate data = LocalDate.now();

        EventResponseDTO dto = new EventResponseDTO(
                2L, "Evento2", "Desc2", data, data, StatusEvento.FINALIZADO, "Org", new HashSet<>()
        );

        assertEquals(2L, dto.getId());
        assertEquals("Evento2", dto.getNome());
        assertEquals(StatusEvento.FINALIZADO, dto.getStatus());
    }
}
