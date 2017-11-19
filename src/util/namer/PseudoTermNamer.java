package util.namer;

import java.util.Map;
import java.util.HashMap;
import java.lang.Iterable;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException;

import util.entity.Posting;

public class PseudoTermNamer implements TermNamer {
    private String format;
    private Map<String, Integer> terms;

    public PseudoTermNamer(Iterable<Posting> postings) {
        terms = new HashMap<String, Integer>();

        for (Posting p : postings) {
            terms.putIfAbsent(p.getNgram(), terms.size());
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
