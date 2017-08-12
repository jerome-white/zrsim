package exec;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.lang.reflect.UndeclaredThrowableException;

import util.SubList;
import util.LogAgent;
import util.TermNamer;
import util.PseudoTerm;
import util.ForwardIndex;
import exec.task.TermCreator;
import exec.task.TokenCollector;

public class MakeTerms {
    public static void main(String[] args) {
        Path posting = Paths.get(args[0]);
        int workers = Integer.parseInt(args[1]);
        Path output = Paths.get(args[2]);

        LogAgent.LOGGER.setLevel(Level.INFO);

        ExecutorService executors = Executors.newFixedThreadPool(workers);

        List<Callable<String>> tasks = new ArrayList<Callable<String>>();

        /*
         * Collect terms
         */
        ForwardIndex index = new ForwardIndex();

        for (int i = 0; i < workers; i++) {
            tasks.add(new TokenCollector(index, posting, i));
        }

        try {
            executors.invokeAll(tasks);
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        /*
         * Create a database to give the terms nice names
         */
        TermNamer pt = new PseudoTerm(index.termIterator());

        /*
         * Save
         */
        tasks.clear();

        List<String> documents = new ArrayList<String>(index.documents());
        for (List<String> subdocs : new SubList<String>(documents, workers)) {
            tasks.add(new TermCreator(index, subdocs, pt, output));
        }

        try {
            executors.invokeAll(tasks);
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }
}
