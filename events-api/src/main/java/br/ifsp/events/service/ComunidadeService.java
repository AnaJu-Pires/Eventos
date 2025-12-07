package br.ifsp.events.service;

import java.util.List;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.dto.comunidade.ComunidadeResponseDTO;

public interface ComunidadeService {
    
    ComunidadeResponseDTO create(ComunidadeCreateDTO dto);

    List<ComunidadeResponseDTO> listAll();

    ComunidadeResponseDTO findById(Long id);

    void deleteComunidade(Long comunidadeId);
}
