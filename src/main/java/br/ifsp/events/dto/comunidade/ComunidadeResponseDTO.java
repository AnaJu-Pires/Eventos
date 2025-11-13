package br.ifsp.events.dto.comunidade;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO de resposta para uma Comunidade")
public class ComunidadeResponseDTO {

    @Schema(description = "ID único da comunidade")
    private Long id;

    @Schema(description = "Nome único da comunidade")
    private String nome;

    @Schema(description = "Descrição da comunidade")
    private String descricao;

    @Schema(description = "Nome do usuário que criou a comunidade")
    private String criadorNome;

    @Schema(description = "Data e hora da criação")
    private LocalDateTime dataCriacao;
}
