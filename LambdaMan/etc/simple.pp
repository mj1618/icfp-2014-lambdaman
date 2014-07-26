main:
  LDC  0    ; keeping no state yet
  LDF  step
  CONS
  RTN

step:
  LDC  0    ; pre-load empty AI state onto stack
  LD   0 1
CDR
CAR
CDR
CAR

LDF  neighbours
AP   1
  LDF  curried_cell
LDF  map
AP   2
  DBUG
  LDC  dLeft

  CONS
  RTN

neighbours:
  ; expects ($x $y) coordinate pair as arg
  ; pushes list of neighbouring coordinates: (UP RIGHT DOWN LEFT)
  LD   0 0
LDF  up
AP   1
  LD   0 0
LDF  right
AP   1
  LD   0 0
LDF  down
AP   1
  LD   0 0
LDF  left
AP   1
  LDC  0
  CONS
  CONS
  CONS
  CONS
  RTN
  
curried_cell:
  ; gets called by map with just a coordinate. references the
  ; parent environment to get the game map.
  LD 0 0
  DBUG
  LD 1 1
CAR
  LD 0 0
  LDF cell
  TAP 2

; stdlib.gcc contains standard "system" functions, not
; specific to any of the game logic.

elt:
  ; expects $list $int as args. assumes $int is >= 0.
  ; pushes the $int'th index of $list onto stack
  LD   0 0
  LD   0 1
  TSEL elt_recurse elt_done
elt_done:
  ; $int == 0 => push CAR of $list and we're done
  CAR
  RTN
elt_recurse:
  ; $int > 0
  CDR       ; push CDR($list)
  LD   0 1
  LDC  1
  SUB       ; push $int - 1
  LDF  elt
  TAP  2


map:
  ; expects $list $closure as args
  ; calls $closure on each element of $list and pushes the resulting list
  LD   0 0
  ATOM
  TSEL map_empty map_cons

map_empty:
  ; input list was empty => so is output
  LDC  0
  RTN

map_cons:
  ; otherwise we have a CONS
  LD   0 0
  CAR
  LD   0 1
  AP   1   ; run $closure on CAR($list)

  ; TOS is now the result from $closure
  LD   0 0
  CDR
  LD   0 1
  LDF  map
  AP   2   ; recurse to process rest of list

  CONS     ; prepend our result
  RTN


reduce:
  ; expects $list $closure as args
  ; returns a single list element by repeatedly passing two elements
  ; to $closure, which should return the argument to be kept
  LD   0 0
  ATOM
  TSEL reduce_empty reduce_cons

reduce_empty:
  ; input list was empty => return 0
  LDC  0
  RTN

reduce_cons:
  LD   0 0
  CDR
  ATOM
  TSEL reduce_last reduce_mid

reduce_last:
  ; we're on the last CONS of the list, just return CAR
  LD   0 0
  CAR
  RTN

reduce_mid:
  ; in the middle of the list. get the value from reducing the rest
  ; of the list, and then call $closure against the current value.
  LD   0 0
  CDR
  LD   0 1
  ; optimisation - we know CDR is a cons (otherwise we would have
  ; branched to reduce_last). so, we can recurse directly into
  ; reduce_cons instead of the function's normal entry point.
  LDF  reduce_cons
  AP   2

  ; now we have the reduce result on the stack, push our CAR and
  ; call $closure
  LD   0 0
  CAR
  LD   0 1
  AP   2
  RTN


max2:
  ; returns the largest of the two arguments.
  LD   0 0
  LD   0 1
  CGTE
  TSEL max2_first max2_second
max2_first:
  LD   0 0
  RTN
max2_second:
  LD   0 1
  RTN
; named constants for indexes into the world state
$const wMap    0
$const wLambda 1
$const wGhost  2
$const wFruit  3

; indexes into lambda-man state
$const lmVitality  0
$const lmLocation  1
$const lmDirection 2
$const lmLives     3
$const lmScore     4

$const Wall        0
$const Empty       1
$const Pill        2
$const PowerPill   3
$const Fruit       4
$const LambdaStart 5
$const GhostStart  6

$const dUp    0
$const dRight 1
$const dDown  2
$const dLeft  3
; requires stdlib.gcc

cell:
  ; expects MAP and ($x $y) coordinate as args
  ; returns the entry in the map at position ($x $y)
  LD   0 0
  LD   0 1
  CDR      ; MAP is row-major, so lookup $y-th row first
  LDF  elt
  AP   2

  LD   0 1
  CAR
  LDF  elt
  AP   2

  RTN
  

left:
  ; expects ($x $y) coordinate pair as arg
  ; pushes ($x-1 $y)
  LD   0 0
  CAR
  LDC  1
  SUB
  LD   0 0
  CDR
  CONS
  RTN

right:
  ; expects ($x $y) coordinate pair => ($x+1 $y)
  LD   0 0
  CAR
  LDC  1
  ADD
  LD   0 0
  CDR
  CONS
  RTN

up:
  ; expects ($x $y) coordinate pair => ($x $y-1)
  LD   0 0
  CAR
  LD   0 0
  CDR
  LDC  1
  SUB
  CONS
  RTN

down:
  ; expects ($x $y) coordinate pair => ($x $y+1)
  LD   0 0
  CAR
  LD   0 0
  CDR
  LDC  1
  ADD
  CONS
  RTN


