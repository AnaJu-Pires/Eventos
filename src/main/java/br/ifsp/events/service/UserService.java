package br.ifsp.events.service;

import br.ifsp.events.dto.user.UserLoginDTO;
import br.ifsp.events.dto.user.UserLoginResponseDTO;
import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.dto.user.UserRoleUpdateDTO;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.Authentication; 

public interface UserService extends UserDetailsService {
    
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

    UserLoginResponseDTO login(UserLoginDTO loginDTO);

    UserResponseDTO getMyInfo(Authentication authentication);

    /**
     * @param userId
     * @param roleUpdateDTO
     * @return
     */
    UserResponseDTO updateUserRole(Long userId, UserRoleUpdateDTO roleUpdateDTO);
}