#!/bin/bash

# 1. Call Preprocessor with the topic and k top websites 
# e.g. java Preprocessor "北京 三里屯" 10

cd src
javac Preprocessor.java
java Preprocessor ${1+"$@"} $2

# 2. Call findZHchar.py to find chinese charcters in documents
./findZHchar.py

# 3. Call createData.py which will create input data
# which are required for lda
./createData.py

# 4. Finally call lda.sh
#You can change the parameters in lda.sh as you like.
./lda.sh

# 5. Check the result in /results e.g. topic_zh.txt
