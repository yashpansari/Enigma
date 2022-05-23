package enigma;

import java.util.HashMap;
import java.util.Locale;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

/** The suite of all JUnit tests for the Machine class.
 *  @author Yash Pansari
 */
public class MachineTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTS ***** */

    private static final Alphabet AZ = new Alphabet(TestUtils.UPPER_STRING);

    private static final HashMap<String, Rotor> ROTORS = new HashMap<>();

    static {
        HashMap<String, String> nav = TestUtils.NAVALA;
        ROTORS.put("B", new Reflector("B", new Permutation(nav.get("B"), AZ)));
        ROTORS.put("Beta",
                new FixedRotor("Beta",
                        new Permutation(nav.get("Beta"), AZ)));
        ROTORS.put("III",
                new MovingRotor("III",
                        new Permutation(nav.get("III"), AZ), "V"));
        ROTORS.put("IV",
                new MovingRotor("IV", new Permutation(nav.get("IV"), AZ),
                        "J"));
        ROTORS.put("I",
                new MovingRotor("I", new Permutation(nav.get("I"), AZ),
                        "Q"));
    }

    private static final String[] ROTORS1 = { "B", "Beta", "III", "IV", "I" };
    private static final String SETTING1 = "AXLE";

    private Machine mach1() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS.values());
        mach.insertRotors(ROTORS1);
        mach.setRotors(SETTING1);
        return mach;
    }

    @Test
    public void testInsertRotors() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS.values());
        mach.insertRotors(ROTORS1);
        assertEquals(5, mach.numRotors());
        assertEquals(3, mach.numPawls());
        assertEquals(AZ, mach.alphabet());
        assertEquals(ROTORS.get("B"), mach.getRotor(0));
        assertEquals(ROTORS.get("Beta"), mach.getRotor(1));
        assertEquals(ROTORS.get("III"), mach.getRotor(2));
        assertEquals(ROTORS.get("IV"), mach.getRotor(3));
        assertEquals(ROTORS.get("I"), mach.getRotor(4));
    }

    @Test
    public void testSetRotors() {
        Alphabet upperTest = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Machine machine = new Machine(upperTest, 5, 3, ROTORS.values());
        String[] testNames = {"B", "Beta", "III", "IV", "I"};
        machine.insertRotors(testNames);
        machine.setRotors("AXLE");
        assertEquals("Wrong setting at 1", 'A', machine.getRotor(1).setting());
        assertEquals("Wrong setting at 2", 'X', machine.getRotor(2).setting());
        assertEquals("Wrong setting at 3", 'L', machine.getRotor(3).setting());
        assertEquals("Wrong setting at 4", 'E', machine.getRotor(4).setting());
    }

    @Test
    public void testAdvance() {
        Alphabet upperTest = AZ;
        HashMap<String, Rotor> rotors2 = new HashMap<>();
        String rp;
        rp = "(AZ) (BY) (CX) (DW) (EV) (FU) (GT) (HS) (IR) (JQ) (KP) (LO) (MN)";
        String iperm = "(wordle) (is) (fun)".toUpperCase(Locale.ROOT);
        String iiperm = "(tears) (boing) (lucky)".toUpperCase(Locale.ROOT);
        String iiiperm = "(QUACK) (FROZE) (TWINS) (GLYPH)";
        rotors2.put("B", new Reflector("B", new Permutation(rp, AZ)));
        rotors2.put("I",
                new MovingRotor("I",
                        new Permutation(iperm, AZ), "A"));
        rotors2.put("II",
                new MovingRotor("II", new Permutation(iiperm, AZ),
                        "B"));
        rotors2.put("III",
                new MovingRotor("III", new Permutation(iiiperm, AZ),
                        "M"));
        Machine machine = new Machine(upperTest, 4, 3, rotors2.values());
        String[] testNames = {"B", "III", "II", "I"};
        machine.insertRotors(testNames);
        machine.setRotors("MAA");
        assertEquals("Wrong setting at 1", 'M', machine.getRotor(1).setting());
        assertEquals("Wrong setting at 2", 'A', machine.getRotor(2).setting());
        assertEquals("Wrong setting at 3", 'A', machine.getRotor(3).setting());
        machine.advanceRotors();
        assertEquals("Wrong setting at 1", 'M', machine.getRotor(1).setting());
        assertEquals("Wrong setting at 2", 'B', machine.getRotor(2).setting());
        assertEquals("Wrong setting at 3", 'B', machine.getRotor(3).setting());
        machine.advanceRotors();
        assertEquals("Wrong setting at 1", 'N', machine.getRotor(1).setting());
        assertEquals("Wrong setting at 2", 'C', machine.getRotor(2).setting());
        assertEquals("Wrong setting at 3", 'C', machine.getRotor(3).setting());
        machine.advanceRotors();
        assertEquals("Wrong setting at 1", 'N', machine.getRotor(1).setting());
        assertEquals("Wrong setting at 2", 'C', machine.getRotor(2).setting());
        assertEquals("Wrong setting at 3", 'D', machine.getRotor(3).setting());
    }

    @Test
    public void testConvertChar() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(YF) (HZ)", AZ));
        assertEquals(25, mach.convert(24));
    }

    @Test
    public void testConvertMsg() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", AZ));
        assertEquals("QVPQSOKOILPUBKJZPISFXDW",
                mach.convert("FROMHISSHOULDERHIAWATHA"));
        Alphabet upperTest = AZ;
        HashMap<String, Rotor> rotors2 = new HashMap<>();
        String rp;
        rp = "(AZ) (BY) (CX) (DW) (EV) (FU) (GT) (HS) (IR) (JQ) (KP) (LO) (MN)";
        String iperm = "(wordle) (is) (fun)".toUpperCase(Locale.ROOT);
        String iiperm = "(tears) (boing) (lucky)".toUpperCase(Locale.ROOT);
        String iiiperm = "(QUACK) (FROZE) (TWINS) (GLYPH)";
        rotors2.put("B", new Reflector("B", new Permutation(rp, AZ)));
        rotors2.put("I",
                new MovingRotor("I",
                        new Permutation(iperm, AZ), "A"));
        rotors2.put("II",
                new MovingRotor("II", new Permutation(iiperm, AZ),
                        "B"));
        rotors2.put("III",
                new MovingRotor("III", new Permutation(iiiperm, AZ),
                        "M"));
        Machine machine = new Machine(upperTest, 4, 3, rotors2.values());
        machine.setPlugboard(new Permutation("(AZ) (MN)", AZ));
        String[] testNames = {"B", "III", "II", "I"};
        machine.insertRotors(testNames);
        machine.setRotors("MAA");
        assertEquals(machine.convert("AJC"), "WOS");
    }
}
