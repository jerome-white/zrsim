package test.unit;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import util.SubList;

public class SubListTest {
    @Test
    public void testSingle() {
	List<Integer> list = new ArrayList<Integer>();
	for (int i = 0; i < 10; i++) {
	    list.add(i);
	}
	
	SubList subList = new SubList(list);
	for i in 
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
