package br.ifsp.events.dto.user;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInteresseUpdateDTO {

    @NotEmpty(message = "A lista de interesses não pode estar vazia")
    private List<Long> modalidadeIds;
}
