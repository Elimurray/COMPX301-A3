// 1626260 Eli Murray

import java.util.*;

public class REcompile {
    private static class FSM {
        List<State> states = new ArrayList<>();
        int stateCount = 0;

        static class State {
            int id;
            String type;
            int next1, next2;

            State(int id, String type, int next1, int next2) {
                this.id = id;
                this.type = type;
                this.next1 = next1;
                this.next2 = next2;
            }

            public String toString() {
                return id + "," + type + "," + next1 + "," + next2;
            }
        }

        // Adds a new state and returns its ID
        int addState(String type, int next1, int next2) {
            states.add(new State(stateCount, type, next1, next2));
            return stateCount++;
        }
    }

    private String regex;
    private int pos;
    private FSM fsm;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java REcompile \"regex\"");
            System.exit(1);
        }
        try {
            REcompile compiler = new REcompile(args[0]);
            compiler.compile();
            compiler.printFSM(); // Outputs FSM to stdout
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public REcompile(String regex) {
        this.regex = regex;
        this.pos = 0;
        this.fsm = new FSM();
    }

    // Top-level compilation logic
    private void compile() {
        int start = fsm.addState("BR", -1, -1); // BR to actual expression
        int[] result = parseRegexp();           // Compile main regex
        int finalState = fsm.addState("ACCEPT", -1, -1);
        fsm.states.get(start).next1 = result[0];
        fsm.states.get(result[1]).next1 = finalState;
    }

    private void printFSM() {
        for (FSM.State state : fsm.states) {
            System.out.println(state);
        }
    }

    // Parses an entire regexp, handling alternation (|)
    private int[] parseRegexp() {
        int[] term = parseTerm();
        if (pos < regex.length() && regex.charAt(pos) == '|') {
            pos++;
            int branch = fsm.addState("BR", term[0], -1);
            int[] rest = parseRegexp();
            int merge = fsm.addState("BR", -1, -1);
            fsm.states.get(term[1]).next1 = merge;
            fsm.states.get(rest[1]).next1 = merge;
            fsm.states.get(branch).next2 = rest[0];
            return new int[]{branch, merge};
        }
        return term;
    }

    // Parses sequence (concatenation)
    private int[] parseTerm() {
        int[] factor = parseFactor();
        if (pos < regex.length() && regex.charAt(pos) != ')' && regex.charAt(pos) != '|') {
            int[] rest = parseTerm();
            fsm.states.get(factor[1]).next1 = rest[0];
            return new int[]{factor[0], rest[1]};
        }
        return factor;
    }

    // Parses factor + ?, *, +
    private int[] parseFactor() {
        int[] base = parseBase();
        if (pos < regex.length()) {
            char c = regex.charAt(pos);
            if (c == '*') {
                pos++;
                int start = fsm.addState("BR", base[0], -1);
                int end = fsm.addState("BR", -1, -1);
                fsm.states.get(base[1]).next1 = start;
                fsm.states.get(start).next2 = end;
                return new int[]{start, end};
            } else if (c == '?') {
                pos++;
                int start = fsm.addState("BR", base[0], -1);
                int end = fsm.addState("BR", -1, -1);
                fsm.states.get(base[1]).next1 = end;
                fsm.states.get(start).next2 = end;
                return new int[]{start, end};
            } else if (c == '+') {
                pos++;
                int loop = fsm.addState("BR", base[0], -1);
                int end = fsm.addState("BR", -1, -1);
                fsm.states.get(base[1]).next1 = loop;
                fsm.states.get(loop).next2 = end;
                return new int[]{base[0], end};
            }
        }
        return base;
    }

    // Parses a base symbol, escaped character, or group
    private int[] parseBase() {
        if (pos >= regex.length()) throw new RuntimeException("Unexpected end of regex");
        char c = regex.charAt(pos++);
        if (c == '(') {
            int[] regexp = parseRegexp();
            if (pos >= regex.length() || regex.charAt(pos) != ')') {
                throw new RuntimeException("Missing closing parenthesis");
            }
            pos++;
            return regexp;
        } else if (c == '\\') {
            if (pos >= regex.length()) {
                throw new RuntimeException("Incomplete escape sequence");
            }
            c = regex.charAt(pos++);
            int state = fsm.addState(String.valueOf(c), -1, -1);
            return new int[]{state, state};
        } else if (c == '.') {
            int state = fsm.addState("WC", -1, -1); // Wildcard match
            return new int[]{state, state};
        } else if (!isSpecial(c)) {
            int state = fsm.addState(String.valueOf(c), -1, -1); // Literal character
            return new int[]{state, state};
        } else {
            throw new RuntimeException("Invalid character: " + c);
        }
    }

    private boolean isSpecial(char c) {
        return c == '*' || c == '?' || c == '+' || c == '|' || c == '(' || c == ')' || c == '.' || c == '\\';
    }
}
