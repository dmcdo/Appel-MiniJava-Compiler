This is a compiler that compiles MiniJava to SPARC code built following
Modern Compiler Implementation in Java by Andrew Appel up to Chapter 12.

Building:
$ export SUPPORT=support.jar  # https://cs.fit.edu/~ryan/cse4251/support.jar
$ make javacc                 # Build only the javacc files.
$ make compiler               # Build only the compiler.
$ make assembler              # Build the compiler and assembler.
$ make                        # Same as 'make assembler.'

Usage:
./compile Test.java
./assemble Test.s

# thank you for investing your time in this
