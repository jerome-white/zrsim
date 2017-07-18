package simulate.task;

import java.io.PrintStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import tree.SuffixTree;
import visitor.OutputVisitor;

public class OutputFragment implements Callable<String> {
    private Path path;
    private SuffixTree root;
    private String ngram;
    private int appearances;
    private boolean redundants;

    public OutputFragment(Path path,
			  SuffixTree root,
			  String ngram,
			  int appearances,
			  boolean redundants) {
	this.path = path;
	this.root = root;
	this.ngram = ngram;
	this.appearances = appearances;
	this.redundants = redundants;
    }

    public OutputFragment(Path path, SuffixTree root, String ngram) {
	this(path, root, ngram, 2, false);
    }

    public String call() {
	try (PrintStream printStream =
	     new PrintStream(Files.newOutputStream(path), true)) {
	    OutputVisitor visitor =
		new OutputVisitor(ngram, appearances, redundants, printStream);
	    root.accept(visitor);
	}
	catch (IOException ex) {
	    throw new UncheckedIOException(ex);
	}

	return ngram;
    }
}
