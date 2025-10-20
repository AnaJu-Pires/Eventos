package br.ifsp.events.dto.user;

import java.util.List;

import br.ifsp.events.dto.modalidade.ModalidadeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInteresseResponseDTO {
    private List<ModalidadeDTO> interesses;
}