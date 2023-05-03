SPARC-LINUX-GCC = sparc-linux-gcc
JAVACC = javacc
JAVAC = javac
JAR = jar

default: compiler assembler

compiler: compile.jar
	chmod +x compile

assembler: compiler runtime.o
	chmod +x assemble

runtime.o:
	$(SPARC-LINUX-GCC) -Wall -c runtime.c -o runtime.o

compile.jar: Manifest javacc
	$(JAVAC) -classpath .:$(SUPPORT) */*.java */*/*.java
	$(JAR) cvfm $@ Manifest */*.class */*/*.class

Manifest:
	echo "Manifest-Version: 1.0" > Manifest
	echo "Main-Class: main.Main" >> Manifest
	echo "Class-Path: $(SUPPORT)" >> Manifest

javacc:
	$(JAVACC) -DEBUG_PARSER -OUTPUT_DIRECTORY=parser/ parser/parser.jj

phase12.jar: clean
	$(JAR) cvf $@ ./compile ./Makefile ./runtime.c ./Test.java ./README.txt ./LICENSE ./assemble */*

clean:
	-/bin/rm -f Manifest
	-/bin/rm -f phase12.jar
	-/bin/rm -f */~ */*~ */*.class
	-/bin/rm -f parser/MiniParser.java
	-/bin/rm -f parser/MiniParserConstants.java
	-/bin/rm -f parser/MiniParserTokenManager.java
	-/bin/rm -f parser/ParseException.java
	-/bin/rm -f parser/SimpleCharStream.java
	-/bin/rm -f parser/Token.java
	-/bin/rm -f parser/TokenMgrError.java
	-/bin/rm -f compile.jar
	-/bin/rm -f checker/*.class
	-/bin/rm -f checker/error/*.class
	chmod -x compile
