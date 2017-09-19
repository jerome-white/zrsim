package task.container;

import java.util.List;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.concurrent.Callable;

import index.SuffixTree;

public abstract class TaskContainer implements BiConsumer<String, SuffixTree> {
    protected List<Callable<String>> tasks =
        new LinkedList<Callable<String>>();

    public List<Callable<String>> getTasks() {
        return tasks;
    }
}
