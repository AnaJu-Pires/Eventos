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
import static org.mockito.ArgumentMatchers.anyInt;
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
        usuarioFixture.setEmail("joao@aluno.ifsp.edu.br");
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
        assertEquals(usuarioFixture.getPontosSaldo(), usuarioFixture.getPontosRecorde());
        verify(userRepository, times(1)).save(usuarioFixture);
    }

    @Test
    void checarPermissao_criarComunidade_usuarioNaoGold_lancaException() {
        // Arrange
        usuarioFixture.setNivel(NivelEngajamento.BRONZE);
        usuarioFixture.setPontosSaldo(1500L);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            gamificationService.checarPermissao(usuarioFixture, TipoAcaoGamificacao.CRIAR_COMUNIDADE);
        });
    }

    @Test
    void checarPermissao_criarComunidade_usuarioGold_sucesso() {
        // Arrange
        usuarioFixture.setNivel(NivelEngajamento.GOLD);
        usuarioFixture.setPontosSaldo(1500L); 

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
        when(userRepository.findTopNByPontosSaldo(100)).thenReturn(List.of());


        // Act
        gamificationService.atualizarRanks();

        // Assert
        verify(userRepository, times(1)).save(userPlatina);
        verify(userRepository, times(1)).save(userDiamante);
        
        assertEquals(RankEngajamento.NENHUM, userPlatina.getRank());
        assertEquals(RankEngajamento.NENHUM, userDiamante.getRank());
    }
    
    @Test
    void atualizarRanks_defineNovosRanks() {
        // Arrange
        User userTop1 = new User(); userTop1.setId(1L); userTop1.setPontosSaldo(1000L); userTop1.setRank(RankEngajamento.NENHUM);
        User userTop2 = new User(); userTop2.setId(2L); userTop2.setPontosSaldo(900L); userTop2.setRank(RankEngajamento.NENHUM);
        User userTop3 = new User(); userTop3.setId(3L); userTop3.setPontosSaldo(800L); userTop3.setRank(RankEngajamento.NENHUM);
        
        User userAntigoPlatina = new User(); userAntigoPlatina.setId(44L); userAntigoPlatina.setPontosSaldo(50L); userAntigoPlatina.setRank(RankEngajamento.PLATINA);

        when(userRepository.findAllByRankIn(any()))
            .thenReturn(List.of(userAntigoPlatina));

        when(userRepository.findTopNByPontosSaldo(100))
            .thenReturn(Arrays.asList(userTop1, userTop2, userTop3));
            
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        gamificationService.atualizarRanks();

        // Assert
        // 1. Verifica se o rank antigo foi limpo
        assertEquals(RankEngajamento.NENHUM, userAntigoPlatina.getRank());
        
        // 2. Verifica se os novos ranks foram atribuídos
        // COMO O TOP 10 É DIAMANTE, E TEMOS APENAS 3 USUÁRIOS, TODOS SERÃO DIAMANTE
        assertEquals(RankEngajamento.DIAMANTE, userTop1.getRank());
        assertEquals(RankEngajamento.DIAMANTE, userTop2.getRank()); // Corrigido para DIAMANTE
        assertEquals(RankEngajamento.DIAMANTE, userTop3.getRank()); // Corrigido para DIAMANTE

        // 3. Verifica se o save foi chamado
        verify(userRepository, times(4)).save(any(User.class));
        verify(userRepository, times(1)).save(userAntigoPlatina);
        verify(userRepository, times(1)).save(userTop1);
        verify(userRepository, times(1)).save(userTop2);
        verify(userRepository, times(1)).save(userTop3);
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