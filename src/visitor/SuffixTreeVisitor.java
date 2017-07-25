package visitor;

import util.SuffixTree;

public interface SuffixTreeVisitor {
    public SuffixTreeVisitor spawn(String ngram);
    public void visit(SuffixTree node);
}
