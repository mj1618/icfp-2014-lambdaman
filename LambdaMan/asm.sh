#!/bin/sh
# eg: ./asm.sh etc/local.gcc
# (will create etc/local.absgcc with the label resolved)
java -cp bin/ asm.Asm "$@"
