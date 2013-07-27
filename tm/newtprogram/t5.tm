1
{6}


1 a x R 2     state two will be the c finder
2 a a R 2     go till find c
2 b b R 2     screw b's, i want c
2 c c R 3     state three is new side xy
3 y y R 3     screw the other y's i need to find a
3 a y L 4     screw a, heres a y
4 y y L 4     go back
4 q q L 4
4 c c L 7     passingc
7 x x R 1     for the specail case of seeing an x right after c (a's done)
7 a a L 4     once past, go back to shifting left
7 b b L 4
4 a a L 4
4 b b L 4
4 x x R 1
4 ? ? R 1


1 b z R 2    b is now z, find the c
3 b q L 8    far b's are q
3 q q R 3
8 q q L 8    8 will get us back to z
8 c c L 9
8 y y L 8
9 z z R 1
9 b b L 8
9 a a L 9
8 a a L 8
8 b b L 8
8 z z R 1
1 c c R 5
5 y y R 5
5 q q R 5
5 ? ? R 6