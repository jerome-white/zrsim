package util;

import java.util.StringJoiner;

public class Token {
    private final int offset;

    public Token(String document, int offset, String ngram) {
        super(document, ngram);

        this.offset = offset;
        fields.add("offset");
    }

    public Token(Token token) {
        this(token.getDocument(), token.offset, token.getNgram());
    }

    public StringJoiner compose() {
        return super().compose()
            .add(String.valueOf(offset));
    }

    public String toString() {
        return compose.toString();
    }

    public static Token fromString(String string) {
        String[] parts = string.split(Token.DELIMITER);

        String document = parts[0];
        String ngram = parts[1];
        int offset = Integer.valueOf(parts[2])/*.intValue()*/;

        return new Token(document, offset, ngram);
    }
}
