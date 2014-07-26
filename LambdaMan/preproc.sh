#!/bin/sh

. include/macros.sh

eval "$(awk -v Q="'" '
/!{.*}/ {
	sub(".*!{", "")
	sub("}.*", "")
	print
	next
}

{
	gsub(Q, Q Q "\\" Q)
	print "echo " Q $0 Q
}')"
