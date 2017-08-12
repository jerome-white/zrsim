package exec;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UncheckedIOException;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.lang.Void;
import java.lang.IllegalStateException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import util.SubList;
import util.LogAgent;
import util.SuffixTree;
import exec.task.TermSelector;
import exec.task.DocumentParser;
import exec.task.OutputFragment;

public class Simulator {
    public final static String SLURM_JOBTMP = "SLURM_JOBTMP";

    public static void main(String[] args) {
        Path corpus = Paths.get(args[0]);
        int min_ngram = Integer.parseInt(args[1]);
        int max_ngram = Integer.parseInt(args[2]);
        Path output = Paths.get(args[3]);
        int workers = Integer.parseInt(args[4]);

        LogAgent.LOGGER.setLevel(Level.INFO);
        LogAgent.LOGGER.info("Begin: " + min_ngram + " -- " + max_ngram);

        int procs = Runtime.getRuntime().availableProcessors();
        if (workers > procs) {
            workers = procs;
        }
        ExecutorService executors = Executors.newFixedThreadPool(workers);

        SuffixTree suffixTree = new SuffixTree(min_ngram);
        List<Callable<String>> tasks = new ArrayList<Callable<String>>();

        /*
         *
         */
        LogAgent.LOGGER.info("Adding terms");

        tasks.clear();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(corpus)) {
            for (Path file : stream) {
                tasks.add(new DocumentParser(suffixTree, max_ngram, file));
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        try {
            executors.invokeAll(tasks);
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        LogAgent.LOGGER.info("Term selection");

        tasks.clear();
        for (String ngram : suffixTree.getChildren().keySet()) {
            tasks.add(new TermSelector(suffixTree, ngram));
        }

        try {
            executors.invokeAll(tasks);
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        /*
         *
         */
        List<String> children = new ArrayList<String>();
        for (Map.Entry<String, SuffixTree> entry :
                 suffixTree.getChildren().entrySet()) {
            children.add(entry.getKey());
        }

        /*
         *
         */
        Map<String, String> env = System.getenv();
        Path tmpdir = env.containsKey(SLURM_JOBTMP) ?
            Paths.get(env.get(SLURM_JOBTMP)) : null;

        List<Path> fragments = new ArrayList<Path>();

        /*
         *
         */
        LogAgent.LOGGER.info("Terms to disk");

        tasks.clear();
        for (List<String> ngrams : new SubList<String>(children, workers)) {
            try {
                Path tmpfile = (tmpdir == null) ?
                    Files.createTempFile(null, null) :
                    Files.createTempFile(tmpdir, null, null);
                tasks.add(new OutputFragment(suffixTree, ngrams, tmpfile));
                fragments.add(tmpfile);
            }
            catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        try {
            executors.invokeAll(tasks);
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        /*
         *
         */
        LogAgent.LOGGER.info("Disk consolidation");

        try (FileChannel dest =
             FileChannel.open(output,
                              StandardOpenOption.WRITE,
                              StandardOpenOption.CREATE,
                              StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Path input : fragments) {
                try (FileChannel src =
                     FileChannel.open(input,
                                      StandardOpenOption.DELETE_ON_CLOSE)) {
                    dest.transferFrom(src, dest.size(), src.size());
                }
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        executors.shutdown();

        LogAgent.LOGGER.info("Complete");
    }
}
