package test.unit;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import util.StringPartition;

public class StringPartitionTest {
    @Test
    public void testSingle() {
        String string = "a";
        StringPartition partition = new StringPartition(string,
                                                        string.length());

        assertEquals(partition.head, string);
        assertTrue(partition.tail.isEmpty());
    }

    @Test
    public void testHead() {
        String string = "abcd";
        StringPartition partition = new StringPartition(string, 4);

        assertEquals(partition.head, string);
        assertTrue(partition.tail.isEmpty());
    }

    @Test
    public void testTail() {
        String string = "abcde";
        StringPartition partition = new StringPartition(string, 4);

        assertEquals(partition.head, "abcd");
        assertEquals(partition.tail, "e");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLong() {
        String string = "a";
        StringPartition partition = new StringPartition(string,
                                                        string.length() + 1);
    }
}
