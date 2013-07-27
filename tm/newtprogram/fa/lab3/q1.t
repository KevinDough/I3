0                Start state
4                List of final states (i.e., accept states)
0 b 1 x R        This Turing machine accepts {b^nc^n | n >= 1}
1 b 1 b R
1 y 1 y R
1 c 2 y L
2 y 2 y L
2 b 2 b L
2 x 0 x R
0 y 3 y R
3 y 3 y R
3 ? 4 ? R        ? represents a blank