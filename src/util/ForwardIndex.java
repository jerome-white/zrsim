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

public class ForwardIndex {
    private class TokenIterator implements Iterable<Token>, Iterator<Token> {
        Iterator<Token> tokenIterator;
        Iterator<String> documentIterator;

        public TokenIterator() {
            documentIterator = index.keySet().iterator();
            tee();
        }

        public Iterator<Token> iterator() {
            return this;
        }

        private void tee() {
            tokenIterator = (documentIterator.hasNext()) ?
                index.get(documentIterator.next()).iterator() :
                Collections.emptyIterator();
        }

        public boolean hasNext() {
            return tokenIterator.hasNext();
        }

        public Token next() {
            Token token = tokenIterator.next();

            if (!hasNext()) {
                tee();
            }

            return token;
        }
    }

    private Map<String, List<Token>> index;

    public ForwardIndex() {
        index = new HashMap<String, List<Token>>();
    }

    private List<Token> seed(String document) {
        return index.computeIfAbsent(document, k -> new LinkedList<Token>());
    }

    public void add(Token token) {
        add(token.getDocument(), token);
    }

    public void add(String document, Token token) {
        seed(document).add(token);
    }

    public void add(String document, List<Token> tokens) {
        seed(document).addAll(tokens);
    }

    public void fold(ForwardIndex index) {
        index.index.forEach((k, v) -> add(k, v));
    }

    public void forEachToken(String document, Consumer<Token> consumer) {
        for (Token token : index.get(document)) {
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
