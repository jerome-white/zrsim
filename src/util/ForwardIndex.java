package util;

import java.lang.Iterable;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;
import java.util.function.Consumer;

import util.entity.Token;

public class ForwardIndex {
    private class Nugget {
        public final int offset;
        public final String ngram;

        public Nugget(Token token) {
            offset = token.getOffset();
            ngram = token.getNgram();
        }

        public Token toToken(String document) {
            return new Token(document, ngram, offset);
        }
    }

    private class TokenIterator implements Iterable<Token>, Iterator<Token> {
        private String currentDoc;
        private Iterator<Nugget> nuggetIterator;
        private Iterator<String> documentIterator;

        public TokenIterator() {
            documentIterator = index.keySet().iterator();
            tee();
        }

        public Iterator<Token> iterator() {
            return this;
        }

        private void tee() {
            if (documentIterator.hasNext()) {
                currentDoc = documentIterator.next();
                nuggetIterator = index.get(currentDoc).iterator();
            }
            else {
                nuggetIterator = Collections.emptyIterator();
            }
        }

        public boolean hasNext() {
            return nuggetIterator.hasNext();
        }

        public Token next() {
            Token token = nuggetIterator.next().toToken(currentDoc);

            if (!hasNext()) {
                tee();
            }

            return token;
        }
    }

    private Map<String, List<Nugget>> index;

    public ForwardIndex() {
        index = new HashMap<String, List<Nugget>>();
    }

    private List<Nugget> seed(String document) {
        return index.computeIfAbsent(document, k -> new LinkedList<Nugget>());
    }

    public void add(String document, Token token) {
        seed(document).add(new Nugget(token));
    }

    public void add(Token token) {
        add(token.getDocument(), token);
    }

    public void fold(ForwardIndex index) {
        index.index.forEach((k, v) -> seed(k).addAll(v));
    }

    public void forEachToken(String document, Consumer<Token> consumer) {
        for (Nugget nugget : index.get(document)) {
            Token token = new Token(document, nugget.ngram, nugget.offset);
            consumer.accept(token);
        }
    }

    public Iterable<Token> tokenIterator() {
        return new TokenIterator();
    }

    public Set<String> documents() {
        return index.keySet();
    }
}
