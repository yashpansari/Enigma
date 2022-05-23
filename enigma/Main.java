package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.NoSuchElementException;



import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Yash Pansari
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        if (args.size() < 1 || args.size() > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        String temp = _input.nextLine();
        if (!temp.startsWith("*")) {
            throw new EnigmaException("no rotors in machine");
        }
        setUp(enigma, temp);
        while (_input.hasNextLine()) {
            String answer = "";
            String nextLine = _input.nextLine();
            if (nextLine.startsWith("*")) {
                setUp(enigma, nextLine);
                if (_input.hasNextLine()) {
                    nextLine = _input.nextLine();
                } else {
                    break;
                }
            }
            String[] words = nextLine.split("\s+");
            for (String word : words) {
                for (char ch : word.toCharArray()) {
                    if (!_alphabet.contains(ch)) {
                        throw new EnigmaException("message not in alphabet");
                    }
                }
                answer += enigma.convert(word);
            }
            printMessageLine(answer);
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            if (!_config.hasNext()) {
                throw new EnigmaException("Format Missing Alphabet.");
            }
            _alphabet = new Alphabet(_config.next());
            if (_alphabet.contains('*')) {
                throw new EnigmaException("Banned characters in alphabet.");
            }
            if (_alphabet.contains('(') || _alphabet.contains(')')) {
                throw new EnigmaException("Banned characters in alphabet.");
            }
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Format Missing NumRotors.");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Format Missing Pawls.");
            }
            int pawls = _config.nextInt();
            ArrayList<Rotor> allRotors = new ArrayList<>();
            while (_config.hasNext()) {
                rotorName = _config.next();
                if (!_config.hasNext()) {
                    throw new EnigmaException("Rotor setting missing.");
                }
                String rotorSetting = _config.next();
                rotorType = rotorSetting.substring(0, 1);
                notch = "";
                if (rotorType.equals("M")) {
                    if (rotorSetting.length() == 1) {
                        throw new EnigmaException("Moving must have notch.");
                    }
                    notch = rotorSetting.substring(1);
                } else if (rotorSetting.length() != 1) {
                    throw new EnigmaException("only movingRotor has notches.");
                }
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String rotorConfig = "";
            while (_config.hasNext("\\(.*\\)")) {
                rotorConfig +=  _config.next() + " ";
            }
            Permutation rotorPerm = new Permutation(rotorConfig, _alphabet);
            if (rotorType.equals("M")) {
                for (char i : notch.toCharArray()) {
                    if (!_alphabet.contains(i)) {
                        throw new EnigmaException("notch not on wheel");
                    }
                }
                return new MovingRotor(rotorName, rotorPerm, notch);
            } else if (rotorType.equals("N")) {
                return new FixedRotor(rotorName, rotorPerm);
            } else if (rotorType.equals("R")) {
                if (!rotorPerm.derangement()) {
                    throw new EnigmaException("R always derangement");
                }
                return new Reflector(rotorName, rotorPerm);
            } else {
                throw new EnigmaException("type invalid");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    private String ringstellung(Machine M, String initial, String temp) {
        for (int i = 1; i < M.numRotors(); i++) {
            Rotor rotor = M.getRotor(i);
            String notches = rotor.notches();
            String nn = "";
            for (char ch : notches.toCharArray()) {
                int j = M.alphabet().toInt(ch);
                j -= M.alphabet().toInt(temp.charAt(i - 1));
                j = M.alphabet().wrap(j);
                nn += M.alphabet().toChar(j);
            }
            rotor.setNotches(nn);
        }
        String result = "";
        for (int i = 0; i < initial.length(); i++) {
            char ch = initial.charAt(i);
            int j = M.alphabet().toInt(ch);
            j -= M.alphabet().toInt(temp.charAt(i));
            j = M.alphabet().wrap(j);
            result += M.alphabet().toChar(j);
        }
        return result;
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        if (!settings.startsWith("*")) {
            throw new EnigmaException("no rotors in machine.");
        }
        if (settings.split(" ").length < M.numRotors() + 1) {
            throw new EnigmaException("Not enough rotors");
        }
        Scanner settingsSc = new Scanner(settings);
        settingsSc.next();
        int n = M.numRotors();
        String[] names = new String[n];
        for (int i = 0; i < n; i = i + 1) {
            names[i] = settingsSc.next();
        }
        for (int i = 0; i < names.length - 1; i++) {
            for (int j = i + 1; j < names.length; j++) {
                if (names[i].equals(names[j])) {
                    throw new EnigmaException("Repeated Rotor");
                }
            }
        }
        M.insertRotors(names);
        if (!M.getRotor(0).reflecting()) {
            throw new EnigmaException("First rotor should reflect");
        }
        int moving = 0;
        boolean flag = false;
        for (int i = 1; i < n; i = i + 1) {
            if (M.getRotor(i).reflecting()) {
                throw new EnigmaException("Only first can reflect");
            }
            if (M.getRotor(i).rotates()) {
                flag = true;
                moving += 1;
            } else if (flag) {
                throw new EnigmaException("Moving placed before fixed.");
            }
        }
        if (moving != M.numPawls()) {
            throw new EnigmaException("Wrong no. of pawls");
        }
        String initial = settingsSc.next();
        String cyclesPb = "";
        if (settingsSc.hasNext()) {
            String temp = settingsSc.next();
            if (!temp.matches("\\(.*\\)")) {
                initial = ringstellung(M, initial, temp);
            } else {
                cyclesPb = temp;
            }
            while (settingsSc.hasNext("\\(.*\\)")) {
                cyclesPb += settingsSc.next("\\(.*\\)") + " ";
            }
        }
        if (settingsSc.hasNext()) {
            throw new EnigmaException("Wrong no. of settings");
        }
        M.setRotors(initial);
        M.setPlugboard(new Permutation(cyclesPb, _alphabet));
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int ch = 1; ch <= msg.length(); ch++) {
            _output.print(msg.charAt(ch - 1));
            if (!(ch == msg.length()) && (ch % 5 == 0)) {
                _output.print(" ");
            }
        }
        _output.print("\r\n");
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Type of rotor. */
    private String rotorType;

    /** Notches of rotor. */
    private String notch;

    /** Name of rotor. */
    private String rotorName;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
