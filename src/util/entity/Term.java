package util.entity;

import java.util.StringJoiner;

public class Term extends Token {
    private final int end;

    public Term(Token token, String name) {
        super(name, token.getNgram(), token.getOffset());

        end = getOffset() + getNgram().length();

        fields.set(0, "term");
        fields.add("end");
    }

    public Term(Token token) {
        this(token, token.getNgram());
    }

    protected StringJoiner compose() {
        return super.compose()
            .add(String.valueOf(end));
    }

    public String toString() {
        return compose().toString();
    }
}
