package br.ifsp.events.dto.user;

import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        UserResponseDTO dto = new UserResponseDTO();

        assertNull(dto.getId());
        assertNull(dto.getNome());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        UserResponseDTO dto = new UserResponseDTO(
                1L,
                "Ana Silva",
                "ana@aluno.ifsp.edu.br",
                PerfilUser.ALUNO,
                StatusUser.ATIVO,
                100L,   
                "BRONZE", 
                "NENHUM" 
        );

        assertEquals(1L, dto.getId());
        assertEquals("Ana Silva", dto.getNome());
        assertEquals("ana@aluno.ifsp.edu.br", dto.getEmail());
        assertEquals(PerfilUser.ALUNO, dto.getPerfilUser());
        assertEquals(StatusUser.ATIVO, dto.getStatus());
        assertEquals(100L, dto.getPontosSaldo());
        assertEquals("BRONZE", dto.getNivel());
    }

    @Test
    void setters_updateValues() {
        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(2L);
        dto.setNome("João");
        dto.setEmail("joao@ifsp.edu.br");
        dto.setPerfilUser(PerfilUser.ADMIN);
        dto.setStatus(StatusUser.INATIVO);
        dto.setPontosSaldo(500L);
        dto.setNivel("PRATA");

        assertEquals(2L, dto.getId());
        assertEquals("João", dto.getNome());
        assertEquals(PerfilUser.ADMIN, dto.getPerfilUser());
        assertEquals(StatusUser.INATIVO, dto.getStatus());
        assertEquals(500L, dto.getPontosSaldo());
        assertEquals("PRATA", dto.getNivel());
    }

    @Test
    void equality_twoInstancesWithSameData() {
        UserResponseDTO dto1 = new UserResponseDTO(1L, "Ana", "ana@ifsp.edu.br", PerfilUser.ALUNO, StatusUser.ATIVO, 50L, "PRATA", "NENHUM");
        UserResponseDTO dto2 = new UserResponseDTO(1L, "Ana", "ana@ifsp.edu.br", PerfilUser.ALUNO, StatusUser.ATIVO, 50L, "PRATA", "NENHUM");

        assertEquals(dto1, dto2);
    }
}