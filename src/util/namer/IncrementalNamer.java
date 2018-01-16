package util.namer;

import java.util.Map;
import java.util.HashMap;
import java.lang.Iterable;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException;

import util.entity.Posting;

public class IncrementalNamer implements TermNamer {
    protected Map<String, Integer> terms;

    public IncrementalNamer(Iterable<Posting> postings) {
        terms = new HashMap<String, Integer>();

        for (Posting p : postings) {
            terms.putIfAbsent(p.getNgram(), terms.size());
        }
    }

    public String get(String ngram) {
        if (!terms.containsKey(ngram)) {
            throw new IllegalArgumentException();
        }

        return terms
            .get(ngram)
            .toString();
    }
}
