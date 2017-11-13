package util.entity;

public class Posting extends Token {
    public Posting(String document, String ngram, int offset) {
        super("document", document, ngram, offset);
    }

    private Posting(String[] parts) {
        this(parts[0], parts[1], Integer.valueOf(parts[2]));
    }

    public Posting(String asString) {
        this(asString.split(Token.DELIMITER));
    }

    public String getDocument() {
        return getKey();
    }
}
