// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Set i = r1
@R1 
D=M
@i
M=D

// Set r2 = 0
@R2
M=0

(LOOP)
    // if (i <= 0) break
    @i
    D=M
    @END  
    D;JLE

    // r2 = r2 + r0
    @R2 
    D=M 
    @R0
    D=D+M 
    @R2 
    M=D

    // i-- 
    @i 
    M=M-1

    @LOOP
    0;JMP

(END)
    @END
    0;JMP