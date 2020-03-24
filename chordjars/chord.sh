#!/bin/sh

for index in {1..5}
do
	echo "Main-Class: no.hvl.dat110.node.peers.Process$index\n" > Manifest.txt
	jar -cfm process$index.jar Manifest.txt -C bin/ .
done