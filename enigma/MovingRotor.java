package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Yash Pansari
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        _old = _notches;
    }
    @Override
    void setNotches(String notches) {
        _old = _notches;
        _notches = notches;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        return _notches.contains("" + setting());
    }

    @Override
    void advance() {
        set(permutation().alphabet().toInt(setting()) + 1);
    }

    @Override
    String notches() {
        return _notches;
    }

    @Override
    String old() {
        return _old;
    }

    /** The notches pf this rotor. */
    private String _notches;

    /** The original notches pf this rotor. */
    private String _old;

}
