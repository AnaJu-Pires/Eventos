package br.ifsp.events.service.impl;

import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final int EXPIRATION_HOURS = 24;


    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }


    @Override
    public void registerUser(UserRegisterDTO registerDTO) {

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessRuleException("Este e-mail já está em uso.");
        }

        User user = new User();
        user.setNome(registerDTO.getNome());
        user.setEmail(registerDTO.getEmail());

        user.setSenha(passwordEncoder.encode(registerDTO.getSenha()));
        
        user.setPerfilUser(PerfilUser.ROLE_ALUNO); 
        user.setStatusUser(StatusUser.INATIVO);

        String token = UUID.randomUUID().toString();
        user.setTokenConfirmacao(token);
        user.setDataExpiracaoTokenConfirmacao(LocalDateTime.now().plusHours(EXPIRATION_HOURS));

        userRepository.save(user);
    
        emailService.sendConfirmationEmail(user.getEmail(), token);
    }

    @Override
    public UserResponseDTO confirmUser(String token) {

        User user = userRepository.findByTokenConfirmacao(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token de confirmação inválido."));

        if (user.getStatusUser() == StatusUser.ATIVO) {
            throw new BusinessRuleException("Esta conta já foi ativada.");
        }

        if (LocalDateTime.now().isAfter(user.getDataExpiracaoTokenConfirmacao())) {
            throw new BusinessRuleException("Token de confirmação expirado. Por favor, registre-se novamente.");
        }

        user.setStatusUser(StatusUser.ATIVO);
        user.setTokenConfirmacao(null);
        user.setDataExpiracaoTokenConfirmacao(null);

        User activatedUser = userRepository.save(user);
        return toResponseDTO(activatedUser);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getNome(),
            user.getEmail(),
            user.getPerfilUser()
        );
    }
}