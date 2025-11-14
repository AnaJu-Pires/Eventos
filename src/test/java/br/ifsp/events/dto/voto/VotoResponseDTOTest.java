package br.ifsp.events.dto.voto;

import br.ifsp.events.model.TipoVoto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VotoResponseDTOTest {

    @Test
    void builderSetsAllFields() {
        VotoResponseDTO dto = VotoResponseDTO.builder()
                .novoPlacar(10)
                .seuVoto(TipoVoto.DOWNVOTE)
                .build();

        assertEquals(10, dto.getNovoPlacar());
        assertEquals(TipoVoto.DOWNVOTE, dto.getSeuVoto());
    }
}
