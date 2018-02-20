package test.unit;

import java.util.List;
import java.util.StringJoiner;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import util.entity.Token;

public class TokenTest {
    @Test
    public void testTriple() {
	StringJoiner joiner = new StringJoiner(Token.DELIMITER);
	for (int i = 0; i < 3; i++) {
	    joiner.add(String.valueOf(i));
	}
	List<String> strings = Token.fromString(joiner.toString());

        assertEquals(strings.size(), 3);
    }

    @Test
    public void testMultipleOkay() {
	StringJoiner joiner = new StringJoiner(Token.DELIMITER);
	for (int i = 0; i < 10; i++) {
	    joiner.add(String.valueOf(i));
	}
	List<String> strings = Token.fromString(joiner.toString());

        assertEquals(strings.size(), 3);
    }

    @Test
    public void testMultipleCorrect() {
	StringJoiner center = new StringJoiner(Token.DELIMITER);

	for (int i = 1; i < 10; i++) {
	    center.add(String.valueOf(i));
	}

	StringJoiner container = new StringJoiner(Token.DELIMITER);
	container
	    .add("0")
	    .merge(center)
	    .add("10");

	List<String> strings = Token.fromString(container.toString());

        assertEquals(strings.get(0), "0");
	assertEquals(strings.get(1), center.toString());
        assertEquals(strings.get(2), "10");
    }
}
