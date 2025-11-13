package br.ifsp.events.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.dto.comentario.ComentarioCreateDTO;
import br.ifsp.events.dto.comentario.ComentarioResponseDTO;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Comentario;
import br.ifsp.events.model.Post;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComentarioRepository;
import br.ifsp.events.repository.PostRepository;
import br.ifsp.events.service.ComentarioService;
import br.ifsp.events.service.GamificationService;

@Service
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PostRepository postRepository;
    private final GamificationService gamificationService;

    public ComentarioServiceImpl(ComentarioRepository comentarioRepository, PostRepository postRepository, GamificationService gamificationService) {
        this.comentarioRepository = comentarioRepository;
        this.postRepository = postRepository;
        this.gamificationService = gamificationService;
    }

    @Override
    @Transactional
    public ComentarioResponseDTO create(ComentarioCreateDTO dto, Long postId) {
        User autor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post com ID " + postId + " não encontrado."));

        Comentario comentarioPai = null;
        if (dto.getComentarioPaiId() != null) {
            comentarioPai = comentarioRepository.findById(dto.getComentarioPaiId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentário pai com ID " + dto.getComentarioPaiId() + " não encontrado."));
        }

        Comentario novoComentario = Comentario.builder()
                .conteudo(dto.getConteudo())
                .autor(autor)
                .post(post)
                .comentarioPai(comentarioPai)
                .votos(0)
                .build();

        Comentario comentarioSalvo = comentarioRepository.save(novoComentario);

        gamificationService.registrarAcao(autor, TipoAcaoGamificacao.CRIAR_COMENTARIO);

        if (!autor.getId().equals(post.getAutor().getId())) {
            gamificationService.registrarAcao(post.getAutor(), TipoAcaoGamificacao.RECEBER_COMENTARIO);
        }

        return toResponseDTO(comentarioSalvo);
    }

    private ComentarioResponseDTO toResponseDTO(Comentario comentario) {
        return ComentarioResponseDTO.builder()
                .id(comentario.getId())
                .conteudo(comentario.getConteudo())
                .autorNome(comentario.getAutor().getNome())
                .postId(comentario.getPost().getId())
                .comentarioPaiId(comentario.getComentarioPai() != null ? comentario.getComentarioPai().getId() : null)
                .dataCriacao(comentario.getDataCriacao())
                .votos(comentario.getVotos())
                .build();
    }
}