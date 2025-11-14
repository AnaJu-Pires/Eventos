package br.ifsp.events.dto.partida;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PartidaResponseDTOTest {

    @Test
    void builderSetsAllFields() {
        PartidaResponseDTO dto = PartidaResponseDTO.builder()
                .id(1L)
                .round(1)
                .statusPartida("AGUARDANDO")
                .time1Nome("Time A")
                .time1Placar(2)
                .time2Nome("Time B")
                .time2Placar(1)
                .vencedorNome("Time A")
                .build();

        assertEquals(1L, dto.getId());
        assertEquals(1, dto.getRound());
        assertEquals("AGUARDANDO", dto.getStatusPartida());
        assertEquals("Time A", dto.getTime1Nome());
        assertEquals(2, dto.getTime1Placar());
        assertEquals("Time B", dto.getTime2Nome());
        assertEquals(1, dto.getTime2Placar());
        assertEquals("Time A", dto.getVencedorNome());
    }

    @Test
    void gettersReturnCorrectValues() {
        PartidaResponseDTO dto = PartidaResponseDTO.builder()
                .id(5L)
                .round(3)
                .statusPartida("ENCERRADA")
                .time1Nome("Equipe X")
                .time1Placar(3)
                .time2Nome("Equipe Y")
                .time2Placar(2)
                .vencedorNome("Equipe X")
                .build();

        assertEquals(5L, dto.getId());
        assertEquals(3, dto.getRound());
        assertEquals(3, dto.getTime1Placar());
        assertEquals(2, dto.getTime2Placar());
    }

    @Test
    void builderPartiallySet() {
        PartidaResponseDTO dto = PartidaResponseDTO.builder()
                .id(2L)
                .round(2)
                .build();

        assertEquals(2L, dto.getId());
        assertEquals(2, dto.getRound());
        assertNull(dto.getStatusPartida());
    }
}
