package br.ifsp.events.dto.comentario;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@Schema(description = "DTO de resposta para um Comentário")
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioResponseDTO {

    @Schema(description = "ID único do comentário", example = "1")
    private Long id;

    @Schema(description = "Corpo de texto do comentário", example = "Muito bom o evento!")
    private String conteudo;

    @Schema(description = "Nome do autor do comentário", example = "João Silva")
    private String autorNome;

    @Schema(description = "ID do post ao qual o comentário pertence", example = "1")
    private Long postId;

    @Schema(description = "ID do comentário pai (nulo se for um comentário principal)", example = "1")
    private Long comentarioPaiId;

    @Schema(description = "Data e hora da criação", example = "2023-03-15T10:00:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Placar de votos (upvotes - downvotes)", example = "5")
    private int votos;
}