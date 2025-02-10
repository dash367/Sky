package com.craftinginterepter.sky;

public class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    static class Return extends RuntimeException {
        final Object value;

        Return(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }

    static class BreakException extends RuntimeException {
        BreakException() {
            super(null, null, false, false);
        }
    }

    static class ContinueException extends RuntimeException {
        ContinueException() {
            super(null, null, false, false);
        }
    }
}
