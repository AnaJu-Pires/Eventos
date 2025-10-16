package br.ifsp.events.controller;

import br.ifsp.events.dto.modalidade.ModalidadeDTO;
import br.ifsp.events.service.ModalidadeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/modalidades")
public class ModalidadeController {

    @Autowired
    private ModalidadeService modalidadeService;

    // GET /modalidades (Listar todas)
    @GetMapping
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<List<ModalidadeDTO>> listarModalidades() {
        return ResponseEntity.ok(modalidadeService.findAll());
    }

    // GET /modalidades/{id} (Buscar por ID)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<ModalidadeDTO> buscarModalidadePorId(@PathVariable Long id) {
        return ResponseEntity.ok(modalidadeService.findById(id));
    }

    // POST /modalidades (Criar)
    @PostMapping
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<ModalidadeDTO> criarModalidade(@Valid @RequestBody ModalidadeDTO modalidadeDTO) {
        ModalidadeDTO novaModalidade = modalidadeService.create(modalidadeDTO);
        return new ResponseEntity<>(novaModalidade, HttpStatus.CREATED);
    }

    // PUT /modalidades/{id} (Atualizar completo)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<ModalidadeDTO> atualizarModalidade(@PathVariable Long id, @Valid @RequestBody ModalidadeDTO modalidadeDTO) {
        return ResponseEntity.ok(modalidadeService.update(id, modalidadeDTO));
    }
    
    // PATCH /modalidades/{id} (Atualizar parcial)
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<ModalidadeDTO> atualizarParcialmenteModalidade(@PathVariable Long id, @RequestBody ModalidadeDTO modalidadeDTO) {
        return ResponseEntity.ok(modalidadeService.patch(id, modalidadeDTO));
    }

    // DELETE /modalidades/{id} (Deletar)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<Void> deletarModalidade(@PathVariable Long id) {
        modalidadeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}