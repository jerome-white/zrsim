package util;

import java.util.StringJoiner;

public class Term extends Token {
    private int end;

    public Term(Token token, String name) {
        super(token.getDocument(), token.getOffset(), name);

        end = offset + getNgram().length();
        fields.add("end");
    }

    public Term(Token token) {
        this(token, token.getNgram());
    }

    public StringJoiner compose() {
        return super.compose()
            .add(String.valueOf(end));
    }

    public String toString() {
        return compose.toString();
    }
}
