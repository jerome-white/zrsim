package util;

import java.util.Map;
import java.util.HashMap;
import java.lang.Iterable;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException;

public class PseudoTerm implements TermNamer {
    private String format;
    private Map<String, Integer> terms;

    public PseudoTerm (Iterable<Token> tokens) {
        terms = new HashMap<String, Integer>();

        for (Token token : tokens) {
            terms.putIfAbsent(token.getNgram(), terms.size() + 1);
        }

        StringBuilder fmt = new StringBuilder("pt%0");
        format = fmt
            .append(String.valueOf(terms.size()).length())
            .append("d")
            .toString();
    }

    public String get(String ngram) {
        if (!terms.containsKey(ngram)) {
            throw new IllegalArgumentException();
        }

        return String.format(format, terms.get(ngram));
    }
}
