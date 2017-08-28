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
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import util.LogAgent;
import exec.task.DocumentParser;
import exec.task.generator.TaskGenerator;
import exec.task.generator.FragmentGenerator;
import exec.task.generator.SelectionGenerator;
import index.SuffixTree;

public class Simulator {
    public static void main(String[] args) {
        /*
         *
         */
        Path corpus = Paths.get(args[0]);
        int min_ngram = Integer.parseInt(args[1]);
        int max_ngram = Integer.parseInt(args[2]);
        Path output = Paths.get(args[3]);
        int workers = Integer.parseInt(args[4]);
        Path tmpdir = Paths.get(args[5]);

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
                DocumentParser parser =
                    new DocumentParser(suffixTree, file, min_ngram, max_ngram);
                tasks.add(parser);
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

        TaskGenerator generator = new SelectionGenerator(suffixTree);
        suffixTree.forEachChild(generator);
        try {
            executors.invokeAll(generator.getTasks());
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        executors.shutdown();

        /*
         *
         */
        LogAgent.LOGGER.info("Terms to disk");

        generator = new FragmentGenerator();
        suffixTree.forEachChild(generator);

        StreamStorageThreadFactory factory =
            new StreamStorageThreadFactory(tmpdir);
        executors = Executors.newFixedThreadPool(workers, factory);
        try {
            executors.invokeAll(generator.getTasks());
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
            for (Path input : factory) {
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
