package util.entity;

import java.util.List;
import java.util.Arrays;
import java.util.StringJoiner;

public abstract class Entity {
    public static final String DELIMITER = ",";

    private final String document;
    private final String ngram;
    protected final List<String> fields;

    public Entity(String document, String ngram) {
        this.document = document;
        this.ngram = ngram;

        fields = Arrays.asList("term", "ngram");
    }

    public String getNgram() {
        return ngram;
    }

    public String getDocument() {
        return document;
    }

    public String getFields() {
        StringJoiner joiner = new StringJoiner(Entity.DELIMITER);

        for (String field : fields) {
            joiner.add(field);
        }

        return joiner.toString();
    }

    protected StringJoiner compose() {
        StringJoiner composition = new StringJoiner(Entity.DELIMITER);

        return composition
            .add(document)
            .add(ngram);
    }

    public abstract String toString();
}
