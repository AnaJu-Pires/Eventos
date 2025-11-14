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
                1L, "Ana Silva", "ana@aluno.ifsp.edu.br", PerfilUser.ALUNO, StatusUser.ATIVO
        );

        assertEquals(1L, dto.getId());
        assertEquals("Ana Silva", dto.getNome());
        assertEquals("ana@aluno.ifsp.edu.br", dto.getEmail());
        assertEquals(PerfilUser.ALUNO, dto.getPerfilUser());
        assertEquals(StatusUser.ATIVO, dto.getStatus());
    }

    @Test
    void setters_updateValues() {
        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(2L);
        dto.setNome("João");
        dto.setEmail("joao@ifsp.edu.br");
        dto.setPerfilUser(PerfilUser.ADMIN);
        dto.setStatus(StatusUser.INATIVO);

        assertEquals(2L, dto.getId());
        assertEquals("João", dto.getNome());
        assertEquals(PerfilUser.ADMIN, dto.getPerfilUser());
        assertEquals(StatusUser.INATIVO, dto.getStatus());
    }

    @Test
    void equality_twoInstancesWithSameData() {
        UserResponseDTO dto1 = new UserResponseDTO(1L, "Ana", "ana@ifsp.edu.br", PerfilUser.ALUNO, StatusUser.ATIVO);
        UserResponseDTO dto2 = new UserResponseDTO(1L, "Ana", "ana@ifsp.edu.br", PerfilUser.ALUNO, StatusUser.ATIVO);

        assertEquals(dto1, dto2);
    }
}
