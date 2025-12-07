package br.ifsp.events.dto.modalidade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModalidadeResponseDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        ModalidadeResponseDTO dto = new ModalidadeResponseDTO();

        assertNull(dto.getId());
        assertNull(dto.getNome());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        ModalidadeResponseDTO dto = new ModalidadeResponseDTO(
                1L, "Futebol", "Esporte com bola"
        );

        assertEquals(1L, dto.getId());
        assertEquals("Futebol", dto.getNome());
        assertEquals("Esporte com bola", dto.getDescricao());
    }

    @Test
    void setters_updateValues() {
        ModalidadeResponseDTO dto = new ModalidadeResponseDTO();

        dto.setId(2L);
        dto.setNome("Basquete");
        dto.setDescricao("Esporte de quadra");

        assertEquals(2L, dto.getId());
        assertEquals("Basquete", dto.getNome());
        assertEquals("Esporte de quadra", dto.getDescricao());
    }

    @Test
    void equality_sameData() {
        ModalidadeResponseDTO dto1 = new ModalidadeResponseDTO(1L, "Vôlei", "Esporte");
        ModalidadeResponseDTO dto2 = new ModalidadeResponseDTO(1L, "Vôlei", "Esporte");

        assertEquals(dto1, dto2);
    }
}
