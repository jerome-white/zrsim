import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Manager {
    private final static Logger LOGGER =
        Logger.getLogger(Manager.class.getName());

    public static void main(String[] args) {
        LOGGER.setLevel(Level.INFO);

        File directory = new File(args[0]);
        int min_ngrams = Integer.parseInt(args[1]);
        int max_ngrams = Integer.parseInt(args[2]);

        SuffixTree root = new SuffixTree();

        /*
         * add the documents to the suffix tree
         */
        LOGGER.info("Adding terms");

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
        LOGGER.info("Term selection");

        root.getChildren().forEachValue(1, v -> v.markRedundants());

        /*
         * Dump the tree to disk
         */
        LOGGER.info("Terms to disk");

        root.getChildren().forEachValue(1, v -> {
                v.accept(new SuffixTreeVisitor(min_ngrams, 2, false));
            });
    }
}
