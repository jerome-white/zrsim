package util;

import java.lang.Iterable;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Token>> index;

    public ForwardIndex() {
        index = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Token>>();
    }

    public void add(String document, Token token) {
        index
            .computeIfAbsent(document, k -> new ConcurrentLinkedQueue<Token>())
            .add(token);
    }

    public void add(Token token) {
        add(token.getDocument(), token);
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
