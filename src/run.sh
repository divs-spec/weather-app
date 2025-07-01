#!/bin/bash

# Create necessary directories
mkdir -p lib .tests

# JSON Library details
JSON_URL="https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar"
JSON_JAR="lib/json-20230227.jar"

# Download JSON library if not exists
if [ ! -f "$JSON_JAR" ]; then
    echo "Downloading JSON library..."
    wget -O "$JSON_JAR" "$JSON_URL"
fi

# Classpath configuration
CLASSPATH=".:$JSON_JAR"

# Compile function
compile() {
    echo "Compiling WeatherServer..."
    javac -cp "$CLASSPATH" WeatherServer.java
}

# Compile test function
compile_test() {
    echo "Compiling Test..."
    javac -cp "$CLASSPATH" .tests/Test.java
}

# Run WeatherServer
run() {
    echo "Running WeatherServer..."

    # Find and kill any running instance of WeatherServer
    PID=$(ps aux | grep "[j]ava -cp $CLASSPATH WeatherServer" | awk '{print $2}')

    if [ ! -z "$PID" ]; then
        echo "Killing existing WeatherServer process: $PID"
        kill -9 $PID
        sleep 2  # Allow time for the process to stop
    fi

    java -cp "$CLASSPATH" WeatherServer &
    SERVER_PID=$!
    sleep 2
}

# Run tests
test() {
    compile
    compile_test
    run
    echo "Running tests..."

    # Execute the test class inside .tests directory
    java -cp "$CLASSPATH:.tests" Test

    # Kill the WeatherServer after testing
    kill $SERVER_PID
}

# Handle command-line arguments
case "$1" in
    "compile")
        compile
        ;;
    "compile-test")
        compile_test
        ;;
    "run")
        run
        ;;
    "test")
        test
        ;;
    *)
        echo "Usage: $0 {compile|compile-test|run|test}"
        exit 1
esac
