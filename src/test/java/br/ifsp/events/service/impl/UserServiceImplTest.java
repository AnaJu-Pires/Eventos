package br.ifsp.events.service.impl;

import br.ifsp.events.dto.user.UserLoginDTO;
import br.ifsp.events.dto.user.UserLoginResponseDTO;
import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.NivelEngajamento;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.RankEngajamento;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link UserServiceImpl}.
 * Cobre registro, login, confirmação de usuário e gerenciamento de perfil.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User usuarioFixture;
    private UserRegisterDTO registerDTOFixture;

    @BeforeEach
    void setUp() {
        // Fixture: User
        usuarioFixture = new User();
        usuarioFixture.setId(1L);
        usuarioFixture.setNome("João Silva");
        usuarioFixture.setEmail("joao@test.com");
        usuarioFixture.setSenha("senha_hasheada");
        usuarioFixture.setPerfilUser(PerfilUser.USUARIO);
        usuarioFixture.setStatusUser(StatusUser.INATIVO);
        usuarioFixture.setTokenConfirmacao("token-test");
        usuarioFixture.setPontosSaldo(0L);
        usuarioFixture.setNivel(NivelEngajamento.BRONZE);
        usuarioFixture.setRank(RankEngajamento.NENHUM);

        // Fixture: RegisterDTO
        registerDTOFixture = new UserRegisterDTO();
        registerDTOFixture.setNome("João Silva");
        registerDTOFixture.setEmail("joao@test.com");
        registerDTOFixture.setSenha("senha123");
    }

    @Test
    void registrarUsuario_comDadosValidos_sucesso() {
        // Arrange
        when(userRepository.existsByEmail("joao@test.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senha_hasheada");
        when(userRepository.save(any(User.class))).thenReturn(usuarioFixture);

        // Act
        assertDoesNotThrow(() -> {
            userService.registerUser(registerDTOFixture);
        });

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendConfirmationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registrarUsuario_comEmailDuplicado_lancaBusinessRuleException() {
        // Arrange
        when(userRepository.existsByEmail("joao@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            userService.registerUser(registerDTOFixture);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void confirmarUsuario_comTokenValido_ativaUsuario() {
        // Arrange
        usuarioFixture.setStatusUser(StatusUser.INATIVO);
        when(userRepository.findByTokenConfirmacao("token-test")).thenReturn(Optional.of(usuarioFixture));
        when(userRepository.save(any(User.class))).thenReturn(usuarioFixture);

        // Act
        UserResponseDTO result = userService.confirmUser("token-test");

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void confirmarUsuario_comTokenInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(userRepository.findByTokenConfirmacao("token-invalido")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.confirmUser("token-invalido");
        });
    }

    @Test
    void confirmarUsuario_jaAtivo_lancaBusinessRuleException() {
        // Arrange
        usuarioFixture.setStatusUser(StatusUser.ATIVO);
        when(userRepository.findByTokenConfirmacao("token-test")).thenReturn(Optional.of(usuarioFixture));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            userService.confirmUser("token-test");
        });
    }

    @Test
    void login_comCredenciaisValidas_retornaToken() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(usuarioFixture);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtService.generateToken(usuarioFixture)).thenReturn("jwt-token");

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setEmail("joao@test.com");
        loginDTO.setSenha("senha123");

        // Act
        UserLoginResponseDTO result = userService.login(loginDTO);

        // Assert
        assertNotNull(result);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(usuarioFixture);
    }

    @Test
    void loadUserByUsername_comUsuarioValido_retornaUser() {
        // Arrange
        when(userRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(usuarioFixture));

        // Act
        var result = userService.loadUserByUsername("joao@test.com");

        // Assert
        assertNotNull(result);
        assertEquals("joao@test.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_comUsuarioInvalido_lancaUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail("desconhecido@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("desconhecido@test.com");
        });
    }

    @Test
    void getMyInfo_retornaInformacoesDoUsuario() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(usuarioFixture);

        // Act
        UserResponseDTO result = userService.getMyInfo(authentication);

        // Assert
        assertNotNull(result);
    }
}
