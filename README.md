# CS4613-Fall-2020-CSP-Backtrack-Project
## Project Description
Design and implement a program to solve Hyper Sudoku puzzles. Hyper 
Sudoku differs from the classic Sudoku in that four overlapping regions are defined in addition to 
the regular regions, as described below. The rules of the game are:<br /><br />
• The game board consists of 9 × 9 cells divided into 3 × 3 non-overlapping regions (See 
Figure 1.) Four additional overlapping regions are defined, as highlighted in green. The 
game board therefore contains 9 non-overlapping regions and 4 overlapping regions, with 
each region containing 3 × 3 cells. Some of the cells already have numbers (1 to 9) 
assigned to them initially.<br /><br />
• The goal is to find assignments (1 to 9) for the empty cells so that every row, column, non-overlapping region and overlapping region contains all the digits from 1 to 9. Each of the 
9 digits, therefore, can only appear once in every row, column, non-overlapping region and 
overlapping region. Figure 2 shows the solution for the initial game board in Figure 1.<br /><br />
As a first step in your program, apply Forward Checking to cells that already have a number 
assigned to them and reduce the domain of their neighbors. If any cell has an empty domain after 
applying Forward Checking, then the puzzle does not have a solution and the program can stop and 
exit. Here, you run the Forward Checking algorithm before you run the Backtracking Algorithm
for CSPs. Next, use the Backtracking Algorithm in Figure 4 below to solve for a solution.
Implement the function SELECT-UNASSIGNED-VARIABLE by using the minimum remaining 
value heuristic and then the degree heuristic. If there are more than one variables left after applying 
the two heuristics, you can arbitrarily choose a variable to work on next. You do not have to 
implement the least constraining value heuristic in the ORDER-DOMAIN-VALUES function; 
instead, simply order the domain values in increasing order, from 1 to 9. You do not have to 
implement the INFERENCE function inside the Backtracking Algorithm. (Remember: two or more 
variables are neighbors if they share a common constraint.)

## Input and ouput format
Your program will read in the initial game board configuration from an 
input text file and produce an output text file that contains the solution. The input file contains 9 rows (or lines) of integers. Each row contains 9 integers ranging from 0 to 9, separated by blank 
spaces. Digits 1-9 represent the cell values and 0’s represent blank cells. The input file for the initial 
game board in Figure 1 is shown in Figure 3(a) below. Similarly, the output file contains 9 rows of 
integers, with each row containing digits ranging from 1 to 9, separated by blank spaces. The output 
file for the initial game board in Figure 1 is shown in Figure 3(b) below.
