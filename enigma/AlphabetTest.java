package enigma;

import org.junit.Test;
import static org.junit.Assert.*;

public class AlphabetTest {

    @Test
    public void test1() {
        Alphabet test = new Alphabet("ABCD");
        assertEquals(4, test.size());
        assertTrue(test.contains('A'));
        assertFalse(test.contains('Z'));
        assertEquals('A', test.toChar(0));
        assertEquals('D', test.toChar(3));
        assertEquals(2, test.toInt('C'));
        assertEquals(1, test.toInt('B'));
    }

}
