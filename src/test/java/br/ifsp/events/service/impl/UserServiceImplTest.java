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
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private ModelMapper modelMapper;

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
        usuarioFixture.setEmail("joao@aluno.ifsp.edu.br");
        usuarioFixture.setSenha("senha_hasheada");
        usuarioFixture.setPerfilUser(PerfilUser.ALUNO);
        usuarioFixture.setStatusUser(StatusUser.INATIVO);
        usuarioFixture.setTokenConfirmacao("token-test");
        usuarioFixture.setPontosSaldo(0L);
        usuarioFixture.setNivel(NivelEngajamento.BRONZE);
        usuarioFixture.setRank(RankEngajamento.NENHUM);

        // Fixture: RegisterDTO
        registerDTOFixture = new UserRegisterDTO();
        registerDTOFixture.setNome("João Silva");
        registerDTOFixture.setEmail("joao@aluno.ifsp.edu.br");
        registerDTOFixture.setSenha("senha123");
    }

    @Test
    void registrarUsuario_comDadosValidos_sucesso() {
        // Arrange
    // Use o DTO do setUp (com email "joao@aluno.ifsp.edu.br")
    when(userRepository.existsByEmail(registerDTOFixture.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(registerDTOFixture.getSenha())).thenReturn("senha_hasheada");

    // Configure o mock 'save' para retornar o usuário que recebeu
    // Isso é mais limpo para testes de criação
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
        User userSalvo = invocation.getArgument(0);
        userSalvo.setId(1L); // Simula o ID sendo gerado pelo banco
        return userSalvo;
    });

    // Crie um ArgumentCaptor para a classe User
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // Act
    // Não precisamos mais do assertDoesNotThrow,
    // pois a ausência de exceção já é o comportamento esperado.
    userService.registerUser(registerDTOFixture);

    // Assert
    // Verifique se o 'save' foi chamado e capture o argumento
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(emailService, times(1)).sendConfirmationEmail(anyString(), anyString(), anyString());

    // Capture o valor e faça asserções detalhadas (Princípio do "Right")
    User userSalvo = userCaptor.getValue();
    assertEquals("João Silva", userSalvo.getNome());
    assertEquals("joao@aluno.ifsp.edu.br", userSalvo.getEmail());
    assertEquals("senha_hasheada", userSalvo.getSenha());
    assertEquals(PerfilUser.ALUNO, userSalvo.getPerfilUser()); // Verifica a lógica de `determinePerfilFromEmail`
    assertEquals(StatusUser.INATIVO, userSalvo.getStatusUser()); // Garante que o usuário está inativo
    assertNotNull(userSalvo.getTokenConfirmacao()); // Garante que um token foi gerado
    assertNotNull(userSalvo.getDataExpiracaoTokenConfirmacao());
    }

    @Test
    void registrarUsuario_comDominioDeEmailInvalido_lancaBusinessRuleException() {
        // Arrange
        UserRegisterDTO dtoEmailInvalido = new UserRegisterDTO();
        dtoEmailInvalido.setNome("Usuário Inválido");
        dtoEmailInvalido.setEmail("usuario@dominioestranho.com"); // Email inválido
        dtoEmailInvalido.setSenha("senha123");

        when(userRepository.existsByEmail(dtoEmailInvalido.getEmail())).thenReturn(false);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            userService.registerUser(dtoEmailInvalido);
        });
        
        // Assert (Mensagem de exceção)
        assertEquals("Domínio de email não reconhecido.", exception.getMessage());

        // Garante que nada foi salvo ou enviado
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registrarUsuario_comEmailDuplicado_lancaBusinessRuleException() {
        // Arrange
        when(userRepository.existsByEmail("joao@aluno.ifsp.edu.br")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            userService.registerUser(registerDTOFixture);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void confirmarUsuario_comTokenValido_ativaUsuario() {
        // Arrange
        usuarioFixture.setStatusUser(StatusUser.INATIVO); // Garante o estado inicial
        usuarioFixture.setDataExpiracaoTokenConfirmacao(LocalDateTime.now().plusHours(1)); // Garante que não está expirado

        when(userRepository.findByTokenConfirmacao("token-test")).thenReturn(Optional.of(usuarioFixture));
        
        // Use o ArgumentCaptor para verificar o que é salvo
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(usuarioFixture); // Captura na chamada do save

        // Mock do ModelMapper
        when(modelMapper.map(any(User.class), eq(UserResponseDTO.class)))
            .thenReturn(new UserResponseDTO()); // Retorne um DTO qualquer, só para evitar NPE

        // Act
        UserResponseDTO result = userService.confirmUser("token-test");

        // Assert
        assertNotNull(result);

        // Verifique o estado do usuário que foi para o 'save'
        User userSalvo = userCaptor.getValue();
        assertEquals(StatusUser.ATIVO, userSalvo.getStatusUser());
        assertNull(userSalvo.getTokenConfirmacao()); // Token deve ser limpo
        assertNull(userSalvo.getDataExpiracaoTokenConfirmacao()); // Data de expiração deve ser limpa
        
        verify(userRepository, times(1)).save(any(User.class));
        verify(modelMapper, times(1)).map(any(User.class), eq(UserResponseDTO.class));
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
        loginDTO.setEmail("joao@aluno.ifsp.edu.br");
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
        when(userRepository.findByEmail("joao@aluno.ifsp.edu.br")).thenReturn(Optional.of(usuarioFixture));

        // Act
        var result = userService.loadUserByUsername("joao@aluno.ifsp.edu.br");

        // Assert
        assertNotNull(result);
        assertEquals("joao@aluno.ifsp.edu.br", result.getUsername());
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
        
        // Mock do ModelMapper
        UserResponseDTO dtoEsperado = new UserResponseDTO();
        dtoEsperado.setId(usuarioFixture.getId());
        dtoEsperado.setEmail(usuarioFixture.getEmail());
        
        when(modelMapper.map(usuarioFixture, UserResponseDTO.class))
            .thenReturn(dtoEsperado);

        // Act
        UserResponseDTO result = userService.getMyInfo(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(usuarioFixture.getEmail(), result.getEmail()); // Verificação mais forte
        verify(modelMapper, times(1)).map(usuarioFixture, UserResponseDTO.class);
    }

    @Test
    void confirmarUsuario_comTokenExpirado_lancaBusinessRuleException() {
        // Arrange
        // Configure a fixture com um token que expirou ontem
        usuarioFixture.setStatusUser(StatusUser.INATIVO);
        usuarioFixture.setTokenConfirmacao("token-expirado");
        usuarioFixture.setDataExpiracaoTokenConfirmacao(LocalDateTime.now().minusDays(1)); // Expirou ontem

        when(userRepository.findByTokenConfirmacao("token-expirado"))
            .thenReturn(Optional.of(usuarioFixture));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            userService.confirmUser("token-expirado");
        });

        // Assert (Mensagem de exceção)
        assertEquals("Token de confirmação expirado. Por favor, registre-se novamente.", exception.getMessage());
        
        // Garante que o usuário não foi salvo (status não mudou)
        verify(userRepository, never()).save(any(User.class));
    }
}
