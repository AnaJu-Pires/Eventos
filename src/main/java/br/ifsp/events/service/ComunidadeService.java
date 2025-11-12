package br.ifsp.events.service;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.dto.comunidade.ComunidadeResponseDTO;

public interface ComunidadeService {
    /** 
     * @param dto Os dados da nova comunidade
     * @return A comunidade criada
     */
    ComunidadeResponseDTO create(ComunidadeCreateDTO dto);
}
