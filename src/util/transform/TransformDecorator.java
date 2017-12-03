package util.transform;

public abstract class TransformDecorator implements NgramTransformer {
    private NgramTransformer transformer;

    public TransformDecorator(NgramTransformer transformer) {
        this.transformer = transformer;
    }

    public String transform(String ngram) {
        return transformer.transform(ngram);
    }
}
