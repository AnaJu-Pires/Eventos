package br.ifsp.events.dto.event;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventPatchDTO {
    private Optional<String> nome = Optional.empty();
    private Optional<String> descricao = Optional.empty();
    private Optional<LocalDate> dataInicio = Optional.empty();
    private Optional<LocalDate> dataFim = Optional.empty();
    private Optional<Set<Long>> modalidadesIds = Optional.empty();
}