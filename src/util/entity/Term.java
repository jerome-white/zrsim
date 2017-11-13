package util.entity;

public class Term extends Token {
    public Term(String term, String ngram, int offset) {
        super("term", term, ngram, offset);
    }

    public Term(String term, Posting posting) {
        this(term, posting.getNgram(), posting.getOffset());
    }

    public String getTerm() {
        return getKey();
    }
}
