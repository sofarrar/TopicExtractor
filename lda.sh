#!/bin.bash
./lib/lda est 10 4 settings.txt ../ap/ap.dat random temp
#./lda inf inf-settings.txt ../ap/ap.dat temp
./lib/topics.py ./lib/temp/010.beta ./lib/ap/vocab.txt 20 > ./output/topics010.txt

