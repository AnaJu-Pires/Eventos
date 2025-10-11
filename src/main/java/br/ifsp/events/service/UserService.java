package br.ifsp.events.service;

import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;

public interface UserService {
    
    /**
     * @param registerDTO 
     */
    void registerUser(UserRegisterDTO registerDTO);
    
    /**
     * ativa o usuario se o token for valido e nao tiver vencido
     * @param token
     * @return
     */
    UserResponseDTO confirmUser(String token);
}