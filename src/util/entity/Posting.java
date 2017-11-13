package util.entity;

public class Posting extends Token {
    private final int offset;

    public Token(String document, String ngram, int offset) {
        super("document", document, ngram, offset);
    }

    private Token(String[] parts) {
        this(parts[0], parts[1], Integer.valueOf(parts[2]));
    }

    public Token(String asString) {
        this(asString.split(Entity.DELIMITER));
    }

    public int getDocument() {
        return getName();
    }
}
