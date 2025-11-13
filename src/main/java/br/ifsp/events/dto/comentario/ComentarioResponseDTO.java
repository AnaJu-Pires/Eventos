package br.ifsp.events.dto.comentario;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComentarioResponseDTO {
    private Long id;
    private String conteudo;
    private String autorNome;
    private Long postId;
    private Long comentarioPaiId;
    private LocalDateTime dataCriacao;
    private int votos;
}