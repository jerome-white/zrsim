package util;

import java.util.Map;
import java.util.HashMap;
import java.lang.Iterable;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException;

public class PseudoTerm implements TermNamer {
    private Map<String, Integer> terms;

    public PseudoTerm (Iterable<Token> tokens) {
        terms = new HashMap<String, Integer>();
        for (Token token : tokens) {
            terms.putIfAbsent(token.getNgram(), terms.size() + 1);
        }
    }

    public String get(String ngram) {
        if (!terms.containsKey(ngram)) {
            throw new IllegalArgumentException();
        }

        StringBuilder term = new StringBuilder("pt");

        return term
            .append(terms.get(ngram))
            .toString();
    }
}
