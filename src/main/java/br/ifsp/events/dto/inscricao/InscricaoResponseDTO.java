package br.ifsp.events.dto.inscricao;

import java.util.Set;

import br.ifsp.events.model.StatusInscricao;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InscricaoResponseDTO {
    private Long id;
    private String nomeTime;
    private String nomeEvento;
    private String nomeModalidade;
    private StatusInscricao statusInscricao;
    private String nomeCapitao;
    private Set<String> nomesJogadores;
}