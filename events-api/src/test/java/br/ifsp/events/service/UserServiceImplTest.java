package br.ifsp.events.service;

import br.ifsp.events.dto.user.*;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    private ModelMapper modelMapper;
    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterDTO registerDTO;
    private User user;

    @BeforeEach
    void setUp() {
        registerDTO = new UserRegisterDTO("Test User", "test@aluno.ifsp.edu.br", "password123");
        
        user = new User();
        user.setId(1L);
        user.setNome("Test User");
        user.setEmail("test@aluno.ifsp.edu.br");
        user.setSenha("hashedpassword");
        user.setPerfilUser(PerfilUser.ALUNO);
    }

    @Test
    @DisplayName("Deve registrar um novo usuário")
    void registerUserOk() {
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
        when(modelMapper.map(any(UserRegisterDTO.class), eq(User.class))).thenReturn(user);
        when(passwordEncoder.encode(registerDTO.getSenha())).thenReturn("hashedpassword");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.registerUser(registerDTO);

        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(emailService, times(1)).sendConfirmationEmail(anyString(), anyString(), anyString());
        assertEquals(StatusUser.INATIVO, userCaptor.getValue().getStatusUser());
    }

    @Test
    @DisplayName("registerUser deve lançar exceção se o e-mail já existir")
    void registerUserEmailJaExiste() {
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> {
            userService.registerUser(registerDTO);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve confirmar o usuário")
    void confirmUserOk() {
        String token = "token-valido";
        user.setStatusUser(StatusUser.INATIVO);
        user.setDataExpiracaoTokenConfirmacao(LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByTokenConfirmacao(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserResponseDTO.class)).thenReturn(new UserResponseDTO());
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.confirmUser(token);

        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals(StatusUser.ATIVO, userCaptor.getValue().getStatusUser());
    }
    
    @Test
    @DisplayName("confirmUser deve lançar exceção se o token estiver expirado")
    void confirmUserTokenExpirado() {
        String token = "token-expirado";
        user.setStatusUser(StatusUser.INATIVO);
        user.setDataExpiracaoTokenConfirmacao(LocalDateTime.now().minusHours(1));
        
        when(userRepository.findByTokenConfirmacao(token)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> {
            userService.confirmUser(token);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve logar com sucesso ")
    void loginOk() {
        UserLoginDTO loginDTO = new UserLoginDTO(user.getEmail(), "password123");
        Authentication authMock = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        UserLoginResponseDTO response = userService.login(loginDTO);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
    }

    @Test
    @DisplayName("login deve lançar exceção se as credenciais forem inválidas")
    void loginCredenciaisInvalidas() {
        UserLoginDTO loginDTO = new UserLoginDTO(user.getEmail(), "senha-errada");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThrows(BadCredentialsException.class, () -> {
            userService.login(loginDTO);
        });
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("updateUserRole deve lançar exceção ao tentar promover Aluno para Admin")
    void updateUserRoleRegraDeNegocio() {
        UserRoleUpdateDTO roleUpdateDTO = new UserRoleUpdateDTO(PerfilUser.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user)); 

        assertThrows(BusinessRuleException.class, () -> {
            userService.updateUserRole(1L, roleUpdateDTO);
        });
        verify(userRepository, never()).save(any());
    }
}