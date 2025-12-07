package br.ifsp.events.exception;

public class CsvGenerationException extends RuntimeException {

    public CsvGenerationException(String message) {
        super(message);
    }
    public CsvGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}