package br.ifsp.events.dto.time;

import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TimeResponseDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        TimeResponseDTO dto = new TimeResponseDTO();

        assertNull(dto.getId());
        assertNull(dto.getNome());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        UserResponseDTO capitao = new UserResponseDTO(
                1L, "Ana", "ana@ifsp.edu.br", br.ifsp.events.model.PerfilUser.ALUNO, 10L, br.ifsp.events.model.NivelEngajamento.BRONZE, br.ifsp.events.model.RankEngajamento.NENHUM, br.ifsp.events.model.StatusUser.ATIVO
        );
        ModalidadeResponseDTO modalidade = new ModalidadeResponseDTO(1L, "Futebol", "Esporte");
        Set<UserResponseDTO> membros = new HashSet<>();

        TimeResponseDTO dto = new TimeResponseDTO(1L, "Time A", capitao, modalidade, membros);

        assertEquals(1L, dto.getId());
        assertEquals("Time A", dto.getNome());
        assertEquals(capitao, dto.getCapitao());
        assertEquals(modalidade, dto.getModalidade());
        assertEquals(membros, dto.getMembros());
    }

    @Test
    void setters_updateAllFields() {
        TimeResponseDTO dto = new TimeResponseDTO();

        dto.setId(2L);
        dto.setNome("Time B");

        assertEquals(2L, dto.getId());
        assertEquals("Time B", dto.getNome());
    }
}
