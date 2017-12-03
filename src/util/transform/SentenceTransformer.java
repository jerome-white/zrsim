package util.transform;

public class SentenceTransformer extends TransformDecorator {
    public SentenceTransformer(NgramTransformer transformer) {
        super(transformer);
    }

    public String transform(String ngram) {
        if (ngram.contains(".")) {
            throw new IllegalArgumentException();
        }

        return super.transform(ngram);
    }
}
