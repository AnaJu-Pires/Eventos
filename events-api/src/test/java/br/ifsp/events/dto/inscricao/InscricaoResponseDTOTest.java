package br.ifsp.events.dto.inscricao;

import br.ifsp.events.model.StatusInscricao;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InscricaoResponseDTOTest {

    @Test
    void builderSetsAllFields() {
        Set<String> jogadores = new HashSet<>();
        jogadores.add("João");
        jogadores.add("Maria");

        InscricaoResponseDTO dto = InscricaoResponseDTO.builder()
                .id(1L)
                .nomeTime("Time Alpha")
                .nomeEvento("Torneio 2025")
                .nomeModalidade("Futebol")
                .statusInscricao(StatusInscricao.APROVADA)
                .nomeCapitao("João Silva")
                .nomesJogadores(jogadores)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Time Alpha", dto.getNomeTime());
        assertEquals("Torneio 2025", dto.getNomeEvento());
        assertEquals("Futebol", dto.getNomeModalidade());
        assertEquals(StatusInscricao.APROVADA, dto.getStatusInscricao());
        assertEquals("João Silva", dto.getNomeCapitao());
        assertEquals(2, dto.getNomesJogadores().size());
    }

    @Test
    void builderCreatesValidDto() {
        InscricaoResponseDTO dto = InscricaoResponseDTO.builder()
                .id(2L)
                .nomeTime("Team Beta")
                .build();

        assertEquals(2L, dto.getId());
        assertEquals("Team Beta", dto.getNomeTime());
    }

    @Test
    void noArgsConstructor_createsEmptyDto() {
        InscricaoResponseDTO dto = InscricaoResponseDTO.builder().build();

        assertNull(dto.getId());
        assertNull(dto.getNomeTime());
    }

    @Test
    void setters_updateValues() {
        InscricaoResponseDTO dto = InscricaoResponseDTO.builder().build();

        dto.setId(2L);
        dto.setNomeTime("Team Beta");
        dto.setStatusInscricao(StatusInscricao.PENDENTE);

        assertEquals(2L, dto.getId());
        assertEquals("Team Beta", dto.getNomeTime());
        assertEquals(StatusInscricao.PENDENTE, dto.getStatusInscricao());
    }

    @Test
    void differentStatuses() {
        InscricaoResponseDTO dto1 = InscricaoResponseDTO.builder()
                .statusInscricao(StatusInscricao.APROVADA)
                .build();
        InscricaoResponseDTO dto2 = InscricaoResponseDTO.builder()
                .statusInscricao(StatusInscricao.REJEITADA)
                .build();

        assertNotEquals(dto1.getStatusInscricao(), dto2.getStatusInscricao());
    }
}
