package exec.task.generator;

import java.nio.file.Path;
import java.util.List;
import java.util.Iterator;

import util.SuffixTree;
import exec.task.OutputFragment;

public class FragmentGenerator extends TaskGenerator {
    private List<Path> tmpfiles;
    private Iterator<Path> iterator;

    public FragmentGenerator(List<Path> tmpfiles) {
        super();

        this.tmpfiles = tmpfiles;
        iterator = tmpfiles.iterator();
    }

    public void accept(String t, SuffixTree u) {
        if (!iterator.hasNext()) {
            iterator = tmpfiles.iterator();
        }

        addTask(new OutputFragment(u, t, iterator.next()));
    }
}
