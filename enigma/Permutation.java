package enigma;

import static enigma.EnigmaException.*;
import java.util.Arrays;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Yash Pansari
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        String cycless = cycles.trim().replace("(", "");
        _cycles = cycless.replace(" ", "").split("\\)");
        String usedLetters = "";
        for (String cycle : _cycles) {
            for (char letter : usedLetters.toCharArray()) {
                if (cycle.contains("" + letter)) {
                    throw new EnigmaException("repeats in cycles");
                }
            }
            usedLetters += cycle;
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycles = Arrays.copyOf(_cycles, _cycles.length + 1);
        _cycles[_cycles.length - 1] = cycle;
    }

    /** Return the value of P modulo the size of this permutation. */
    int wrap(int p) {
        return wrap(p, size());
    }

    int wrap(int p, int size) {
        int r = p % size;
        if (r < 0) {
            r += size;
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return _alphabet.toInt(permute(_alphabet.toChar(wrap(p))));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return _alphabet.toInt(invert(_alphabet.toChar(wrap(c))));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        p = _alphabet.toChar(wrap(_alphabet.toInt(p)));
        for (String cycle : _cycles) {
            if (cycle.contains("" + p)) {
                int ans = wrap((cycle.indexOf("" + p) + 1), cycle.length());
                return cycle.charAt(ans);
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        c = _alphabet.toChar(wrap(_alphabet.toInt(c)));
        for (String cycle : _cycles) {
            if (cycle.contains("" + c)) {
                int ans = wrap((cycle.indexOf("" + c) - 1), cycle.length());
                return cycle.charAt(ans);
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int sUm = Arrays.stream(_cycles).mapToInt(String::length).sum();
        if (sUm < _alphabet.size()) {
            return false;
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** All of the parsed cycles. */
    private String[] _cycles;
}
