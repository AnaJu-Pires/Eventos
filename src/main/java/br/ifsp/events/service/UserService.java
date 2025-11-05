package br.ifsp.events.service;

import java.io.Writer;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import br.ifsp.events.dto.user.UserInteresseResponseDTO;
import br.ifsp.events.dto.user.UserInteresseUpdateDTO;
import br.ifsp.events.dto.user.UserLoginDTO;
import br.ifsp.events.dto.user.UserLoginResponseDTO;
import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.dto.user.UserRoleUpdateDTO; 

public interface UserService extends UserDetailsService {
    
    /**
     * @param registerDTO 
     */
    void registerUser(UserRegisterDTO registerDTO);
    
    /**
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

    List<UserResponseDTO> listarPerfisUsuarios();

    UserInteresseResponseDTO getUserInteresses(Long userId);

    UserInteresseResponseDTO updateUserInteresses(Long userId, UserInteresseUpdateDTO interessesDTO);

    void writeUsersCsv(Writer writer);
}