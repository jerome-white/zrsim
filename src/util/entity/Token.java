package util.entity;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringJoiner;

public class Token {
    public static final String DELIMITER = ",";

    private final int offset;
    private final String ngram;
    protected final String key;
    protected final List<String> fields;

    protected Token(String name, String key, String ngram, int offset) {
        this.key = key;
        this.ngram = ngram;
        this.offset = offset;

        fields = new LinkedList<String>(Arrays.asList(name,
                                                      "ngram",
                                                      "position"));
    }

    public String getNgram() {
        return ngram;
    }

    public int getOffset() {
        return offset;
    }

    public String getFields() {
        StringJoiner joiner = new StringJoiner(Token.DELIMITER);

        for (String field : fields) {
            joiner.add(field);
        }

        return joiner.toString();
    }

    public String toString() {
        return new StringJoiner(Token.DELIMITER)
            .add(key)
            .add(ngram)
            .add(String.valueOf(offset))
            .toString();
    }
}
