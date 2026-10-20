#!/bin/sh
# Compiles and starts the SC Ice Cream Management System (macOS / Linux).
# Run with: sh run.sh
cd "$(dirname "$0")" || exit 1

rm -rf out
javac -d out src/*.java || {
    echo "Compilation failed. Make sure a JDK is installed and javac is on your PATH."
    exit 1
}

java -cp out Main
