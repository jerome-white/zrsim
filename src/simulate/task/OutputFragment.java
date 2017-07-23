package simulate.task;

import java.io.PrintStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.Callable;

import tree.SuffixTree;
import visitor.OutputVisitor;

public class OutputFragment implements Callable<String> {
    private int appearances;
    private boolean redundants;

    private Path path;
    private Path tmpfile;
    private SuffixTree root;
    private Collection<String> ngrams;

    public OutputFragment(SuffixTree root,
			  Collection<String> ngrams,
			  Path tmpfile,
			  int appearances,
			  boolean redundants) {
	this.root = root;
	this.ngrams = ngrams;
	this.tmpfile = tmpfile;
	this.appearances = appearances;
	this.redundants = redundants;
    }

    public OutputFragment(SuffixTree root,
			  Collection<String> ngrams,
			  Path tmpfile) {
	this(root, ngrams, tmpfile, 2, false);
    }

    public String call() {
	StringBuffer results = new StringBuffer();

	try (PrintStream printStream =
	     new PrintStream(Files.newOutputStream(tmpfile), true)) {
	    for (String n : ngrams) {
		OutputVisitor visitor =
		    new OutputVisitor(n, appearances, redundants, printStream);
		root.accept(visitor);
		results
		    .append(n)
		    .append(",");
	    }
	}
	catch (IOException ex) {
	    throw new UncheckedIOException(ex);
	}

	return results.toString();
    }
}
