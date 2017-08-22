package exec;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.lang.reflect.UndeclaredThrowableException;

import util.LogAgent;
import util.namer.TermNamer;
import util.namer.PseudoTermNamer;
import exec.task.TermCreator;
import exec.task.TokenCollector;
import index.ForwardIndex;

public class MakeTerms {
    public static void main(String[] args) {
        Path posting = Paths.get(args[0]);
        int workers = Integer.parseInt(args[1]);
        Path output = Paths.get(args[2]);

        LogAgent.LOGGER.setLevel(Level.INFO);

        ExecutorService executors = Executors.newFixedThreadPool(workers);

        List<Callable<ForwardIndex>> indexTasks =
            new LinkedList<Callable<ForwardIndex>>();

        /*
         * Collect terms
         */
        LogAgent.LOGGER.info("Term collection");

        for (int i = 0; i < workers; i++) {
            indexTasks.add(new TokenCollector(posting, i, workers));
        }

        ForwardIndex index = null;

        try {
            List<Future<ForwardIndex>> result =
                executors.invokeAll(indexTasks);

            /*
             * Combine the indices
             */
            LogAgent.LOGGER.info("Combining indexes");

            for (Future<ForwardIndex> future : result) {
                ForwardIndex i = future.get();
                if (index == null) {
                    index = i;
                }
                else {
                    index.fold(i);
                }
            }
        }
        catch (InterruptedException | ExecutionException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        /*
         * Create a database to give the terms nice names
         */
        LogAgent.LOGGER.info("Term database");

        TermNamer termNamer = new PseudoTermNamer(index.tokenIterator());

        /*
         * Save
         */
        LogAgent.LOGGER.info("Save to disk");

        List<Callable<String>> creationTasks =
            new LinkedList<Callable<String>>();
        for (String document : index.documents()) {
            TermCreator creator = new TermCreator(index,
                                                  document,
                                                  termNamer,
                                                  output);
            creationTasks.add(creator);
        }
        try {
            executors.invokeAll(creationTasks);
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        executors.shutdown();

        LogAgent.LOGGER.info("Complete");
    }
}
