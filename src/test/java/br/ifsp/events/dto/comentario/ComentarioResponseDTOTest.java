package br.ifsp.events.dto.comentario;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ComentarioResponseDTOTest {

    @Test
    void builderSetsAllFields() {
        LocalDateTime now = LocalDateTime.now();

        ComentarioResponseDTO dto = ComentarioResponseDTO.builder()
                .id(1L)
                .conteudo("Conteudo")
                .autorNome("Autor")
                .postId(3L)
                .comentarioPaiId(null)
                .dataCriacao(now)
                .votos(2)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Conteudo", dto.getConteudo());
        assertEquals("Autor", dto.getAutorNome());
        assertEquals(3L, dto.getPostId());
        assertEquals(now, dto.getDataCriacao());
        assertEquals(2, dto.getVotos());
    }
}
