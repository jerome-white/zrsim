package util;

import java.util.StringJoiner;

public class Token {
    protected final int offset;

    protected final String document;
    protected final String ngram;
    protected final String delimiter;

    public Token(String document, int offset, String ngram, String delimiter) {
        this.document = document;
        this.offset = offset;
        this.ngram = ngram;
        this.delimiter = delimiter;
    }

    public Token(String document, int offset, String ngram) {
        this(document, offset, ngram, ",");
    }

    public Token(Token token) {
        this(token.document, token.offset, token.ngram, token.delimiter);
    }

    public String getNgram() {
        return ngram;
    }

    public String getDocument() {
        return document;
    }

    public String toString() {
        StringJoiner joiner = new StringJoiner(delimiter);
        joiner
            .add(document)
            .add(ngram)
            .add(String.valueOf(offset));

        return joiner.toString();
    }

    public static Token fromString(String string, String delimiter) {
        String[] parts = string.split(delimiter);

        String document = parts[0];
        String ngram = parts[1];
        int offset = Integer.valueOf(parts[2])/*.intValue()*/;

        return new Token(document, offset, ngram, delimiter);
    }

    public static Token fromString(String string) {
        return Token.fromString(string, ",");
    }
}
