package exec.task.generator;

import java.util.List;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.concurrent.Callable;

import index.SuffixTree;

public abstract class TaskGenerator implements BiConsumer<String, SuffixTree> {
    private List<Callable<String>> tasks;

    public TaskGenerator() {
        tasks = new LinkedList<Callable<String>>();
    }

    public void addTask(Callable<String> task) {
        tasks.add(task);
    }

    public List<Callable<String>> getTasks() {
        return tasks;
    }
}
