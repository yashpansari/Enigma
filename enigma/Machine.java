package enigma;

import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Yash Pansari
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors.toArray(new Rotor[allRotors.size()]);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _rotors = new Rotor[_numRotors];
        if (rotors.length != _numRotors) {
            throw new EnigmaException("wrong number of rotors");
        }
        for (int i = 0; i < rotors.length; i++) {
            for (Rotor rotor:_allRotors) {
                if (rotor.name().equals(rotors[i])) {
                    _rotors[i] = rotor;
                    rotor.setNotches(rotor.old());
                }
            }
            if (_rotors[i] == null) {
                throw new EnigmaException(rotors[i] + " has been misnamed.");
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _numRotors - 1) {
            throw new EnigmaException("Incorrect number of settings.");
        }
        for (int i = 1; i < _numRotors; i++) {
            char settingI = setting.charAt(i - 1);
            if (!_alphabet.contains(settingI)) {
                throw new EnigmaException("The setting is not on the wheel.");
            }
            _rotors[i].set(settingI);
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        getRotor(r).setting());
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().invert(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    void advanceRotors() {
        boolean[] stepped = new boolean[numRotors()];
        for (int rotor = 0; rotor < numRotors(); rotor++) {
            stepped[rotor] = false;
        }
        for (int i = 1; i < _numRotors - 1; i++) {
            if (_rotors[i + 1].atNotch() && !stepped[i]
                    && _rotors[i].rotates()) {
                _rotors[i].advance();
                stepped[i] = true;
                if (i != _numRotors - 2 && !stepped[i + 1]
                        && _rotors[i + 1].rotates()) {
                    _rotors[i + 1].advance();
                    stepped[i + 1] = true;
                }
            }
        }
        _rotors[_numRotors - 1].advance();
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _numRotors - 1; i > -1; i--) {
            c = _rotors[i].convertForward(c);
        }
        for (int i = 1; i < _numRotors; i++) {
            c = _rotors[i].convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String cypherText = "";
        for (int i = 0; i < msg.length(); i++) {
            int temp = convert(_alphabet.toInt(msg.charAt(i)));
            cypherText += _alphabet.toChar(temp);
        }
        return cypherText;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Plugboard permutation for my rotors. */
    private Permutation _plugboard;

    /** Number of rotor slots. */
    private int _numRotors;

    /** Number of pawls. */
    private int _pawls;

    /** All available rotors. */
    private Rotor[] _allRotors;

    /** Rotors used in machine. */
    private Rotor[] _rotors;
}
