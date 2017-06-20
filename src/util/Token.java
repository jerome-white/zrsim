package util;

import java.util.StringJoiner;

public class Token {
    private final int offset;

    private final String document;
    private final String ngram;
    private final String delimiter = ",";

    public Token(String document, int offset, String ngram) {
        this.document = document;
        this.offset = offset;
        this.ngram = ngram;
    }

    public String toString() {
        StringJoiner joiner = new StringJoiner(delimiter);
        joiner
            .add(document)
            .add(ngram)
            .add(String.valueOf(offset));

        return joiner.toString();
    }
}
