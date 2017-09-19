package task.container;

import task.OutputFragment;
import index.SuffixTree;

public class FragmentContainer extends TaskContainer {
    public void accept(String t, SuffixTree u) {
        tasks.add(new OutputFragment(u, t));
    }
}
