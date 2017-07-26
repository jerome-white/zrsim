package simulate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class Simulator {
    public static void main(String[] args) {
        Manager.LOGGER.setLevel(Level.INFO);

        Path corpus = Paths.get(args[0]);
        int min_ngram = Integer.parseInt(args[1]);
        int max_ngram = Integer.parseInt(args[2]);
        Path output = Paths.get(args[3]);
        int workers = Integer.parseInt(args[4]);

        Manager.LOGGER.info("Begin: " + min_ngram + " -- " + max_ngram);

        // Manager manager = new StreamManager(min_ngram);

        Manager manager = (workers > 0) ?
            new PoolManager(min_ngram, workers) :
            new PoolManager(min_ngram);

        manager.addDocuments(corpus, max_ngram);
        manager.selectTerms();
        manager.generate(output);
        manager.shutdown();

        Manager.LOGGER.info("Complete");
    }
}
