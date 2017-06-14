import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Manager {
    public static void main(String[] args) {
        File directory = new File(args[0]);
        int min_ngrams = Integer.parseInt(args[1]);
        int max_ngrams = Integer.parseInt(args[2]);
        
        SuffixTree root = new SuffixTree();

        /*
         * add the documents to the suffix tree
         */
        int pool = Runtime.getRuntime().availableProcessors();
        ExecutorService es = Executors.newFixedThreadPool(pool);
        for (File document : directory.listFiles()) {
            assert !document.isDirectory();
            es.execute(new SuffixTreeBuilder(root, document, max_ngrams));
        }
        es.shutdown();

        /*
         * Prune the tree by marking entries that are redundant
         */
        root.getChildren().forEachValue(1, v -> v.markRedundants());

        // SuffixTreeIterator it =
        //     new SuffixTreeIterator(root) ; //, 2, min_ngrams, false);
        // for (Token token : it) {
        //     if (token != null) {
        //         System.out.println(token);
        //     }
        // }

        root.getChildren().forEach((k, v) -> {
                v.accept(new SuffixTreeVisitor(min_ngrams, 2, false));
            });

        // root.dump();
    }
}
