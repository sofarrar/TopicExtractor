#!/usr/bin/env python2.6
# -*- encoding: utf8 -*-

import re, codecs

# open UTF-8 files
input = codecs.open("prep.txt","r","utf-8")
output = open("prepout.txt","w")
output.write(codecs.BOM_UTF8)

# find chinese characters
pat =  re.compile(ur'[\u4e00-\u9fff]+')
title = re.compile('Title: ')

begin = 1 
lines = input.readlines()
for l in lines: 
  if title.search(l):
     if begin:
       begin = 0
     else:
      output.write('\n')

  for n in pat.findall(l):
    output.write(n.encode("utf-8") + ' ')

input.close()
output.close()
