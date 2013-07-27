0                Start state
0 4              Final states (i.e., accept states)
0 ^ ^ 1 $        This pda accepts {a^nb^n | n >= 0}
1 a $ 2 a$   
2 a a 2 aa
2 b a 3 ^        ^ represents null string
3 b a 3 ^
3 ^ $ 4 ^    
