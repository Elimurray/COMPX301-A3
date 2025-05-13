# REcompile.java

## Overview

`REcompile.java` is a Java program that compiles a regular expression (regex) into a Finite State Machine (FSM). The FSM is represented as a series of states, where each state has an ID, type (e.g., literal character, wildcard, branch, or accept), and up to two next-state transitions. The program outputs the FSM description to stdout.

## Functionality

- **Input**: A regular expression provided as a command-line argument.
- **Output**: A list of FSM states in the format `id,type,next1,next2`, where:
  - `id`: State identifier
  - `type`: State type (`BR` for branch, `WC` for wildcard, `ACCEPT` for accept state, or a literal character)
  - `next1`, `next2`: IDs of the next states (-1 if not applicable)
- **Error Handling**: Throws exceptions for invalid regex syntax, such as unmatched parentheses or unexpected characters.

## Usage

1. Compile the program:
   ```bash
   javac REcompile.java
   ```
2. Run the program with a regex as a command-line argument:
   ```bash
   java REcompile "a(b|c)*"
   # java REcompile "a(b|c)*" > fsm.txt #to output to a file to be used in REsearch
   ```
3. The program outputs the FSM states to stdout. Example output for `a(b|c)*`:
   ```
   0,BR,1,-1
   1,a,2,-1
   2,BR,3,-1
   3,BR,4,6
   4,b,5,-1
   5,BR,3,7
   6,c,5,-1
   7,BR,-1,-1
   8,ACCEPT,-1,-1
   ```

## Notes

- The FSM is designed to be used with a companion program (e.g., `REsearch.java`) that reads the FSM and performs pattern matching.
- The program assumes valid input for simplicity but includes basic error checking for regex syntax.
- Special characters (`*`, `?`, `+`, `|`, `(`, `)`, `.`, `\\`) are treated as regex operators unless escaped.
