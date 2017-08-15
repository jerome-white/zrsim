package exec.task.generator;

import java.nio.file.Path;
import java.util.List;
import java.util.Iterator;

import util.SuffixTree;
import exec.task.OutputFragment;

public class FragmentGenerator extends TaskGenerator {
    private Iterator<Path> iterator;

    public FragmentGenerator() {
        super();
    }

    public void accept(String t, SuffixTree u) {
        addTask(new OutputFragment(u, t));
    }
}
