package br.ifsp.events.dto.event;

import java.time.LocalDate;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para requisição de criação de um Evento")
public class EventRequestDTO {

    @NotBlank(message = "O nome do evento é obrigatório")
    @Schema(description = "Nome do evento", example = "Campeonato de Futebol")
    private String nome;

    @Schema(description = "Descrição do evento", example = "Campeonato de Futebol 2023")
    private String descricao;

    @NotNull(message = "A data de início é obrigatória")
    @FutureOrPresent(message = "A data de início não pode ser no passado")
    @Schema(description = "Data de início do evento", example = "2023-01-01")
    private LocalDate dataInicio;

    @NotNull(message = "A data de fim é obrigatória")
    @FutureOrPresent(message = "A data de fim não pode ser no passado")
    @Schema(description = "Data de fim do evento", example = "2023-12-31")
    private LocalDate dataFim;

    @NotNull(message = "O evento deve ter pelo menos uma modalidade")
    @Schema(description = "Modalidades do evento")
    private Set<EventoModalidadeRequestDTO> modalidades;
}
