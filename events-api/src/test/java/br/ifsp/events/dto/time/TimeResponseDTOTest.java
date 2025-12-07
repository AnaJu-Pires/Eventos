package br.ifsp.events.dto.time;

import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;

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
                1L,
                "Ana Silva",
                "ana@aluno.ifsp.edu.br",
                PerfilUser.ALUNO,
                StatusUser.ATIVO,
                100L,
                "BRONZE",  
                "NENHUM"        
        );
        ModalidadeResponseDTO modalidade = new ModalidadeResponseDTO(1L, "Futebol", "Esporte");
        Set<UserResponseDTO> membros = new HashSet<>();

        // CORREÇÃO: Adicionado o 6º argumento 'winRate' (0.0)
        TimeResponseDTO dto = new TimeResponseDTO(1L, "Time A", capitao, modalidade, membros, 0.0);

        assertEquals(1L, dto.getId());
        assertEquals("Time A", dto.getNome());
        assertEquals(capitao, dto.getCapitao());
        assertEquals(modalidade, dto.getModalidade());
        assertEquals(membros, dto.getMembros());
        assertEquals(0.0, dto.getWinRate()); // Adicionado assert para o novo campo
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
