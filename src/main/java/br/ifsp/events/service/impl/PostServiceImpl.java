package br.ifsp.events.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.ifsp.events.dto.post.PostCreateDTO;
import br.ifsp.events.dto.post.PostResponseDTO;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.Post;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComunidadeRepository;
import br.ifsp.events.repository.PostRepository;
import br.ifsp.events.service.GamificationService;
import br.ifsp.events.service.PostService;
import jakarta.transaction.Transactional;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ComunidadeRepository comunidadeRepository;
    private final GamificationService gamificationService;

    public PostServiceImpl(PostRepository postRepository, ComunidadeRepository comunidadeRepository, GamificationService gamificationService){
        this.postRepository = postRepository;
        this.comunidadeRepository = comunidadeRepository;
        this.gamificationService = gamificationService;
    }

    @Override
    @Transactional
    public PostResponseDTO create(PostCreateDTO dto, Long comunidadeId){
        User autor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Comunidade comunidade = comunidadeRepository.findById(comunidadeId).orElseThrow(() -> new ResourceNotFoundException("Comunidade com ID " + comunidadeId + " não encontrada."));

        gamificationService.checarPermissao(autor, TipoAcaoGamificacao.CRIAR_POST);

        Post novoPost = Post.builder()
                .titulo(dto.getTitulo())
                .conteudo(dto.getConteudo())
                .autor(autor)
                .comunidade(comunidade)
                .votos(0)
                .build();

        Post postSalvo = postRepository.save(novoPost);

        gamificationService.registrarAcao(autor, TipoAcaoGamificacao.CRIAR_POST);

        if (!autor.getId().equals(comunidade.getCriador().getId())) {
            gamificationService.registrarAcao(comunidade.getCriador(), TipoAcaoGamificacao.RECEBER_POST_EM_COMUNIDADE);
        }

        return toResponseDTO(postSalvo);
    }

    private PostResponseDTO toResponseDTO(Post post) {
        return PostResponseDTO.builder()
                .id(post.getId())
                .titulo(post.getTitulo())
                .conteudo(post.getConteudo())
                .autorNome(post.getAutor().getNome())
                .comunidadeNome(post.getComunidade().getNome())
                .dataCriacao(post.getDataCriacao())
                .votos(post.getVotos())
                .build();
    }
}
