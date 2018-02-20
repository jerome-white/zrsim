package util.entity;

import java.util.List;

public class Posting extends Token {
    public Posting(String document, String ngram, int offset) {
        super("document", document, ngram, offset);
    }

    private Posting(List<String> parts) {
        this(parts.get(0), parts.get(1), Integer.valueOf(parts.get(2)));
    }

    public Posting(String asString) {
        this(Token.fromString(asString));
    }

    public String getDocument() {
        return key;
    }
}
