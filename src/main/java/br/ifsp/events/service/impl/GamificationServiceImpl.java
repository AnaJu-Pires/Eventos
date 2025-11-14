package br.ifsp.events.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.config.GamificationConstants;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.model.NivelEngajamento;
import br.ifsp.events.model.RankEngajamento;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.GamificationService;

@Service
public class GamificationServiceImpl implements GamificationService {

    private final UserRepository userRepository;

    private static final Map<TipoAcaoGamificacao, Long> PONTOS_MAP = Stream.of(new Object[][] {
        { TipoAcaoGamificacao.CRIAR_POST, GamificationConstants.PONTOS_CRIAR_POST },
        { TipoAcaoGamificacao.CRIAR_COMUNIDADE, GamificationConstants.PONTOS_CRIAR_COMUNIDADE },
        { TipoAcaoGamificacao.CRIAR_COMENTARIO, GamificationConstants.PONTOS_CRIAR_COMENTARIO },
        { TipoAcaoGamificacao.VOTAR, GamificationConstants.PONTOS_VOTAR },
        { TipoAcaoGamificacao.RECEBER_UPVOTE, GamificationConstants.PONTOS_RECEBER_UPVOTE },
        { TipoAcaoGamificacao.RECEBER_DOWNVOTE, GamificationConstants.PONTOS_RECEBER_DOWNVOTE },
        { TipoAcaoGamificacao.RECEBER_COMENTARIO, GamificationConstants.PONTOS_RECEBER_COMENTARIO },
        { TipoAcaoGamificacao.RECEBER_POST_EM_COMUNIDADE, GamificationConstants.PONTOS_RECEBER_POST_EM_COMUNIDADE }
    }).collect(Collectors.toMap(data -> (TipoAcaoGamificacao) data[0], data -> (Long) data[1]));

    public GamificationServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void registrarAcao(User usuario, TipoAcaoGamificacao tipo) {
        long pontos = PONTOS_MAP.get(tipo);
        
        usuario.setPontosSaldo(usuario.getPontosSaldo() + pontos);

        if (usuario.getPontosSaldo() > usuario.getPontosRecorde()) {
            usuario.setPontosRecorde(usuario.getPontosSaldo());
        }
        
        atualizarNivel(usuario);
        
        userRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public void checarPermissao(User usuario, TipoAcaoGamificacao tipo) {
        if (tipo == TipoAcaoGamificacao.CRIAR_COMUNIDADE) {
            if (usuario.getNivel() != NivelEngajamento.GOLD) {
                throw new BusinessRuleException("Apenas usuários Nível GOLD podem criar comunidades."); 
            }
        }

        long custo = PONTOS_MAP.get(tipo);
        if (usuario.getPontosSaldo() + custo < 0) {
            throw new BusinessRuleException("Pontos insuficientes. Você precisa de " + (custo * -1) + " pontos, mas só tem " + usuario.getPontosSaldo() + ".");
        }
    }

    @Override
    @Transactional
    public void atualizarRanks() {
        List<User> usuariosComRank = userRepository.findAllByRankIn(
            List.of(RankEngajamento.PLATINA, RankEngajamento.DIAMANTE)
        );
        
        for (User user : usuariosComRank) {
            user.setRank(RankEngajamento.NENHUM);
            userRepository.save(user);
        }

        List<User> top100 = userRepository.findTopNByPontosSaldo(
            GamificationConstants.RANK_PLATINA_TOP_N
        );

        int rankAtual = 1;
        for (User user : top100) {
            if (rankAtual <= GamificationConstants.RANK_DIAMANTE_TOP_N) {
                user.setRank(RankEngajamento.DIAMANTE);
            } else {
                user.setRank(RankEngajamento.PLATINA);
            }
            userRepository.save(user);
            rankAtual++;
        }
    }

    private void atualizarNivel(User usuario) {
        if (usuario.getPontosRecorde() >= GamificationConstants.REQUISITO_NIVEL_GOLD) {
            usuario.setNivel(NivelEngajamento.GOLD);
        } else if (usuario.getPontosRecorde() >= GamificationConstants.REQUISITO_NIVEL_PRATA) {
            usuario.setNivel(NivelEngajamento.PRATA);
        } else {
            usuario.setNivel(NivelEngajamento.BRONZE);
        }
    }
}