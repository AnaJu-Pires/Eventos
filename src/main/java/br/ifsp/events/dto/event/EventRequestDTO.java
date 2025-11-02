package br.ifsp.events.dto.event;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDTO {

    @NotBlank(message = "O nome do evento é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "A data de início é obrigatória")
    @FutureOrPresent(message = "A data de início não pode ser no passado")
    private LocalDate dataInicio;

    @NotNull(message = "A data de fim é obrigatória")
    @FutureOrPresent(message = "A data de fim não pode ser no passado")
    private LocalDate dataFim;

    @NotEmpty(message = "O evento deve ter pelo menos uma modalidade")
    private Set<Long> modalidadesIds;
}
