package test.unit;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

import util.Location;
import tree.SuffixTree;

public class SuffixTreeTest {
    private SuffixTree suffixTree;
    private final String key = "abcd";
    private final Location location = new Location("Test", 0);

    @Before
    public void setup() {
        suffixTree = new SuffixTree(key.length());
        suffixTree.add(key, location);
    }

    @Test
    public void testCorrectLocationSize() {
        SuffixTree found = suffixTree.find(key);
        assertEquals(found.getLocations().size(), 1);
    }

    @Test
    public void testCorrectLocation() {
        SuffixTree found = suffixTree.find(key);
        assertEquals(found.getLocations().peek(), location);
    }
}
