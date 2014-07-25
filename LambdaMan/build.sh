#!/bin/sh

mkdir -p bin
javac $(find src -name '*.java') -d bin
