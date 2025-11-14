package br.ifsp.events.service.impl;

import br.ifsp.events.config.GamificationConstants;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link GamificationServiceImpl}.
 * Cobre registro de ações, verificação de permissões e atualização de ranks.
 */
@ExtendWith(MockitoExtension.class)
class GamificationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GamificationServiceImpl gamificationService;

    private User usuarioFixture;

    @BeforeEach
    void setUp() {
        // Fixture: User
        usuarioFixture = new User();
        usuarioFixture.setId(1L);
        usuarioFixture.setNome("João Gamer");
        usuarioFixture.setEmail("joao@test.com");
        usuarioFixture.setPontosSaldo(100L);
        usuarioFixture.setPontosRecorde(150L);
        usuarioFixture.setNivel(NivelEngajamento.BRONZE);
        usuarioFixture.setRank(RankEngajamento.NENHUM);
    }

    @Test
    void registrarAcao_criarPost_adicionaPontos() {
        // Arrange
        long pontosInicial = usuarioFixture.getPontosSaldo();
        long pontosEsperados = GamificationConstants.PONTOS_CRIAR_POST;

        when(userRepository.save(any(User.class))).thenReturn(usuarioFixture);

        // Act
        gamificationService.registrarAcao(usuarioFixture, TipoAcaoGamificacao.CRIAR_POST);

        // Assert
        assertEquals(pontosInicial + pontosEsperados, usuarioFixture.getPontosSaldo());
        verify(userRepository, times(1)).save(usuarioFixture);
    }

    @Test
    void registrarAcao_criarComunidade_adicionaPontos() {
        // Arrange
        long pontosInicial = usuarioFixture.getPontosSaldo();
        long pontosEsperados = GamificationConstants.PONTOS_CRIAR_COMUNIDADE;

        when(userRepository.save(any(User.class))).thenReturn(usuarioFixture);

        // Act
        gamificationService.registrarAcao(usuarioFixture, TipoAcaoGamificacao.CRIAR_COMUNIDADE);

        // Assert
        assertEquals(pontosInicial + pontosEsperados, usuarioFixture.getPontosSaldo());
        verify(userRepository, times(1)).save(usuarioFixture);
    }

    @Test
    void registrarAcao_atualizaPontosRecorde() {
        // Arrange
        usuarioFixture.setPontosSaldo(200L);
        usuarioFixture.setPontosRecorde(150L);

        when(userRepository.save(any(User.class))).thenReturn(usuarioFixture);

        // Act
        gamificationService.registrarAcao(usuarioFixture, TipoAcaoGamificacao.VOTAR);

        // Assert
        assertTrue(usuarioFixture.getPontosSaldo() > usuarioFixture.getPontosRecorde() || 
                   usuarioFixture.getPontosSaldo() == usuarioFixture.getPontosRecorde());
        verify(userRepository, times(1)).save(usuarioFixture);
    }

    @Test
    void checarPermissao_criarComunidade_usuarioNaoGold_lancaException() {
        // Arrange
        usuarioFixture.setNivel(NivelEngajamento.BRONZE);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            gamificationService.checarPermissao(usuarioFixture, TipoAcaoGamificacao.CRIAR_COMUNIDADE);
        });
    }

    @Test
    void checarPermissao_criarComunidade_usuarioGold_sucesso() {
        // Arrange
        usuarioFixture.setNivel(NivelEngajamento.GOLD);

        // Act & Assert
        assertDoesNotThrow(() -> {
            gamificationService.checarPermissao(usuarioFixture, TipoAcaoGamificacao.CRIAR_COMUNIDADE);
        });
    }

    @Test
    void checarPermissao_pontosSuficientes_sucesso() {
        // Arrange
        usuarioFixture.setPontosSaldo(50L);

        // Act & Assert
        assertDoesNotThrow(() -> {
            gamificationService.checarPermissao(usuarioFixture, TipoAcaoGamificacao.VOTAR);
        });
    }

    @Test
    void checarPermissao_pontosInsuficientes_lancaException() {
        // Arrange
        usuarioFixture.setPontosSaldo(0L);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            gamificationService.checarPermissao(usuarioFixture, TipoAcaoGamificacao.CRIAR_COMUNIDADE);
        });
    }

    @Test
    void atualizarRanks_removeRanksAntigos() {
        // Arrange
        User userPlatina = new User();
        userPlatina.setId(1L);
        userPlatina.setRank(RankEngajamento.PLATINA);

        User userDiamante = new User();
        userDiamante.setId(2L);
        userDiamante.setRank(RankEngajamento.DIAMANTE);

        when(userRepository.findAllByRankIn(any()))
            .thenReturn(Arrays.asList(userPlatina, userDiamante));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        gamificationService.atualizarRanks();

        // Assert
        verify(userRepository, times(2)).save(any(User.class));
        assertEquals(RankEngajamento.NENHUM, userPlatina.getRank());
        assertEquals(RankEngajamento.NENHUM, userDiamante.getRank());
    }

    @Test
    void registrarAcao_criarComentario_adicionaPontos() {
        // Arrange
        long pontosInicial = usuarioFixture.getPontosSaldo();
        long pontosEsperados = GamificationConstants.PONTOS_CRIAR_COMENTARIO;

        when(userRepository.save(any(User.class))).thenReturn(usuarioFixture);

        // Act
        gamificationService.registrarAcao(usuarioFixture, TipoAcaoGamificacao.CRIAR_COMENTARIO);

        // Assert
        assertEquals(pontosInicial + pontosEsperados, usuarioFixture.getPontosSaldo());
        verify(userRepository, times(1)).save(usuarioFixture);
    }
}
