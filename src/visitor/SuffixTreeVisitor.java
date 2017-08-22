package visitor;

import index.SuffixTree;

public interface SuffixTreeVisitor {
    public SuffixTreeVisitor spawn(String ngram);
    public void visit(SuffixTree node);
}
