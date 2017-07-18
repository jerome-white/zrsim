package simulate;

import java.nio.file.Path;
import java.util.logging.Logger;

import tree.SuffixTree;

public abstract class Manager {
    public final static Logger LOGGER =
        Logger.getLogger(Manager.class.getName());
    protected final static String SLURM_JOBTMP = "SLURM_JOBTMP";

    protected SuffixTree suffixTree;

    public Manager(int min_ngram) {
        suffixTree = new SuffixTree(min_ngram);
    }

    public void shutdown() {}

    abstract public void addDocuments(Path corpus, int max_ngram);
    abstract public void selectTerms();
    abstract public void generate(Path output);
}
