package br.ifsp.events.service.impl;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.dto.voto.VotoCreateDTO;
import br.ifsp.events.dto.voto.VotoResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Comentario;
import br.ifsp.events.model.Post;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.TipoVoto;
import br.ifsp.events.model.User;
import br.ifsp.events.model.Voto;
import br.ifsp.events.repository.ComentarioRepository;
import br.ifsp.events.repository.PostRepository;
import br.ifsp.events.repository.VotoRepository;
import br.ifsp.events.service.GamificationService;
import br.ifsp.events.service.VotoService;

@Service
public class VotoServiceImpl implements VotoService {

    private final VotoRepository votoRepository;
    private final PostRepository postRepository;
    private final ComentarioRepository comentarioRepository;
    private final GamificationService gamificationService;

    public VotoServiceImpl(VotoRepository votoRepository, PostRepository postRepository,
                           ComentarioRepository comentarioRepository, GamificationService gamificationService) {
        this.votoRepository = votoRepository;
        this.postRepository = postRepository;
        this.comentarioRepository = comentarioRepository;
        this.gamificationService = gamificationService;
    }

    @Override
    @Transactional
    public VotoResponseDTO votar(VotoCreateDTO dto) {
        User votante = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (dto.getPostId() != null && dto.getComentarioId() != null) {
            throw new BusinessRuleException("Não é possível votar em um post e um comentário simultaneamente.");
        }
        if (dto.getPostId() == null && dto.getComentarioId() == null) {
            throw new BusinessRuleException("Você deve especificar um postId ou um comentarioId para votar.");
        }

        if (dto.getPostId() != null) {
            return processarVotoPost(dto, votante);
        } else {
            return processarVotoComentario(dto, votante);
        }
    }

    private VotoResponseDTO processarVotoPost(VotoCreateDTO dto, User votante) {
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post com ID " + dto.getPostId() + " não encontrado."));
        
        Optional<Voto> votoExistente = votoRepository.findByUsuarioIdAndPostId(votante.getId(), post.getId());

        TipoVoto votoFinal = null;

        if (votoExistente.isEmpty()) {
            Voto novoVoto = Voto.builder()
                    .usuario(votante)
                    .post(post)
                    .tipoVoto(dto.getTipoVoto())
                    .build();
            votoRepository.save(novoVoto);
            votoFinal = dto.getTipoVoto();
            
            gamificationService.registrarAcao(votante, TipoAcaoGamificacao.VOTAR); 
            gamificationService.registrarAcao(post.getAutor(), getAcaoAutor(dto.getTipoVoto()));
            
        } else {
            Voto voto = votoExistente.get();
            if (voto.getTipoVoto() == dto.getTipoVoto()) {
                votoRepository.delete(voto);
                votoFinal = null;

                gamificationService.registrarAcao(post.getAutor(), getAcaoReversaAutor(voto.getTipoVoto()));
                
            } else {
                voto.setTipoVoto(dto.getTipoVoto());
                votoRepository.save(voto);
                votoFinal = dto.getTipoVoto();

                gamificationService.registrarAcao(post.getAutor(), getAcaoReversaAutor(voto.getTipoVoto()));
                gamificationService.registrarAcao(post.getAutor(), getAcaoAutor(dto.getTipoVoto()));
            }
        }
        
        int novoPlacar = calcularPlacarPost(post.getId());
        post.setVotos(novoPlacar);
        postRepository.save(post);

        return VotoResponseDTO.builder()
                .novoPlacar(novoPlacar)
                .seuVoto(votoFinal)
                .build();
    }

    private VotoResponseDTO processarVotoComentario(VotoCreateDTO dto, User votante) {
        Comentario comentario = comentarioRepository.findById(dto.getComentarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Comentário com ID " + dto.getComentarioId() + " não encontrado."));

        Optional<Voto> votoExistente = votoRepository.findByUsuarioIdAndComentarioId(votante.getId(), comentario.getId());

        TipoVoto votoFinal = null;

        if (votoExistente.isEmpty()) {
            Voto novoVoto = Voto.builder()
                    .usuario(votante)
                    .comentario(comentario)
                    .tipoVoto(dto.getTipoVoto())
                    .build();
            votoRepository.save(novoVoto);
            votoFinal = dto.getTipoVoto();
            
            gamificationService.registrarAcao(votante, TipoAcaoGamificacao.VOTAR);
            gamificationService.registrarAcao(comentario.getAutor(), getAcaoAutor(dto.getTipoVoto()));
            
        } else {
            Voto voto = votoExistente.get();
            if (voto.getTipoVoto() == dto.getTipoVoto()) {
                votoRepository.delete(voto);
                votoFinal = null;
                gamificationService.registrarAcao(comentario.getAutor(), getAcaoReversaAutor(voto.getTipoVoto()));
            } else {
                voto.setTipoVoto(dto.getTipoVoto());
                votoRepository.save(voto);
                votoFinal = dto.getTipoVoto();
                gamificationService.registrarAcao(comentario.getAutor(), getAcaoReversaAutor(voto.getTipoVoto()));
                gamificationService.registrarAcao(comentario.getAutor(), getAcaoAutor(dto.getTipoVoto()));
            }
        }

        int novoPlacar = calcularPlacarComentario(comentario.getId());
        comentario.setVotos(novoPlacar);
        comentarioRepository.save(comentario);

        return VotoResponseDTO.builder()
                .novoPlacar(novoPlacar)
                .seuVoto(votoFinal)
                .build();
    }

    
    private TipoAcaoGamificacao getAcaoAutor(TipoVoto tipo) {
        return (tipo == TipoVoto.UPVOTE) ? TipoAcaoGamificacao.RECEBER_UPVOTE : TipoAcaoGamificacao.RECEBER_DOWNVOTE;
    }

    private TipoAcaoGamificacao getAcaoReversaAutor(TipoVoto tipo) {
        return (tipo == TipoVoto.UPVOTE) ? TipoAcaoGamificacao.RECEBER_DOWNVOTE : TipoAcaoGamificacao.RECEBER_UPVOTE;
    }

    
    private int calcularPlacarPost(Long postId) {
        long upvotes = votoRepository.countByPostIdAndTipoVoto(postId, TipoVoto.UPVOTE);
        long downvotes = votoRepository.countByPostIdAndTipoVoto(postId, TipoVoto.DOWNVOTE);
        return (int) (upvotes - downvotes);
    }

    private int calcularPlacarComentario(Long comentarioId) {
        long upvotes = votoRepository.countByComentarioIdAndTipoVoto(comentarioId, TipoVoto.UPVOTE);
        long downvotes = votoRepository.countByComentarioIdAndTipoVoto(comentarioId, TipoVoto.DOWNVOTE);
        return (int) (upvotes - downvotes);
    }
}