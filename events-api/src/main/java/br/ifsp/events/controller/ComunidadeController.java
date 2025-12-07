package br.ifsp.events.controller;

import java.net.URI;
import java.util.List;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.ifsp.events.dto.ErrorResponse;
import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.dto.comunidade.ComunidadeResponseDTO;
import br.ifsp.events.dto.post.PostCreateDTO;
import br.ifsp.events.dto.post.PostResponseDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.service.ComunidadeService;
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
@RequestMapping("/comunidades")
@Tag(name = "Comunidades", description = "Endpoints para gerenciamento de comunidades")
@SecurityRequirement(name = "bearerAuth")
public class ComunidadeController {

    private final ComunidadeService comunidadeService;
    private final PostService postService;

    public ComunidadeController(ComunidadeService comunidadeService, PostService postService) {
        this.comunidadeService = comunidadeService;
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria uma nova comunidade", description= "Cria uma nova comunidade no sistema. Requer Nível GOLD e 1500 pontos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Comunidade criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Regra de negócio violada (ex: Nível/Pontos insuficientes, nome duplicado)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (Nível não é GOLD)")
    })
    public ResponseEntity<ComunidadeResponseDTO> create(@RequestBody @Valid ComunidadeCreateDTO dto) {
        ComunidadeResponseDTO novaComunidade = comunidadeService.create(dto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(novaComunidade.getId())
            .toUri();

        return ResponseEntity.created(location).body(novaComunidade);
    }

    @PostMapping("/{comunidadeId}/posts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria um novo post em uma comunidade", description = "Cria um novo post. Requer 100 pontos de saldo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Post criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Regra de negócio violada (ex: Pontos insuficientes)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Comunidade não encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PostResponseDTO> createPost(
            @Parameter(description = "ID da comunidade onde o post será criado") @PathVariable Long comunidadeId,
            @RequestBody @Valid PostCreateDTO dto) {
        
        PostResponseDTO novoPost = postService.create(dto, comunidadeId);

        return ResponseEntity.status(201).body(novoPost);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista todas as comunidades", 
               description = "Retorna uma lista de todas as comunidades criadas no sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de comunidades retornada")
    })
    public ResponseEntity<List<ComunidadeResponseDTO>> listAll() {
        return ResponseEntity.ok(comunidadeService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Busca uma comunidade por ID", 
               description = "Retorna os dados de uma comunidade específica.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comunidade encontrada"),
        @ApiResponse(responseCode = "404", description = "Comunidade não encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ComunidadeResponseDTO> findById(
            @Parameter(description = "ID da comunidade a ser buscada") @PathVariable Long id) { 
        return ResponseEntity.ok(comunidadeService.findById(id));
    }

    @GetMapping("/{comunidadeId}/posts")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista os posts de uma comunidade",
               description = "Retorna uma lista paginada de todos os posts de uma comunidade específica.")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Lista de posts retornada"),
        @ApiResponse(responseCode = "404", description = "Comunidade não encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PageableAsQueryParam 
    public ResponseEntity<Page<PostResponseDTO>> listPostsByComunidade(
            @Parameter(description = "ID da comunidade para listar os posts") @PathVariable Long comunidadeId,
            @Parameter(hidden = true) @PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        
        return ResponseEntity.ok(postService.listByComunidade(comunidadeId, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Deleta uma comuniade (Admin)", description = "Permite que um administrador (`ADMIN`) delete uma comunidade do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Comunidade removida com sucesso",
            content = @Content(mediaType = "application/json", 
                         schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: comunidade não existente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado. Token não fornecido ou inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com o perfil 'ADMIN' podem executar esta ação",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Comunidade não encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteComunidade(@PathVariable Long id) {
        comunidadeService.deleteComunidade(id);
        return ResponseEntity.noContent().build();
    }

}
