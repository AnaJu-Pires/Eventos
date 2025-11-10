package br.ifsp.events.service;

import java.util.List;

import br.ifsp.events.dto.partida.PartidaResponseDTO;
import br.ifsp.events.model.FormatoEventoModalidade;

public interface PartidaService {

    List<PartidaResponseDTO> listByEvento(Long eventoId);

    /**
     * Gera a chave de confrontos (por exemplo: MATA_MATA ou PONTOS_CORRIDOS) 
     * para o evento informado.
     * Implementação deve validar regras de negócio (ex.: inscrições fechadas) e
     * persistir as partidas iniciais.
     *
     * @param eventoId id do evento
     * @param formato  formato a ser gerado
     */
    // ADICIONE ESTE MÉTODO:
    void gerarChaveParaEvento(Long eventoId, FormatoEventoModalidade formato);
}