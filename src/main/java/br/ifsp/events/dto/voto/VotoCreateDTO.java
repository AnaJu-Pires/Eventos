package br.ifsp.events.dto.voto;

import br.ifsp.events.model.TipoVoto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO para registrar um novo voto em um post ou comentário")
public class VotoCreateDTO {

    @Schema(description = "O ID do Post que está sendo votado. Nulo se for um voto em comentário.")
    private Long postId;

    @Schema(description = "O ID do Comentário que está sendo votado. Nulo se for um voto em post.")
    private Long comentarioId;

    @NotNull(message = "O tipo de voto (UPVOTE ou DOWNVOTE) é obrigatório")
    @Schema(description = "O tipo de voto", example = "UPVOTE")
    private TipoVoto tipoVoto;
}