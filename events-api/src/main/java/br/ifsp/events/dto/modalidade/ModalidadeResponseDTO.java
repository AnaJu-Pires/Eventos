package br.ifsp.events.dto.modalidade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para representar os dados de uma Modalidade na resposta da API")
public class ModalidadeResponseDTO {

    @Schema(description = "ID único da modalidade", example = "1")
    private Long id;

    @Schema(description = "Nome único da modalidade", example = "Voleibol de Praia")
    private String nome;

    @Schema(description = "Breve descrição da modalidade", example = "Jogo disputado na areia por duas duplas.")
    private String descricao;
}