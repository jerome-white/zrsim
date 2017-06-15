import java.io.PrintStream;

public class OutputVisitor implements SuffixTreeVisitor {
    private int appearances;
    private int length;
    private boolean redundants;
    private String ngram;
    private PrintStream printStream;

    public OutputVisitor(String ngram,
                         int length,
                         int appearances,
                         boolean redundants,
                         PrintStream printStream) {
	this.ngram = ngram;        
	this.length = length;
	this.appearances = appearances;
	this.redundants = redundants;
        this.printStream = printStream;
    }

    public OutputVisitor(String ngram,
                         int length,
                         int appearances,
                         boolean redundants) {
        this(ngram, length, appearances, redundants, System.out);
    }

    public OutputVisitor(String ngram) {
        this(ngram, 0, 0, true);
    }

    public SuffixTreeVisitor spawn(Character gram) {
        return new OutputVisitor(this.ngram + gram,
                                 length,
                                 appearances,
                                 redundants,
                                 printStream);
    }

    public void visit(SuffixTree node) {
        if (ngram.length() >= length &&
            node.getLocations().size() >= appearances &&
            (redundants || !redundants && !node.isRedundant())) {
            for (Location location : node.getLocations()) {
                printStream.println(new Token(ngram, location));
            }
        }
    }
}
