package br.ifsp.events.dto.user;

import java.util.List;

import br.ifsp.events.dto.modalidade.ModalidadeRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInteresseResponseDTO {
    private List<ModalidadeRequestDTO> interesses;
}