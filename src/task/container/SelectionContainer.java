package task.container;

import task.TermSelector;
import index.SuffixTree;

public class SelectionContainer extends TaskContainer {
    private SuffixTree root;

    public SelectionContainer(SuffixTree root) {
        super();

        this.root = root;
    }

    public void accept(String t, SuffixTree u) {
        addTask(new TermSelector(root, u, t));
    }
}
