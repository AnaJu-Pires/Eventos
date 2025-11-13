package br.ifsp.events.dto.comentario;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO de resposta para um Comentário")
public class ComentarioResponseDTO {

    @Schema(description = "ID único do comentário")
    private Long id;

    @Schema(description = "Corpo de texto do comentário")
    private String conteudo;

    @Schema(description = "Nome do autor do comentário")
    private String autorNome;

    @Schema(description = "ID do post ao qual o comentário pertence")
    private Long postId;

    @Schema(description = "ID do comentário pai (nulo se for um comentário principal)")
    private Long comentarioPaiId;

    @Schema(description = "Data e hora da criação")
    private LocalDateTime dataCriacao;

    @Schema(description = "Placar de votos (upvotes - downvotes)")
    private int votos;
}