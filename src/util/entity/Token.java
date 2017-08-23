package util.entity;

import java.util.StringJoiner;

public class Token extends Entity {
    private final int offset;

    public Token(String document, String ngram, int offset) {
        super(document, ngram);

        this.offset = offset;
        fields.add("start");
    }

    public int getOffset() {
        return offset;
    }

    protected StringJoiner compose() {
        return super.compose()
            .add(String.valueOf(offset));
    }

    public String toString() {
        return compose().toString();
    }

    public static Token fromString(String string) {
        String[] parts = string.split(Entity.DELIMITER);

        return new Token(parts[0], parts[1], Integer.valueOf(parts[2]));
    }
}
