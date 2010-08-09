#!/bin/bash
javac Preprocessor.java
java Preprocessor $@
./findZHchar.py
./createData.py
