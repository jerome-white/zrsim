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

        List<Callable<ForwardIndex>> t1 =
            new LinkedList<Callable<ForwardIndex>>();

        /*
         * Collect terms
         */
        LogAgent.LOGGER.info("Term collection");

        for (int i = 0; i < workers; i++) {
            t1.add(new TokenCollector(posting, i, workers));
        }

        try {
            List<Future<ForwardIndex>> result = executors.invokeAll(t1);

            ForwardIndex index = new ForwardIndex();

            for (Future<ForwardIndex> future : result) {
                index.fold(future.get());
            }

            /*
             * Create a database to give the terms nice names
             */
            LogAgent.LOGGER.info("Term database");

            TermNamer termNamer = new PseudoTerm(index.tokenIterator());

            /*
             * Save
             */
            LogAgent.LOGGER.info("Save to disk");

            List<Callable<String>> t2 = new LinkedList<Callable<String>>();
            for (String document : index.documents()) {
                t2.add(new TermCreator(index, document, termNamer, output));
            }
            executors.invokeAll(t2);
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }
        catch (ExecutionException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        executors.shutdown();

        LogAgent.LOGGER.info("Complete");
    }
}
