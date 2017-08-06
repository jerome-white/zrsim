package util;

import java.util.StringJoiner;

public class Term extends Token {
    private int end;

    private String name;

    public Term(Token token, String name) {
        super(token);

        this.name = name;
        end = offset + ngram.length();
    }

    public Term(Token token) {
        this(token, token.getNgram());
    }

    public String toString() {
        StringJoiner joiner = new StringJoiner(delimiter);
        joiner
            .add(name)
            .add(ngram)
            .add(String.valueOf(offset))
            .add(String.valueOf(end));

        return joiner.toString();
    }
}
