#!/bin.bash
./lda est 10 4 settings.txt ../ap/ap.dat random temp
#./lda inf inf-settings.txt ../ap/ap.dat temp
./topics.py temp/010.beta ../ap/vocab.txt 20 > topics010.txt

