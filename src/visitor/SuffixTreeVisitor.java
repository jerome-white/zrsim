package visitor;

import tree.SuffixTree;

public interface SuffixTreeVisitor {
    public SuffixTreeVisitor spawn(Character gram);
    public void visit(SuffixTree node);
}
