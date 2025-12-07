package br.ifsp.events.dto.comunidade;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO de resposta para uma Comunidade")
public class ComunidadeResponseDTO {

    @Schema(description = "ID único da comunidade", example = "1")
    private Long id;

    @Schema(description = "Nome único da comunidade", example = "Comunidade de Teste")
    private String nome;

    @Schema(description = "Descrição da comunidade", example = "Esta é uma comunidade de teste.")
    private String descricao;

    @Schema(description = "Nome do usuário que criou a comunidade", example = "Maria Oliveira")
    private String criadorNome;

    @Schema(description = "Data e hora da criação", example = "2023-03-15T10:00:00")
    private LocalDateTime dataCriacao;
}
