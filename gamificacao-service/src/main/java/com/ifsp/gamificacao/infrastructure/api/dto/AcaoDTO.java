package com.ifsp.gamificacao.infrastructure.api.dto;

import com.ifsp.gamificacao.core.domain.model.TipoAcaoGamificacao;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AcaoDTO {
    @NotNull(message = "O ID do usuário é obrigatório")
    private Long userId;
    
    @NotNull(message = "O tipo da ação é obrigatório")
    private TipoAcaoGamificacao tipoAcao;
}