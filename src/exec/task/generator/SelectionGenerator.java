package exec.task.generator;

import exec.task.TermSelector;
import index.SuffixTree;

public class SelectionGenerator extends TaskGenerator {
    private SuffixTree root;

    public SelectionGenerator(SuffixTree root) {
        super();

        this.root = root;
    }

    public void accept(String t, SuffixTree u) {
        addTask(new TermSelector(root, u, t));
    }
}
