package exec.task.generator;

import util.SuffixTree;
import exec.task.TermSelector;

public class SelectionGenerator extends TaskGenerator {
    private SuffixTree root;

    public SelectionGenerator(SuffixTree root) {
        super();

        this.root = root;
    }

    public void accept(String t, SuffixTree u) {
        addTask(new TermSelector(root, t));
    }
}
