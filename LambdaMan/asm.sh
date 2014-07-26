#!/bin/sh
# eg: ./asm.sh etc/local.gcc
# (will write assembled instructions to stdout)
java -cp bin/ asm.Asm <"$1"
