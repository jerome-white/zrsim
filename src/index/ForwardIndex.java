package index;

import java.lang.Iterable;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;
import java.util.function.Consumer;

import util.entity.Posting;

public class ForwardIndex {
    private class PartialPosting {
        private final int offset;
        private final String ngram;

        public PartialPosting(Posting posting) {
            offset = posting.getOffset();
            ngram = posting.getNgram();
        }

        public Posting toPosting(String document) {
            return new Posting(document, ngram, offset);
        }
    }

    private class PostingIterator implements Iterable<Posting>,
                                             Iterator<Posting> {
        private String currentDoc;
        private Iterator<PartialPosting> postingIterator;
        private Iterator<String> documentIterator;

        public PostingIterator() {
            documentIterator = index.keySet().iterator();
            tee();
        }

        public Iterator<Posting> iterator() {
            return this;
        }

        private void tee() {
            if (documentIterator.hasNext()) {
                currentDoc = documentIterator.next();
                postingIterator = index.get(currentDoc).iterator();
            }
            else {
                postingIterator = Collections.emptyIterator();
            }
        }

        public boolean hasNext() {
            return postingIterator.hasNext();
        }

        public Posting next() {
            Posting posting = postingIterator.next().toPosting(currentDoc);

            if (!hasNext()) {
                tee();
            }

            return posting;
        }
    }

    private Map<String, List<PartialPosting>> index;

    public ForwardIndex() {
        index = new HashMap<String, List<PartialPosting>>();
    }

    private List<PartialPosting> seed(String document) {
        return index
            .computeIfAbsent(document, k -> new LinkedList<PartialPosting>());
    }

    public void add(String document, Posting posting) {
        seed(document).add(new PartialPosting(posting));
    }

    public void add(Posting posting) {
        add(posting.getDocument(), posting);
    }

    public void fold(ForwardIndex index) {
        index.index.forEach((k, v) -> seed(k).addAll(v));
    }

    public void forEachPosting(String document, Consumer<Posting> consumer) {
        for (PartialPosting partial : index.get(document)) {
            consumer.accept(partial.toPosting(document));
        }
    }

    public Iterable<Posting> postingIterator() {
        return new PostingIterator();
    }

    public Set<String> documents() {
        return index.keySet();
    }
}
