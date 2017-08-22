package exec.task.generator;

import exec.task.OutputFragment;
import index.SuffixTree;

public class FragmentGenerator extends TaskGenerator {
    public FragmentGenerator() {
        super();
    }

    public void accept(String t, SuffixTree u) {
        addTask(new OutputFragment(u, t));
    }
}
