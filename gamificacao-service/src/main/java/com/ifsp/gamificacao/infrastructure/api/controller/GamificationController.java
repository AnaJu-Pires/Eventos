package com.ifsp.gamificacao.infrastructure.api.controller;

import com.ifsp.gamificacao.core.domain.model.ScoreProfile;
import com.ifsp.gamificacao.core.usecases.ConsultarScoreUseCase;
import com.ifsp.gamificacao.core.usecases.CriarScoreProfileUseCase;
import com.ifsp.gamificacao.core.usecases.ProcessarAcaoUseCase;
import com.ifsp.gamificacao.infrastructure.api.dto.AcaoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final ProcessarAcaoUseCase processarAcaoUseCase;
    private final ConsultarScoreUseCase consultarScoreUseCase;

    @PostMapping("/actions")
    public ResponseEntity<ScoreProfile> registrarAcao(@RequestBody @Valid AcaoDTO dto) {
        ScoreProfile atualizado = processarAcaoUseCase.executar(dto.getUserId(), dto.getTipoAcao());
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping("/scores/{userId}")
    public ResponseEntity<ScoreProfile> consultarPontos(@PathVariable Long userId) {
        return consultarScoreUseCase.executar(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private final CriarScoreProfileUseCase criarScoreProfileUseCase;

    @PostMapping("/users/{userId}")
    public ResponseEntity<ScoreProfile> inicializarPerfil(@PathVariable Long userId) {
        ScoreProfile profile = criarScoreProfileUseCase.executar(userId);
        return ResponseEntity.status(201).body(profile);
    }
}