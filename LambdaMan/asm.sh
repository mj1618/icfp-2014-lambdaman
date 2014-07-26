#!/bin/sh
# eg: ./asm.sh etc/local.gcc
# (will write assembled instructions to stdout)
./preproc.sh <"$1" |
java -cp bin/ asm.Asm
