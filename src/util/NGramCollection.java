package util;

import java.nio.CharBuffer;

public interface NGramCollection {
    /**
     * @param ngram n-gram to be added
     * @param document Document that contains this n-gram
     * @param offset Location within document where n-gram starts
     **/
    public void add(CharBuffer ngram, String document, int offset);
}