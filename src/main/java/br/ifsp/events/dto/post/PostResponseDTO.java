package br.ifsp.events.dto.post;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PostResponseDTO {
    private Long id;
    private String titulo;
    private String conteudo;
    private String autorNome;
    private String comunidadeNome;
    private LocalDateTime dataCriacao;
    private int votos;
}