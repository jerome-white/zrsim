package simulate;

import java.io.File;
import java.io.PrintStream;
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
import java.lang.Long;
import java.lang.Runnable;
import java.lang.IllegalStateException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;

import tree.SuffixTree;
import util.DocumentParser;
import visitor.OutputVisitor;
import visitor.SuffixTreeVisitor;
import visitor.MarkRedundantVisitor;

public class PoolManager extends Manager {
    private ExecutorService executors;

    public PoolManager(int min_gram, int pool) {
	super(min_gram);
	executors = Executors.newFixedThreadPool(pool);
    }

    public PoolManager(int min_gram) {
	this(min_gram, Runtime.getRuntime().availableProcessors());
    }

    private void invoke(Collection<Runnable> tasks) {
	for (Runnable runnable : tasks) {
	    executors.execute(runnable);
	}

	try {
	    executors.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}
	catch (InterruptedException ex) {
	    throw new IllegalStateException(ex);
	}
    }

    public void addDocuments(Path corpus, int max_ngram) {
        LOGGER.info("Adding terms");

	List<Runnable> tasks = new ArrayList<Runnable>();
	DocumentParser parser = new DocumentParser(suffixTree, max_ngram);

	try (DirectoryStream<Path> stream = Files.newDirectoryStream(corpus)) {
	    for (Path file : stream) {
		tasks.add(() -> parser.parse(file));
	    }
	}
	catch (IOException ex) {
	    throw new UncheckedIOException(ex);
	}

	invoke(tasks);
    }

    public void selectTerms() {
        LOGGER.info("Term selection");

	List<Runnable> tasks = new ArrayList<Runnable>();

        suffixTree.getChildren().forEach((k, v) -> {
		SuffixTreeVisitor vs = new MarkRedundantVisitor(k, suffixTree);
		tasks.add(() -> v.accept(vs));
	    });

	invoke(tasks);
    }

    public void generate(Path output) {
	List<Runnable> tasks = new ArrayList<Runnable>();

        Map<String, String> env = System.getenv();
        Path tmpdir = env.containsKey(SLURM_JOBTMP) ?
            Paths.get(env.get(SLURM_JOBTMP)) : null;

        ConcurrentHashMap<String, Path> fragments =
            new ConcurrentHashMap<String, Path>();

        suffixTree.getChildren().forEachKey(1, k -> {
                assert !fragments.containsKey(k);
                try {
                    Path tmpfile = (tmpdir == null) ?
                        Files.createTempFile(k, null) :
                        Files.createTempFile(tmpdir, k, null);
                    fragments.put(k, tmpfile);
                }
                catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

        LOGGER.info("Terms to disk");

        suffixTree.getChildren().forEach((k, v) -> {
                Path path = fragments.get(k);
		tasks.add(() -> {
			try (PrintStream printStream = new
			     PrintStream(Files.newOutputStream(path), true)) {
			    SuffixTreeVisitor visitor =
				new OutputVisitor(k, 2, false, printStream);
			    v.accept(visitor);
			}
			catch (IOException ex) {
			    throw new UncheckedIOException(ex);
			}
		    });
	    });
	invoke(tasks);

        LOGGER.info("Disk consolidation");

        try (FileChannel dest =
             FileChannel.open(output,
                              StandardOpenOption.WRITE,
                              StandardOpenOption.CREATE,
                              StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Path input : fragments.values()) {
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
}
