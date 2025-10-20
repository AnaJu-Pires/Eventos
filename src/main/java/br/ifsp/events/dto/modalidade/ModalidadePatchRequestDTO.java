package br.ifsp.events.dto.modalidade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO para atualização parcial de uma Modalidade")
public class ModalidadePatchRequestDTO {

    @Size(min = 3, max = 100)
    @Schema(description = "Novo nome único da modalidade (opcional)", example = "Futsal Feminino")
    private String nome;

    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.")
    @Schema(description = "Nova descrição da modalidade (opcional)", example = "Exclusivo para equipes femininas.")
    private String descricao;
}