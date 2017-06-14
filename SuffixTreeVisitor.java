public class SuffixTreeVisitor {
    private int appearances;
    private int length;
    private boolean redundants;
    private String ngram;
    
    public SuffixTreeVisitor(int length,
                             int appearances,
                             boolean redundants,
                             String ngram) {
	this.length = length;
	this.appearances = appearances;
	this.redundants = redundants;
	this.ngram = ngram;
    }

    public SuffixTreeVisitor(int length, int appearances, boolean redundants) {
        this(length, appearances, redundants, new String());
    }

    public SuffixTreeVisitor() {
        this(0, 0, true);
    }

    public SuffixTreeVisitor spawn(Character gram) {
        String ngram = new String(this.ngram + gram);
        
        return new SuffixTreeVisitor(length,
                                     appearances,
                                     redundants,
                                     ngram);
    }

    public void visit(SuffixTree node) {
        if (ngram.length() >= length &&
            node.getLocations().size() >= appearances &&
            (redundants || !redundants && !node.isRedundant())) {
            for (Location location : node.getLocations()) {
                System.out.println(new Token(ngram, location));
            }
        }
    }
}
