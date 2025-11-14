package br.ifsp.events.dto.user;

import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserInteresseResponseDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        UserInteresseResponseDTO dto = new UserInteresseResponseDTO();

        assertNull(dto.getInteresses());
    }

    @Test
    void allArgsConstructor_setsInteresses() {
        List<ModalidadeResponseDTO> interesses = new ArrayList<>();
        interesses.add(new ModalidadeResponseDTO(1L, "Futebol", "Esporte coletivo"));
        interesses.add(new ModalidadeResponseDTO(2L, "Voleibol", "Esporte de rede"));

        UserInteresseResponseDTO dto = new UserInteresseResponseDTO(interesses);

        assertEquals(2, dto.getInteresses().size());
        assertTrue(dto.getInteresses().contains(interesses.get(0)));
    }

    @Test
    void setter_updatesInteresses() {
        UserInteresseResponseDTO dto = new UserInteresseResponseDTO();
        List<ModalidadeResponseDTO> interesses = Arrays.asList(
                new ModalidadeResponseDTO(1L, "Basquete", "Esporte")
        );

        dto.setInteresses(interesses);

        assertNotNull(dto.getInteresses());
        assertEquals(1, dto.getInteresses().size());
    }
}
