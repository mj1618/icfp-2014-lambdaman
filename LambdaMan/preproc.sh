#!/bin/sh

. include/macros.sh

eval "$(awk -v Q="'" '
/!{.*}/ {
	sub(".*!{", "")
	sub("}.*", "")
	print "{ " $0 "; } || exit 1"
	next
}

{
	gsub(Q, Q Q "\\" Q)
	print "echo " Q $0 Q
}')"
