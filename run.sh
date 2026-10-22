#!/bin/sh
# Compiles and starts the SC Ice Cream Management System (macOS / Linux).
#   sh run.sh          start the program (options such as --plain are passed on)
#   sh run.sh test     run the automated tests
#   sh run.sh demo     replay the scripted demo session
# The compiled classes go to a temporary folder, so nothing is added to the project.
cd "$(dirname "$0")" || exit 1
BUILD="${TMPDIR:-/tmp}/sc-icecream-build"

rm -rf "$BUILD"
javac -d "$BUILD" -sourcepath src src/Main.java test/AlgorithmTest.java || {
    echo "Compilation failed. Make sure a JDK is installed and javac is on your PATH."
    exit 1
}

case "$1" in
    test) java -cp "$BUILD" AlgorithmTest ;;
    demo) java -cp "$BUILD" Main < test/demo-input.txt ;;
    *) java -cp "$BUILD" Main "$@" ;;
esac
