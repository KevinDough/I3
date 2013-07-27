0               Start state
4               Final state  

0 ^ ^ 1 $       This pda accepts strings equal number of a's and b's 
1 a $ 1 a$
1 b $ 1 b$
1 a a 1 aa
1 b b 1 bb
1 ^ $ 4 ^
1 a b 1 ^
1 b a 1 ^
