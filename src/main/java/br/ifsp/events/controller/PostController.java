package br.ifsp.events.controller;

import java.net.URI;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.ifsp.events.dto.ErrorResponse;
import br.ifsp.events.dto.comentario.ComentarioCreateDTO;
import br.ifsp.events.dto.comentario.ComentarioResponseDTO;
import br.ifsp.events.dto.post.PostResponseDTO;
import br.ifsp.events.service.ComentarioService;
import br.ifsp.events.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Endpoints para gerenciamento de posts e comentários")
@SecurityRequirement(name = "bearerAuth")
public class PostController {

    private final ComentarioService comentarioService;
    private final PostService postService;

    public PostController(ComentarioService comentarioService, PostService postService) {
        this.comentarioService = comentarioService;
        this.postService = postService;
    }

    @PostMapping("/{postId}/comentarios")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria um novo comentário em um post", 
               description = "Cria um novo comentário ou uma resposta a outro comentário. Recompensa o autor com 5 pontos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Comentário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: conteúdo vazio)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Post ou Comentário Pai não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ComentarioResponseDTO> createComentario(
            @Parameter(description = "ID do post que está sendo comentado") @PathVariable Long postId,
            @RequestBody @Valid ComentarioCreateDTO dto) {
        
        ComentarioResponseDTO novoComentario = comentarioService.create(dto, postId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/comentarios/{id}")
                .buildAndExpand(novoComentario.getId())
                .toUri();

        return ResponseEntity.created(location).body(novoComentario);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Busca um post por ID", 
               description = "Retorna os dados de um post específico, incluindo seu placar de votos atual.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post encontrado"),
        @ApiResponse(responseCode = "404", description = "Post não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PostResponseDTO> findById(
            @Parameter(description = "ID do post a ser buscado") @PathVariable Long id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    @GetMapping("/{postId}/comentarios")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista os comentários de um post",
               description = "Retorna uma lista paginada dos comentários de nível superior (não-respostas) de um post.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de comentários retornada"),
        @ApiResponse(responseCode = "404", description = "Post não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PageableAsQueryParam
    public ResponseEntity<Page<ComentarioResponseDTO>> listComentariosByPost(
            @Parameter(description = "ID do post para listar os comentários") @PathVariable Long postId,
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "votos", direction = Sort.Direction.DESC) Pageable pageable) {
        
        return ResponseEntity.ok(comentarioService.listByPost(postId, pageable));
    }
}