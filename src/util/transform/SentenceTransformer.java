package util.transform;

public class SentenceTransformer extends TransformDecorator {
    private String eos;

    public SentenceTransformer(NgramTransformer transformer, String eos) {
        super(transformer);
	this.eos = eos;
    }

    public SentenceTransformer(NgramTransformer transformer) {
	this(transformer, ".");
    }

    public String transform(String ngram) {
        if (ngram.contains(eos)) {
            throw new IllegalArgumentException();
        }

        return super.transform(ngram);
    }
}
