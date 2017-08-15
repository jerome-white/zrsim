package exec.task.generator;

import util.SuffixTree;
import exec.task.TermSelector;

public class SelectionGenerator extends TaskGenerator {
    public SelectionGenerator() {
        super();
    }

    public void accept(String t, SuffixTree u) {
        addTask(new TermSelector(u, t));
    }
}
