package util.entity;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringJoiner;

public class Token {
    public static final String DELIMITER = ",";

    private final int offset;
    private final String key;
    private final String ngram;
    protected final List<String> fields;

    protected Token(String name, String key, String ngram, int offset) {
        this.key = key;
        this.ngram = ngram;
        this.offset = offset;

        fields = new LinkedList<String>(Arrays.asList(name, "ngram", "start"));
    }

    public String getNgram() {
        return ngram;
    }

    protected String getKey() {
        return key;
    }

    public int getOffset() {
        return offset;
    }

    public String getFields() {
        StringJoiner joiner = new StringJoiner(Entity.DELIMITER);

        for (String field : fields) {
            joiner.add(field);
        }

        return joiner.toString();
    }

    public String toString() {
        return new StringJoiner(Entity.DELIMITER)
            .add(key)
            .add(ngram)
            .add(String.valueOf(offset))
            .toString();
    }
}
