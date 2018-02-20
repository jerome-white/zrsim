package util.namer;

import java.util.concurrent.ConcurrentHashMap;

public class SequentialNamer implements TermNamer {
    private ConcurrentHashMap<String, Integer> terms;

    public SequentialNamer() {
        terms = new ConcurrentHashMap<String, Integer>();
    }

    public String get(String ngram) {
        return terms
            .computeIfAbsent(ngram, k -> terms.size())
            .toString();
    }
}
