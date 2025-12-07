package br.ifsp.events.dto.modalidade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para criar ou atualizar completamente uma Modalidade")
public class ModalidadeRequestDTO {

    @NotBlank(message = "O nome da modalidade é obrigatório")
    @Size(min = 3, max = 100)
    @Schema(description = "Nome único da modalidade", example = "Voleibol de Praia")
    private String nome;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.")
    @Schema(description = "Breve descrição da modalidade e suas regras principais", example = "Jogo disputado na areia por duas duplas.")
    private String descricao;
}