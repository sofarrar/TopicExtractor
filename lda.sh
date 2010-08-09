#!/bin/bash
# alpha is the base 
# k is number of topics
./lib/lda-c-dist/lda est 10 20 ./lib/lda-c-dist/settings.txt ./output/topic_zh.dat random temp
#./lib/lda-c-dist/lda inf ./lib/lda-c-dist/inf-settings.txt ./temp/final ./src/topic_zh.dat topic_zh
./lib/lda-c-dist/topics.py ./temp/final.beta ./output/vocab.txt 20 > ./results/topic_zh.txt
