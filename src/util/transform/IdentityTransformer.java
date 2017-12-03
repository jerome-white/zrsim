package util.transform;

public class IdentityTransformer implements NgramTransformer {
    public String transform(String ngram) {
        return ngram;
    }
}
