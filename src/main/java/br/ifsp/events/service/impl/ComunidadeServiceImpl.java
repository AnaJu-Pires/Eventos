package br.ifsp.events.service.impl;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.dto.comunidade.ComunidadeResponseDTO;
import br.ifsp.events.exception.DuplicateResourceException; 
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.TipoAcaoGamificacao; 
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComunidadeRepository;
import br.ifsp.events.service.ComunidadeService;
import br.ifsp.events.service.GamificationService;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComunidadeServiceImpl implements ComunidadeService{
    private final ComunidadeRepository comunidadeRepository;
    private final GamificationService gamificationService;

    public ComunidadeServiceImpl(ComunidadeRepository comunidadeRepository, GamificationService gamificationService) {
        this.comunidadeRepository = comunidadeRepository;
        this.gamificationService = gamificationService;
    }
    
    @Override
    @Transactional
    public ComunidadeResponseDTO create(ComunidadeCreateDTO dto){
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        comunidadeRepository.findByNomeIgnoreCase(dto.getNome()).ifPresent(c -> {
            throw new DuplicateResourceException("Uma comunidade com o nome '" + dto.getNome() + "' já existe.");
        });

        gamificationService.checarPermissao(authenticatedUser, TipoAcaoGamificacao.CRIAR_COMUNIDADE);

        Comunidade novaComunidade = Comunidade.builder()
            .nome(dto.getNome())
            .descricao(dto.getDescricao())
            .criador(authenticatedUser)
            .build();

        Comunidade comunidadeSalva = comunidadeRepository.save(novaComunidade);

        gamificationService.registrarAcao(authenticatedUser, TipoAcaoGamificacao.CRIAR_COMUNIDADE);

        return toResponseDTO(comunidadeSalva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComunidadeResponseDTO> listAll() {
        List<Comunidade> comunidades = comunidadeRepository.findAll();
        
        return comunidades.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ComunidadeResponseDTO findById(Long id) {
        Comunidade comunidade = comunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidade com ID " + id + " não encontrada."));
        
        return toResponseDTO(comunidade);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void deleteComunidade(Long comunidadeId) {
        comunidadeRepository.findById(comunidadeId)
            .orElseThrow(() -> new ResourceNotFoundException("Comunidade com ID " + comunidadeId + " não encontrada."));
        
        comunidadeRepository.deleteById(comunidadeId);
    }


    private ComunidadeResponseDTO toResponseDTO(Comunidade comunidade){
        return ComunidadeResponseDTO.builder()
            .id(comunidade.getId())
            .nome(comunidade.getNome())
            .descricao(comunidade.getDescricao())
            .criadorNome(comunidade.getCriador().getNome())
            .dataCriacao(comunidade.getDataCriacao())
            .build();
    }
}
