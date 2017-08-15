package exec.task.generator;

import util.SuffixTree;
import exec.task.OutputFragment;

public class FragmentGenerator extends TaskGenerator {
    public FragmentGenerator() {
        super();
    }

    public void accept(String t, SuffixTree u) {
        addTask(new OutputFragment(u, t));
    }
}
