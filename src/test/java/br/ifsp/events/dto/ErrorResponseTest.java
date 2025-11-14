package br.ifsp.events.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void builderSetsFields_andTimestampIsPresent() {
        ErrorResponse err = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Recurso não encontrado")
                .path("/api/test")
                .build();

        assertEquals(404, err.getStatus());
        assertEquals("Not Found", err.getError());
        assertEquals("Recurso não encontrado", err.getMessage());
        assertEquals("/api/test", err.getPath());
        assertNotNull(err.getTimestamp());
        assertTrue(err.getTimestamp() instanceof Instant);
    }

    @Test
    void serializationDoesNotIncludeNulls() throws Exception {
        ErrorResponse err = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Dados inválidos")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = mapper.writeValueAsString(err);

        assertTrue(json.contains("\"status\":400"));
        assertFalse(json.contains("validationErrors"), "Null fields should be excluded from JSON");
    }
}
