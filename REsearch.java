// 1644272 Alexander Trotter

import java.io.*;
import java.util.*;

public class REsearch {
    // Inner class to represent a Finite State Machine
    private static class FSM {
        static class State {
            int id;
            String type;   // State type: character, BR (branch), WC (wildcard), or ACCEPT
            int next1, next2;

            State(int id, String type, int next1, int next2) {
                this.id = id;
                this.type = type;
                this.next1 = next1;
                this.next2 = next2;
            }
        }

        List<State> states = new ArrayList<>();
        int acceptState = -1;

        // Adds a new state to the FSM
        void addState(int id, String type, int next1, int next2) {
            states.add(new State(id, type, next1, next2));
            if (type.equals("ACCEPT")) {
                acceptState = id;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java REsearch input_file");
            System.exit(1);
        }
        try {
            FSM fsm = readFSM(); // Build FSM from stdin
            searchFile(args[0], fsm); // Search the file using FSM
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    // Reads FSM description from stdin
    private static FSM readFSM() throws IOException {
        FSM fsm = new FSM();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length != 4) {
                throw new IOException("Invalid FSM format: " + line);
            }
            int id = Integer.parseInt(parts[0]);
            String type = parts[1];
            int next1 = Integer.parseInt(parts[2]);
            int next2 = Integer.parseInt(parts[3]);
            fsm.addState(id, type, next1, next2);
        }
        return fsm;
    }

    // Searches each line of the input file using the FSM
    private static void searchFile(String filename, FSM fsm) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            if (matches(line, fsm)) {
                System.out.println(line); // Print matching lines
            }
        }
        reader.close();
    }

    // Checks if any substring of text matches the FSM
    private static boolean matches(String text, FSM fsm) {
        for (int i = 0; i < text.length(); i++) {
            Set<Integer> currentStates = new HashSet<>();
            addState(0, currentStates, fsm); // Add epsilon closure from start state

            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                Set<Integer> nextStates = new HashSet<>();

                for (int stateId : currentStates) {
                    FSM.State state = fsm.states.get(stateId);
                    if (state.type.equals("WC") || state.type.equals(String.valueOf(c))) {
                        addState(state.next1, nextStates, fsm);
                    }
                }

                if (nextStates.isEmpty()) break;

                currentStates = new HashSet<>();
                for (int s : nextStates) {
                    addState(s, currentStates, fsm); // Expand epsilon transitions
                }
            }

            // If any of the final states is ACCEPT, match is found
            for (int stateId : currentStates) {
                if (stateId == fsm.acceptState) {
                    return true;
                }
            }
        }
        return false;
    }

    // Recursively adds epsilon transitions (BR states)
    private static void addState(int stateId, Set<Integer> states, FSM fsm) {
        if (stateId == -1 || states.contains(stateId)) return;
        states.add(stateId);
        FSM.State state = fsm.states.get(stateId);
        if (state.type.equals("BR")) {
            addState(state.next1, states, fsm);
            addState(state.next2, states, fsm);
        }
    }
}
