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
import util.StreamStorageThreadFactory;
import util.keeper.GateKeeper;
import util.keeper.SentenceGateKeeper;
import task.DocumentParser;
import task.container.TaskContainer;
import task.container.FragmentContainer;
import task.container.SelectionContainer;
import index.SuffixTree;

public class NgramExtractor {
    private int workers;
    private int min_ngram;

    private SuffixTree suffixTree;

    public NgramExtractor(int workers, int min_ngram) {
        this.workers = workers;
        this.min_ngram = min_ngram;

        suffixTree = new SuffixTree(min_ngram);
    }

    public void populate(Path corpus, int max_ngram) {
        LogAgent.LOGGER.info("Adding terms");

        int threads = Math.round(workers / 2);
        ExecutorService executors = Executors.newFixedThreadPool(threads);

        List<Callable<String>> tasks = new LinkedList<Callable<String>>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(corpus)) {
            for (Path file : stream) {
                GateKeeper gateKeeper = new SentenceGateKeeper();
                DocumentParser parser = new DocumentParser(suffixTree,
                                                           file,
                                                           min_ngram,
                                                           max_ngram,
                                                           gateKeeper);
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

        executors.shutdown();
    }

    public void prune() {
        LogAgent.LOGGER.info("Term selection");

        ExecutorService executors = Executors.newFixedThreadPool(workers);

        TaskContainer container = new SelectionContainer(suffixTree);
        suffixTree.forEachChild(container);
        try {
            executors.invokeAll(container.getTasks());
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        executors.shutdown();
    }

    private void todisk(StreamStorageThreadFactory factory) {
        LogAgent.LOGGER.info("Terms to disk");

        TaskContainer container = new FragmentContainer();
        suffixTree.forEachChild(container);

        ExecutorService executors =
            Executors.newFixedThreadPool(workers, factory);
        try {
            executors.invokeAll(container.getTasks());
        }
        catch (InterruptedException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        executors.shutdown();
    }

    private void consolidate(Path output, StreamStorageThreadFactory factory) {
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
    }

    public void dump(Path output, Path tmpdir) {
        StreamStorageThreadFactory factory =
            new StreamStorageThreadFactory(tmpdir);

        todisk(factory);
        consolidate(output, factory);
    }

    public static void main(String[] args) {
        LogAgent.LOGGER.setLevel(Level.INFO);

        /*
         *
         */
        Path corpus = Paths.get(args[0]);
        int min_ngram = Integer.parseInt(args[1]);
        int max_ngram = Integer.parseInt(args[2]);
        Path output = Paths.get(args[3]);
        int workers = Integer.parseInt(args[4]);
        Path tmpdir = Paths.get(args[5]);

        int procs = Runtime.getRuntime().availableProcessors();
        if (workers > procs) {
            workers = procs;
        }

        NgramExtractor extractor = new NgramExtractor(workers, min_ngram);

        /*
         *
         */
        LogAgent.LOGGER.info("Begin: " + min_ngram + " -- " + max_ngram);

        extractor.populate(corpus, max_ngram);
        extractor.prune();
        extractor.dump(output, tmpdir);

        LogAgent.LOGGER.info("Complete");
    }
}
