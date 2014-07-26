include() {
	# $1 = filename to include
	for prefix in include; do
		[ -e "$prefix/$1" ] && { cat "$prefix/$1"; return; }
	done
	cat "$1"
}

index() {
	for i in "$@"; do
		# $i = index of desired element (in list on top of stack)
		yes CDR |head -n "$i"
		echo CAR
	done
}

func() {
	# $1 = function name
	# $2 = number of variables in new frame
	# XXX ideally the compiler would fill out $2 for us
	echo "LDF  $1"
	echo "AP   $2"
}

