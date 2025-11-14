package br.ifsp.events.dto.voto;

import br.ifsp.events.model.TipoVoto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Resposta do servidor após um voto ser processado")
public class VotoResponseDTO {

    @Schema(description = "O novo placar de votos do conteúdo (upvotes - downvotes)")
    private int novoPlacar;

    @Schema(description = "O voto atual do usuário naquele conteúdo (UPVOTE, DOWNVOTE, or null se o voto foi removido)")
    private TipoVoto seuVoto; 
}