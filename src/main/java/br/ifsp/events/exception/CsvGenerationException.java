package br.ifsp.events.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CsvGenerationException extends RuntimeException {

    public CsvGenerationException(String message) {
        super(message);
    }
}