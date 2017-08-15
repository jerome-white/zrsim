package exec;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import util.LogAgent;
import util.SuffixTree;
import exec.task.DocumentParser;
import exec.task.generator.FragmentGenerator;
import exec.task.generator.SelectionGenerator;

public class Simulator {
    public final static String SLURM_JOBTMP = "SLURM_JOBTMP";

    public static void main(String[] args) {
        /*
         *
         */
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

        /*
         *
         */
        LogAgent.LOGGER.info("Adding terms");

        List<Callable<String>> tasks = new LinkedList<Callable<String>>();
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

        /*
         *
         */
        LogAgent.LOGGER.info("Term selection");

        SelectionGenerator selector = new SelectionGenerator();
        suffixTree.forEachChild(selector);
        try {
            executors.invokeAll(selector.getTasks());
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        /*
         *
         */
        LogAgent.LOGGER.info("Terms to disk");

        /*
         * Create temporary (fragment) files.
         */
        Map<String, String> env = System.getenv();
        Path tmpdir = env.containsKey(SLURM_JOBTMP) ?
            Paths.get(env.get(SLURM_JOBTMP)) : null;

        LinkedList<Path> tmpfiles = new LinkedList<Path>();

        try {
            for (int i = 0; i < workers; i++) {
                Path tmpfile = (tmpdir == null) ?
                    Files.createTempFile(null, null) :
                    Files.createTempFile(tmpdir, null, null);
                tmpfiles.add(tmpfile);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        FragmentGenerator fragmentor = new FragmentGenerator(tmpfiles);
        suffixTree.forEachChild(fragmentor);

        try {
            executors.invokeAll(fragmentor.getTasks());
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
            for (Path input : tmpfiles) {
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
