package br.ifsp.events.service;

import br.ifsp.events.dto.convite.ConviteResponseDTO;
import org.springframework.security.core.Authentication;
import java.util.List;

public interface ConviteService {

    /**
     * @param auth
     * @return 
     */
    List<ConviteResponseDTO> listarMeusConvites(Authentication auth);

    /**
     * @param conviteId
     * @param auth
     */
    void aceitarConvite(Long conviteId, Authentication auth);

    /**
     * @param conviteId
     * @param auth
     */
    void recusarConvite(Long conviteId, Authentication auth);
}