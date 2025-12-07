package br.ifsp.events.dto.comunidade;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ComunidadeResponseDTOTest {

    @Test
    void builderSetsAllFields() {
        LocalDateTime now = LocalDateTime.now();

        ComunidadeResponseDTO dto = ComunidadeResponseDTO.builder()
                .id(1L)
                .nome("Clube")
                .descricao("Descricao")
                .criadorNome("Criador")
                .dataCriacao(now)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Clube", dto.getNome());
        assertEquals("Descricao", dto.getDescricao());
        assertEquals("Criador", dto.getCriadorNome());
        assertEquals(now, dto.getDataCriacao());
    }
}
