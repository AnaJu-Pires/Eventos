package br.ifsp.events.service;

import br.ifsp.events.dto.post.PostCreateDTO;
import br.ifsp.events.dto.post.PostResponseDTO;

public interface PostService {
    /**
     * 
     * @param dto Os dados do novo post.
     * @param comunidadeId O ID da comunidade onde o post será criado.
     * @return O post criado.
     */ 
    PostResponseDTO create(PostCreateDTO dto, Long comunidadeId);
}
