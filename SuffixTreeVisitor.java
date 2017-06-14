import java.io.PrintStream;

public class SuffixTreeVisitor {
    private int appearances;
    private int length;
    private boolean redundants;
    private String ngram;
    private PrintStream printStream;

    public SuffixTreeVisitor(int length,
                             int appearances,
                             boolean redundants,
                             PrintStream printStream,
                             String ngram) {
	this.length = length;
	this.appearances = appearances;
	this.redundants = redundants;
	this.ngram = ngram;
        this.printStream = printStream;
    }

    public SuffixTreeVisitor(int length,
                             int appearances,
                             boolean redundants,
                             PrintStream printStream) {
        this(length, appearances, redundants, printStream, new String());
    }

    public SuffixTreeVisitor(int length, int appearances, boolean redundants) {
        this(length, appearances, redundants, System.out);
    }

    public SuffixTreeVisitor() {
        this(0, 0, true);
    }

    public SuffixTreeVisitor spawn(Character gram) {
        String ngram = new String(this.ngram + gram);

        return new SuffixTreeVisitor(length,
                                     appearances,
                                     redundants,
                                     printStream,
                                     ngram);
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
