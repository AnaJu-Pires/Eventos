package br.ifsp.events.dto.modalidade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModalidadePatchRequestDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        ModalidadePatchRequestDTO dto = new ModalidadePatchRequestDTO();

        assertNull(dto.getNome());
        assertNull(dto.getDescricao());
    }

    @Test
    void allArgsConstructor_setsFields() {
        ModalidadePatchRequestDTO dto = new ModalidadePatchRequestDTO(
                "Futsal Feminino", "Exclusivo para equipes femininas"
        );

        assertEquals("Futsal Feminino", dto.getNome());
        assertEquals("Exclusivo para equipes femininas", dto.getDescricao());
    }

    @Test
    void setters_updateValues() {
        ModalidadePatchRequestDTO dto = new ModalidadePatchRequestDTO();

        dto.setNome("Handebol");
        dto.setDescricao("Esporte de quadra com bola");

        assertEquals("Handebol", dto.getNome());
        assertEquals("Esporte de quadra com bola", dto.getDescricao());
    }

    @Test
    void partialUpdate_onlyNome() {
        ModalidadePatchRequestDTO dto = new ModalidadePatchRequestDTO();

        dto.setNome("Novo Nome");
        assertNotNull(dto.getNome());
        assertNull(dto.getDescricao());
    }
}
