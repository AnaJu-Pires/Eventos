package br.ifsp.events.dto.post;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PostResponseDTOTest {

    @Test
    void builderSetsAllFields() {
        LocalDateTime now = LocalDateTime.now();

        PostResponseDTO dto = PostResponseDTO.builder()
                .id(1L)
                .titulo("Titulo")
                .conteudo("Conteudo")
                .autorNome("Autor")
                .comunidadeNome("Comunidade")
                .dataCriacao(now)
                .votos(5)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Titulo", dto.getTitulo());
        assertEquals("Conteudo", dto.getConteudo());
        assertEquals("Autor", dto.getAutorNome());
        assertEquals("Comunidade", dto.getComunidadeNome());
        assertEquals(now, dto.getDataCriacao());
        assertEquals(5, dto.getVotos());
    }
}
