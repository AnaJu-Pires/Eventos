package br.ifsp.events.dto.gamification;

import br.ifsp.events.model.TipoAcaoGamificacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GamificationAcaoDTO {
    private Long userId;
    private TipoAcaoGamificacao tipoAcao;
}