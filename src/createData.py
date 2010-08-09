#!/usr/bin/env python2.6
# -*- encoding: utf8 -*-

import re, codecs, os,sys

# open UTF-8 files
input = codecs.open("prepout.txt.seg","r","utf-8")
vocabout = open("vocab.txt","w")
dataout = open("topic_zh.dat","w") 
stopwords = codecs.open("stopwords.txt","r","utf-8")
stopout = open ("stopout.txt","w")
stopin = codecs.open("stopout.txt.seg","r","utf-8")
#log = open("log.txt","w")

#vocabout.write(codecs.BOM_UTF8)

# read stopwords
pat = re.compile('\s+\d+\s\d+.\d+\s(.*)')
lines = stopwords.readlines()
for l in lines:
  k = pat.search(l)
  if k:
    word = k.group(1)
    stopout.write(word.encode("utf-8") + ' ')
stopout.close()

# segement chinese words
command = './segmenter.sh stopout.txt'
l = os.popen(command)

command = './segmenter.sh prepout.txt'
l = os.popen(command)

# create stopwords dictionary
stopdict = {}
lines = stopin.readlines()
for l in lines:
  k = re.split('\s',l)
  for w in k:
    if re.match('\S',w):
      if not w in stopdict:
        stopdict[w] = 1
        #log.write(w.encode("utf-8") + '\n')
  
# create vocab.txt
dict = {} 
lines = input.readlines()
for l in lines: 
  n = re.split('\s',l)
  for t in n:
    if re.match(ur'[\u4e00-\u9fff]+',t):  
      if not t in stopdict:
        if not t in dict:
          dict[t] = 1
        else:
          dict[t] += 1

d = [(w,f) for w,f in dict.items()]
d.sort()

for w,c in d:
  vocabout.write(w.encode("utf-8") + '\n')


# create data file
input.seek(0)

lines = input.readlines()
for l in lines:
  data = [] 
  found = {}
  k = re.split('\s',l)
  for word in k:
    if re.match('\S',word):
      if word in dict:
        if not word in found:
          found[word] = 1
          # find frequency of the word in this doc
          n = len(re.findall(word+'\s',l))
          # find index of the word in vocab.txt
          for index,e in enumerate(d):
            if (word == e[0]):
#              log.write(word.encode("utf-8")+  ' ' + e[0].encode("utf-8") + ' ')
              data.append((index,n))
#         log.write('\n')
  
  dataout.write(str(len(data)))
  for pair in data:
    dataout.write(' ' + str(pair[0]) + ':' + str(pair[1]))
  dataout.write('\n')

input.close()
vocabout.close()
dataout.close()
stopwords.close()
stopout.close()
stopin.close()
#log.close()
