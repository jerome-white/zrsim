package test.unit;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

import util.SuffixTree;

public class SuffixTreeTest {
    private SuffixTree suffixTree;
    private final String key = "abcd";
    private final String document = "Test";
    private final int offset = 0;

    @Before
    public void setup() {
        suffixTree = new SuffixTree(key.length());
        suffixTree.add(key, document, offset);
    }

    @Test
    public void testCorrectLocationSize() {
        SuffixTree found = suffixTree.find(key);
        assertEquals(found.appearances(), 1);
    }

    // @Test
    // public void testCorrectLocation() {
    //     SuffixTree found = suffixTree.find(key);
    //     Sorte<String>
    //     assertEquals(found.getLocations().first(), location);
    // }
}
