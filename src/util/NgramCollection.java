package util;

public interface NgramCollection {
    /**
     * @param ngram n-gram to be added
     * @param document Document that contains this n-gram
     * @param offset Location within document where n-gram starts
     **/
    public void add(CharSequence ngram, String document, int offset);
}
