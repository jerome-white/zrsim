package simulate;

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
import java.util.logging.Logger;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;

import tree.SuffixTree;
import util.TermSelector;
import util.DocumentParser;
import util.OutputFragment;

public class PoolManager extends Manager {
    private int pool;
    private ExecutorService executors;

    private List<Callable<String>> tasks;

    public PoolManager(int min_gram, int pool) {
	super(min_gram);
	this.pool = pool;
	executors = Executors.newFixedThreadPool(pool);

	tasks = new ArrayList<Callable<String>>();
    }

    public PoolManager(int min_gram) {
	this(min_gram, Runtime.getRuntime().availableProcessors());
    }

    public void addDocuments(Path corpus, int max_ngram) {
        LOGGER.info("Adding terms");

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
    }

    public void selectTerms() {
        LOGGER.info("Term selection");

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
    }

    public void generate(Path output) {
        Map<String, String> env = System.getenv();
        Path tmpdir = env.containsKey(SLURM_JOBTMP) ?
            Paths.get(env.get(SLURM_JOBTMP)) : null;

        List<Path> fragments = new ArrayList<Path>();
	try {
	    for (int i = 0; i < pool; i++) {
		String fname = String.valueOf(i);
		Path tmpfile = (tmpdir == null) ?
		    Files.createTempFile(fname, null) :
		    Files.createTempFile(tmpdir, fname, null);
		fragments.add(tmpfile);
	    }
	}
	catch (IOException ex) {
	    throw new UncheckedIOException(ex);
	}

        LOGGER.info("Terms to disk");

	int i = 0;
	tasks.clear();
	for (Map.Entry<String, SuffixTree> entry :
		 suffixTree.getChildren().entrySet()) {
	    String ngram = entry.getKey();
	    SuffixTree tree = entry.getValue();

	    Path path = fragments.get(i % fragments.size());
	    i++;

	    tasks.add(new OutputFragment(path, tree, ngram));
	}

	try {
	    executors.invokeAll(tasks);
	}
	catch (InterruptedException ex) {
	    throw new UndeclaredThrowableException(ex);
	}

        LOGGER.info("Disk consolidation");

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
    }
}
