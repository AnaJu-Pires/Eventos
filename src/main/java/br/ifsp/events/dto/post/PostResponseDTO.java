package br.ifsp.events.dto.post;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO de resposta para um Post")
public class PostResponseDTO {

    @Schema(description = "ID único do post")
    private Long id;

    @Schema(description = "Título do post")
    private String titulo;

    @Schema(description = "Corpo de texto do post")
    private String conteudo;

    @Schema(description = "Nome do autor do post")
    private String autorNome;

    @Schema(description = "Nome da comunidade onde o post foi criado")
    private String comunidadeNome;

    @Schema(description = "Data e hora da criação")
    private LocalDateTime dataCriacao;

    @Schema(description = "Placar de votos (upvotes - downvotes)")
    private int votos;
}