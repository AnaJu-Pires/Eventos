package com.ifsp.gamificacao.infrastructure.persistence;

import com.ifsp.gamificacao.core.domain.model.ScoreProfile;
import com.ifsp.gamificacao.core.ports.ScoreRepositoryPort;
import com.ifsp.gamificacao.infrastructure.persistence.entity.ScoreEntity;
import com.ifsp.gamificacao.infrastructure.persistence.repository.ScoreJpaRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScoreRepositoryAdapter implements ScoreRepositoryPort {

    private final ScoreJpaRepository jpaRepository;
    private final ModelMapper mapper;

    @Override
    public Optional<ScoreProfile> buscarPorUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .map(entity -> mapper.map(entity, ScoreProfile.class));
    }

    @Override
    public ScoreProfile salvar(ScoreProfile scoreProfile) {
        return salvarEntidade(scoreProfile);
    }

    @Override
    public List<ScoreProfile> buscarTopUsers(int limite) {
        return jpaRepository.findByOrderByPontosSaldoDesc(PageRequest.of(0, limite))
                .stream()
                .map(entity -> mapper.map(entity, ScoreProfile.class))
                .collect(Collectors.toList());
    }

    @Override
    public void salvarTodos(List<ScoreProfile> profiles) {
        for (ScoreProfile profile : profiles) {
            salvarEntidade(profile);
        }
    }

    private ScoreProfile salvarEntidade(ScoreProfile scoreProfile) {
        ScoreEntity entity = jpaRepository.findByUserId(scoreProfile.getUserId())
                .orElse(new ScoreEntity());
        
        entity.setUserId(scoreProfile.getUserId());
        entity.setPontosSaldo(scoreProfile.getPontosSaldo());
        entity.setPontosRecorde(scoreProfile.getPontosRecorde());
        entity.setNivel(scoreProfile.getNivel());
        entity.setRank(scoreProfile.getRank());

        ScoreEntity salvo = jpaRepository.save(entity);
        return mapper.map(salvo, ScoreProfile.class);
    }
}