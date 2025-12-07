package br.ifsp.events.dto.inscricao;

import java.util.Set;

import br.ifsp.events.model.StatusInscricao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO de resposta para uma Inscrição")
public class InscricaoResponseDTO {
    private Long id;
    private String nomeTime;
    private String nomeEvento;
    private String nomeModalidade;
    private StatusInscricao statusInscricao;
    private String nomeCapitao;
    private Set<String> nomesJogadores;
}