package test.unit;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import util.StringPartition;

public class StringPartitionTest {
    @Test
    public void testHead() {
        String abcd = "abcd";
        StringPartition partition = new StringPartition(abcd, 4);
        assertEquals(partition.head, abcd);
        assertTrue(partition.tail.isEmpty());
    }

    @Test
    public void testTail() {
        String abcde = "abcde";
        StringPartition partition = new StringPartition(abcde, 4);
        assertEquals(partition.head, "abcd");
        assertEquals(partition.tail, "e");
    }
}
