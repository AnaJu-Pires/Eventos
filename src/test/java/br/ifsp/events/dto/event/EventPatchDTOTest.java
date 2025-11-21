package br.ifsp.events.dto.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EventPatchDTOTest {

    @Test
    void noArgsConstructor_createsEmptyOptionals() {
        EventPatchDTO dto = new EventPatchDTO();

        assertTrue(dto.getNome().isEmpty());
        assertTrue(dto.getDescricao().isEmpty());
        assertTrue(dto.getDataInicio().isEmpty());
        assertTrue(dto.getDataFim().isEmpty());
        assertTrue(dto.getModalidades().isEmpty());
    }

    @Test
    void allArgsConstructor_setsPresentValues() {
        String nome = "Novo Nome";
        String descricao = "Nova Descricao";
        LocalDate data = LocalDate.now().plusDays(5);
        HashSet<EventoModalidadePatchDTO> modalidades = new HashSet<>();

        EventPatchDTO dto = new EventPatchDTO(
                Optional.of(nome),
                Optional.of(descricao),
                Optional.of(data),
                Optional.of(data),
                Optional.of(modalidades)
        );

        assertTrue(dto.getNome().isPresent());
        assertEquals(nome, dto.getNome().get());
        assertTrue(dto.getDescricao().isPresent());
        assertEquals(descricao, dto.getDescricao().get());
        assertTrue(dto.getDataInicio().isPresent());
        assertTrue(dto.getModalidades().isPresent());
    }

    @Test
    void settersWorkCorrectly() {
        EventPatchDTO dto = new EventPatchDTO();

        dto.setNome(Optional.of("Updated"));
        assertTrue(dto.getNome().isPresent());
        assertEquals("Updated", dto.getNome().get());
    }
}
