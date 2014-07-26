include() {
	# $1 = filename to include
	for prefix in include; do
		[ -e "$prefix/$1" ] && { cat "$prefix/$1"; return; }
	done
	cat "$1"
}

# Reads a named member from the world state tuple on TOS
World() {
	case $1 in
		Map) echo -n "CAR";;
		Lambda) echo -ne "CDR\nCAR";;
		Ghost) echo -ne "CDR\nCDR\nCAR";;
		Fruit) echo -ne "CDR\nCDR\nCDR";;
		*) echo >&2 "world: no tuple member: $1"; exit 1;;
	esac
	echo "  ; World.$1"
	# Allow state chaining - eg $(World Lambda Direction) to get lambdaman's dir
	[ "$#" -gt 1 ] && { cmd="$1"; shift; eval $cmd "$@"; }
	true
}

# Reads a named member from the lambda state tuple on TOS
Lambda() {
	case $1 in
		Vitality) echo -n "CAR";;
		Location) echo -ne "CDR\nCAR";;
		Direction) echo -ne "CDR\nCDR\nCAR";;
		Lives) echo -ne "CDR\nCDR\nCDR\nCAR";;
		Score) echo -ne "CDR\nCDR\nCDR\nCDR";;
		*) echo >&2 "lambda: no tuple member: $1"; exit 1;;
	esac
	echo "  ; Lambda.$1"
}

func() {
	# $1 = function name
	# $2 = number of variables in new frame
	# XXX ideally the compiler would fill out $2 for us
	echo "LDF  $1"
	echo "AP   $2"
}

