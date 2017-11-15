package util.entity;

public class Term extends Token {
    public Term(String name, String ngram, int offset) {
        super("name", name, ngram, offset);
    }

    public Term(String name, Posting posting) {
        this(name, posting.getNgram(), posting.getOffset());
    }

    public String getName() {
        return key;
    }
}
