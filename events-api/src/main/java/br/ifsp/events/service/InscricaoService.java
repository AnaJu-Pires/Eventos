package br.ifsp.events.service;

import java.util.List;

import br.ifsp.events.dto.inscricao.InscricaoResponseDTO;

public interface InscricaoService {
    List<InscricaoResponseDTO> listPendentesByEvento(Long eventoId);

    InscricaoResponseDTO aprovarInscricao(Long inscricaoId);

    InscricaoResponseDTO rejeitarInscricao(Long inscricaoId);
}
