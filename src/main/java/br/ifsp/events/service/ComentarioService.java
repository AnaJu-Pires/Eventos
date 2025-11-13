package br.ifsp.events.service;

import br.ifsp.events.dto.comentario.ComentarioCreateDTO;
import br.ifsp.events.dto.comentario.ComentarioResponseDTO;

public interface ComentarioService {

    ComentarioResponseDTO create(ComentarioCreateDTO dto, Long postId);
}