#!/usr/bin/env bash
# Quick launcher script for AI Chatbot

set -e

# Change directory to project root
cd "$(dirname "$0")"

mkdir -p bin

if [ "$1" == "--test" ] || [ "$1" == "-t" ]; then
    echo "==> Compiling test suite..."
    javac src/*.java -d bin
    echo "==> Running automated tests..."
    java -cp bin ChatEngineTest
    exit $?
fi

echo "==> Compiling Java sources..."
javac src/*.java -d bin

if [ "$1" == "--cli" ] || [ "$1" == "-c" ]; then
    echo "==> Starting Chatbot in CLI mode..."
    java -cp bin Main --cli
else
    echo "==> Starting Chatbot Swing GUI..."
    java -cp bin Main
fi
