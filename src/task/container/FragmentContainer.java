package task.container;

import task.OutputFragment;
import index.SuffixTree;

public class FragmentContainer extends TaskContainer {
    public void accept(String t, SuffixTree u) {
        addTask(new OutputFragment(u, t));
    }
}
