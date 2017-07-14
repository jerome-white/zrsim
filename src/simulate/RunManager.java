package simulate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class RunManager {
    public static void main(String[] args) {
        Manager.LOGGER.setLevel(Level.INFO);

        Path corpus = Paths.get(args[0]);
        int min_ngram = Integer.parseInt(args[1]);
        int max_ngram = Integer.parseInt(args[2]);
        Path output = Paths.get(args[3]);

        Manager.LOGGER.info("Begin");

        // Manager manager = new StreamManager(min_ngram);
        Manager manager = new PoolManager(min_ngram);

        manager.addDocuments(corpus, max_ngram);
        manager.selectTerms();
        manager.generate(output);

        Manager.LOGGER.info("Complete");
    }
}
