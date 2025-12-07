package br.ifsp.events.service.impl;

import br.ifsp.events.dto.gamification.GamificationScoreDTO;
import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.GamificationService;
import br.ifsp.events.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private GamificationService gamificationService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User usuarioFixture;
    private UserRegisterDTO registerDTOFixture;

    @BeforeEach
    void setUp() {
        usuarioFixture = new User();
        usuarioFixture.setId(1L);
        usuarioFixture.setNome("João Silva");
        usuarioFixture.setEmail("joao@aluno.ifsp.edu.br");
        usuarioFixture.setPerfilUser(PerfilUser.ALUNO);
        usuarioFixture.setStatusUser(StatusUser.INATIVO);
        usuarioFixture.setTokenConfirmacao("token-test");

        registerDTOFixture = new UserRegisterDTO();
        registerDTOFixture.setEmail("joao@aluno.ifsp.edu.br");
        registerDTOFixture.setSenha("123");

        lenient().when(modelMapper.map(any(UserRegisterDTO.class), eq(User.class))).thenReturn(new User());
        lenient().when(modelMapper.map(any(User.class), eq(UserResponseDTO.class))).thenReturn(new UserResponseDTO());
    }

    @Test
    void confirmarUsuario_comTokenValido_ativaUsuarioEInicializaGamificacao() {
        usuarioFixture.setStatusUser(StatusUser.INATIVO);
        usuarioFixture.setDataExpiracaoTokenConfirmacao(LocalDateTime.now().plusHours(1));

        when(userRepository.findByTokenConfirmacao("token-test")).thenReturn(Optional.of(usuarioFixture));
        when(userRepository.save(any(User.class))).thenReturn(usuarioFixture);

        userService.confirmUser("token-test");

        verify(userRepository, times(1)).save(any(User.class));

        verify(gamificationService, times(1)).inicializarPerfil(1L);
    }

    @Test
    void getMyInfo_buscaDadosDoMicroservico() {

        when(authentication.getPrincipal()).thenReturn(usuarioFixture);
        
        UserResponseDTO dtoBase = new UserResponseDTO();
        when(modelMapper.map(usuarioFixture, UserResponseDTO.class)).thenReturn(dtoBase);

        GamificationScoreDTO scoreDTO = new GamificationScoreDTO(1L, 500L, 500L, "PRATA", "NENHUM");
        when(gamificationService.buscarPontuacao(1L)).thenReturn(scoreDTO);

        UserResponseDTO result = userService.getMyInfo(authentication);

        assertEquals(500L, result.getPontosSaldo());
        assertEquals("PRATA", result.getNivel());
    }
}