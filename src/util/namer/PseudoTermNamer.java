package util.namer;

import java.lang.Iterable;
import java.lang.StringBuilder;

import util.entity.Posting;

public class PseudoTermNamer extends IncrementalNamer {
    private String format;

    public PseudoTermNamer(Iterable<Posting> postings) {
        super(postings);

        StringBuilder fmt = new StringBuilder("pt%0");
        format = fmt
            .append(String.valueOf(terms.size()).length())
            .append("d")
            .toString();
    }

    public String get(String ngram) {
        return String.format(format, super.get(ngram));
    }
}
