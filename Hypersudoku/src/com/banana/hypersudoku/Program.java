package com.banana.hypersudoku;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Neal SHU
 */
public class Program {

    public static final int ROW = 9;

    public static final int COL = 9;

    public static final int DOMAIN = 9;

    public static String dir = "D:\\Hypersudoku\\INPUT";

    //The global variable stores the final assignment
    public static Integer[][] staticResult = null;

    public static final Integer[][] FAILURE = null;

    public static Integer outputFileNum = 1;

    //Main function. Walk over all input files in the given directory and execute backtrack algorithm one by one
    public static void main(String[] args) throws IOException {
        Files.walk(Paths.get(dir)).filter(Files::isRegularFile).forEach(Program::executeFromFile);
    }

    //Read from a file and initialize assignemnt and variableArray, then execute the backtrack algorithm and print the solution if exists.
    public static void executeFromFile(Path path) {

        //assignment represents the game board, the actual assignment of values
        Integer[][] assignment = new Integer[9][9];
        //variableArray contains all 9x9 = 81 variables for each position of the game board
        Pos[][] variableArray = new Pos[9][9];

        fillZero(assignment);

        try {
            File file = path.toFile();
            System.out.printf("File name: %s%n", file.getName());

            Scanner scanner = new Scanner(file);

            for(int row = 0; row < ROW; row++) {
                for(int col = 0; col < COL; col++) {
                    assignment[row][col] = scanner.nextInt();
                }
            }

            System.out.println("Initial assignment");
            printBoard(assignment);
            initializeVar(variableArray, assignment);
            if(backTrack(assignment, variableArray)) {
                System.out.println("Final assignment");
                printBoard(staticResult);
            }
            else {
                System.out.println("No solution found");
            }

            writeSolution(staticResult);

            //clear the result assignment
            staticResult = new Integer[9][9];

        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Write the solution to output.txt
    public static void writeSolution(Integer[][] assignment) throws IOException {

        File output = new File(String.format(dir + "\\Output%d.txt", outputFileNum));

        outputFileNum++;
        FileWriter writer = new FileWriter(output);

        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COL; col++) {
                writer.write(assignment[row][col] + " ");
            }
            writer.write("\n");
        }

        writer.close();
    }

    //Set up the variableArray by initializing a new Pos type in each position on the game board
    public static void initializeVar(Pos[][] variableArray, Integer[][] assignment) {
        for(int row = 0; row < ROW; row++) {
            for(int col = 0; col < COL; col++) {
                if(assignment[row][col] == 0) {
                    variableArray[row][col] = new Pos(row, col);
                }
                else {
                    variableArray[row][col] = new Pos(row, col, assignment[row][col]);
                }

            }

        }
    }

    //Fill all 0s on the game board
    public static void fillZero(Integer[][] assignment) {
        for(int row = 0; row < ROW; row++) {
            for(int col = 0; col < COL; col++) {
                assignment[row][col] = 0;
            }
        }
    }

    //Print the game board
    public static void printBoard(Integer[][] assignment) {
        for(int row = 0; row < ROW; row++) {
            for(int col = 0; col < COL; col++) {
                System.out.print(assignment[row][col] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    //This is the main csp problem solver. The function takes the initial game board and its variableArray,
    //runs forward checking, and then runs the backtrack algorithm.
    public static boolean backTrack(Integer[][] assignment, Pos[][] variableArray) {
        if(!Constraint.forwardChecking(assignment, variableArray)) {
            return false;
        }
        Integer[][] output = backTrackAlgorithm(assignment, variableArray);
        return Constraint.isValid(output);
    }

    //After foward checking is executed on the initial assignment and variableArray
    public static Integer[][] backTrackAlgorithm(Integer[][] assignment, Pos[][] variableArray) {
        if(isComplete(assignment)) {
            staticResult = assignment;
            return assignment;
        }
        Pos curr = Heuristic.selectUnassignedVariable(variableArray);
        ArrayList<Integer> valueArray = Heuristic.orderDomainValues(curr);
        for(Integer value : valueArray) {
            if(Constraint.isConsistent(curr, value, assignment)) {
                ArrayList<Integer> savedDomain = curr.domain;
                makeAssignment(curr.row, curr.col, value, assignment, variableArray);
                Integer[][] result = backTrackAlgorithm(assignment, variableArray);
                if(result != FAILURE && Constraint.isValid(result)) {
                    return result;
                }
                if(!removeAssignment(curr.row, curr.col, assignment, variableArray, savedDomain)) {
                    return FAILURE;
                }
            }
        }

        return FAILURE;
    }

    //Make an assignment {var = value}
    public static void makeAssignment(int row, int col, Integer value, Integer[][] assignment, Pos[][] variableArray) {
        variableArray[row][col].isAssigned = true;
        variableArray[row][col].value = value;
        variableArray[row][col].domain = null;
        assignment[row][col] = value;

    }

    //Remove an assignment
    public static boolean removeAssignment(int row, int col, Integer[][] assignment, Pos[][] variableArray, ArrayList<Integer> domain) {
        if(variableArray[row][col].isAssigned) {
            variableArray[row][col].isAssigned = false;
            variableArray[row][col].value = null;
            variableArray[row][col].domain = domain;
            assignment[row][col] = 0;
            return true;
        }
        System.out.println("removeAssignment: failed");
        return false;
    }

    //Check if the game board is completely assigned, i.e. check if all variables are assigned
    public static boolean isComplete(Integer[][] assignment) {
        for(int row = 0; row < ROW; row++) {
            for(int col = 0; col < COL; col++) {
                if(assignment[row][col] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

}

//Pos represents a variable on the board
class Pos {
    public int row, col;
    public boolean isAssigned;
    public Integer value;
    public ArrayList<Integer> domain;

    public Pos(int row, int col) {
        this.row = row;
        this.col = col;
        isAssigned = false;
        value = null;
        domain = new ArrayList<>();
        for(int i = 1; i <= Program.DOMAIN; i++) {
            domain.add(i);
        }
    }

    public Pos(int row, int col, Integer num) {
        this.row = row;
        this.col = col;
        isAssigned = true;
        value = num;
        domain = null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof Pos)) {
            return false;
        }
        if(((Pos) obj).isAssigned && this.isAssigned) {
            return ((Pos) obj).value.equals(this.value);
        }
        return ((Pos) obj).domain.equals(this.domain);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

//The class acts like a toolbox, provides constraint-checking functions
class Constraint {

    //Check and update all variables' domains. Return false if any variable's domain is empty list
    public static boolean forwardChecking(Integer[][] assignment, Pos[][] variableArray) {
        //Check all rows and update their domains
        for(int row = 0; row < Program.ROW; row++) {
            if(!updateRow(row, assignment, variableArray)) {
                return false;
            }

        }
        //Check all columns and update their domains
        for(int col = 0; col < Program.COL; col++) {
            if(!updateCol(col, assignment, variableArray)) {
                return false;
            }
        }
        //Check all non-overlapping area and update their domains
        for(int area = 0; area < 9; area += 3) {
            for(int j = 0; j < 9; j += 3) {
                if(!updateArea(area, j, assignment, variableArray)) {
                    return false;
                }
            }
        }
        //Check all overlapping area and update their domains
        for(int area = 0; area < 4; area++) {
            // #1
            if(!updateArea(1,1, assignment, variableArray)) {
                return false;
            }
            // #2
            if(!updateArea(1,5, assignment, variableArray)) {
                return false;
            }
            // #3
            if(!updateArea(5,1, assignment, variableArray)) {
                return false;
            }
            // #4
            if(!updateArea(5,5, assignment, variableArray)) {
                return false;
            }
        }
        return true;
    }

    //Check if game board is valid by checking if all constraints are satisfied
    public static boolean isValid(Integer[][] assignment) {
        if(assignment == null) {
            System.out.println("isValid: assignment is null");
            return false;
        }
        HashSet<Integer> checkDup = new HashSet<>();
        //Row check
        for(int row = 0; row < Program.ROW; row++) {
            for(int col = 0; col < Program.COL; col++) {
                if(!assignment[row][col].equals(0) && !checkDup.add(assignment[row][col])) {
                    return false;
                }
            }
            checkDup.clear();
        }
        //Col check
        for(int col = 0; col < Program.COL; col++) {
            for(int row = 0; row < Program.ROW; row++) {
                if(!checkDup.add(assignment[row][col])) {
                    return false;
                }
            }
            checkDup.clear();
        }
        //Non-overlapping area check
        // #1
        if(!isValidArea(0, 0, assignment)) {
            return false;
        }
        // #2
        if(!isValidArea(0, 3, assignment)) {
            return false;
        }
        // #3
        if(!isValidArea(0, 6, assignment)) {
            return false;
        }
        // #4
        if(!isValidArea(3, 0, assignment)) {
            return false;
        }
        // #5
        if(!isValidArea(3, 3, assignment)) {
            return false;
        }
        // #6
        if(!isValidArea(3, 6, assignment)) {
            return false;
        }
        // #7
        if(!isValidArea(6, 0, assignment)) {
            return false;
        }
        // #8
        if(!isValidArea(6, 3, assignment)) {
            return false;
        }
        // #9
        if(!isValidArea(6, 6, assignment)) {
            return false;
        }

        //Overlapping area check
        // #1
        if(!isValidArea(1, 1, assignment)) {
            return false;
        }
        // #2
        if(!isValidArea(1, 5, assignment)) {
            return false;
        }
        // #3
        if(!isValidArea(5, 1, assignment)) {
            return false;
        }
        // #4
        if(!isValidArea(5, 5, assignment)) {
            return false;
        }
        return true;
    }

    //A helper function for isValid(), check validity for areas
    public static boolean isValidArea(int topLeftRow, int topLeftCol, Integer[][] assignment) {
        if(topLeftRow < 0 ||topLeftRow > 6 || topLeftCol < 0 || topLeftCol > 6) {
            System.out.println("isValidArea: invalid row or col given");
            return false;
        }
        HashSet<Integer> checkDup = new HashSet<>();
        for(int row = topLeftRow; row < topLeftRow + 3; row++) {
            for(int col = topLeftCol; col < topLeftCol + 3; col++) {
                if(!assignment[row][col].equals(0) && !checkDup.add(assignment[row][col])) {
                    return false;
                }
            }
        }
        return true;
    }

    //Check if an assignment is consistent by checking if any neighbor of this variable already has the same value assigned
    public static boolean isConsistent(Pos variable, Integer value, Integer[][] assignment) {
        //Check row
        for(int col = 0; col < Program.COL; col++) {
            if(assignment[variable.row][col].equals(value)) {
                return false;
            }
        }
        //Check col
        for(int row = 0; row < Program.ROW; row++) {
            if(assignment[row][variable.col].equals(value)) {
                return false;
            }
        }

        //Find the variable's non-overlapping area, and check non-overlapping area
        // #1
        if(variable.row < 3 && variable.col < 3) {
            for(int row = 0; row < 3; row++) {
                for(int col = 0; col < 3; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #2
        if(variable.row < 3 && variable.col >= 3 && variable.col < 6) {
            for(int row = 0; row < 3; row++) {
                for(int col = 3; col < 6; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #3
        if(variable.row < 3 && variable.col >= 6) {
            for(int row = 0; row < 3; row++) {
                for(int col = 6; col < 9; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #4
        if(variable.row >= 3 && variable.row < 6 && variable.col < 3) {
            for(int row = 3; row < 6; row++) {
                for(int col = 0; col < 3; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #5
        if(variable.row >= 3 && variable.row < 6 && variable.col >= 3 && variable.col < 6) {
            for(int row = 3; row < 6; row++) {
                for(int col = 3; col < 6; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #6
        if(variable.row >= 3 && variable.row < 6 && variable.col >= 6) {
            for(int row = 3; row < 6; row++) {
                for(int col = 6; col < 9; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #7
        if(variable.row >= 6 && variable.col < 3) {
            for(int row = 6; row < 9; row++) {
                for(int col = 0; col < 3; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #8
        if(variable.row >= 6 && variable.col >= 3 && variable.col < 6) {
            for(int row = 6; row < 9; row++) {
                for(int col = 3; col < 6; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #9
        if(variable.row >= 6 && variable.col >= 6) {
            for(int row = 6; row < 9; row++) {
                for(int col = 6; col < 9; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        //Find the variable's overlapping area, if any, and check overlapping area
        // #1
        if(variable.row >= 1 && variable.row < 4 && variable.col >= 1 && variable.col < 4) {
            for(int row = 1; row < 4; row++) {
                for(int col = 1; col < 4; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #2
        if(variable.row >= 1 && variable.row < 4 && variable.col >= 5 && variable.col < 8) {
            for(int row = 1; row < 4; row++) {
                for(int col = 5; col < 8; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #3
        if(variable.row >= 5 && variable.row < 8 && variable.col >= 1 && variable.col < 4) {
            for(int row = 5; row < 8; row++) {
                for(int col = 1; col < 4; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        // #4
        if(variable.row >= 5 && variable.row < 8 && variable.col >= 5 && variable.col < 8) {
            for(int row = 5; row < 8; row++) {
                for(int col = 5; col < 8; col++) {
                    if(assignment[row][col].equals(value)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //Helper function for forward checking. Check and update variables's domains on a single row, and return false if
    //a variable's domain is empty
    public static boolean updateRow(int rowNumber, Integer[][] assignment, Pos[][] variableArray) {
        ArrayList<Integer> assignedValues = new ArrayList<>();
        for(int col = 0; col < Program.COL; col++) {
            if(assignment[rowNumber][col] != 0) {
                assignedValues.add(assignment[rowNumber][col]);
            }
        }
        if(!assignedValues.isEmpty()) {
            for(int col = 0; col < Program.COL; col++) {
                if(!variableArray[rowNumber][col].isAssigned) {
                    variableArray[rowNumber][col].domain.removeAll(assignedValues);
                    if(variableArray[rowNumber][col].domain.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //Helper function for forward checking. Check and update variables's domains on a single column, and return false if
    //a variable's domain is empty
    public static boolean updateCol(int colNumber, Integer[][] assignment, Pos[][] variableArray) {
        ArrayList<Integer> assignedValues = new ArrayList<>();
        for(int row = 0; row < Program.ROW; row++) {
            if(assignment[row][colNumber] != 0) {
                assignedValues.add(assignment[row][colNumber]);
            }
        }
        if(!assignedValues.isEmpty()) {
            for(int row = 0; row < Program.ROW; row++) {
                if(!variableArray[row][colNumber].isAssigned) {
                    variableArray[row][colNumber].domain.removeAll(assignedValues);
                    if(variableArray[row][colNumber].domain.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //Helper function for forward checking. Check and update variables's domains in an area, and return false if
    //a variable's domain is empty
    public static boolean updateArea(int topLeftRow, int topLeftCol, Integer[][] assignment, Pos[][] variableArray) {
        ArrayList<Integer> assignedValues = new ArrayList<>();
        for(int row = topLeftRow; row < topLeftRow + Program.ROW/3; row++) {
            for(int col = topLeftCol; col < topLeftCol + Program.COL/3; col++) {
                if(assignment[row][col] != 0) {
                    assignedValues.add(assignment[row][col]);
                }
            }
        }
        if(!assignedValues.isEmpty()) {
            for(int row = topLeftRow; row < topLeftRow + Program.ROW/3; row++) {
                for(int col = topLeftCol; col < topLeftCol + Program.COL/3; col++) {
                    if(!variableArray[row][col].isAssigned) {
                        variableArray[row][col].domain.removeAll(assignedValues);
                        if(variableArray[row][col].domain.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}

//The class acts like a toolbox, provides heuristic and other functions
class Heuristic {

    //Select the next unassigned variable in variableArray
    public static Pos selectUnassignedVariable(Pos[][] variableArray) {
        ArrayList<Pos> result = minimumRemainingValue(variableArray);
        if(result.size() == 0) {
            System.out.println("oops, something went wrong");
            return null;

        }
        if(result.size() == 1) {
            return result.get(0);
        }
        else {
            result = degreeHeuristic(result, variableArray);
            if(result.size() == 0) {
                System.out.println("oops, something went wrong");
                return null;

            }
            else {
                return result.get(0);
            }
        }
    }

    //Return an ArrayList<Pos> of candidates which have the minimun domain size
    public static ArrayList<Pos> minimumRemainingValue(Pos[][] variableArray) {
        ArrayList<Pos> candidates = new ArrayList<>();
        int minDomainSize = Program.DOMAIN;
        for(int row = 0; row < Program.ROW; row++) {
            for(int col = 0; col < Program.COL; col++) {
                if(!variableArray[row][col].isAssigned && variableArray[row][col].domain.size() < minDomainSize){
                    minDomainSize = variableArray[row][col].domain.size();
                }
            }
        }
        for(int row = 0; row < Program.ROW; row++) {
            for(int col = 0; col < Program.COL; col++) {
                if(!variableArray[row][col].isAssigned && variableArray[row][col].domain.size() == minDomainSize){
                    candidates.add(variableArray[row][col]);
                }
            }
        }
        return candidates;
    }

    //Return an ArrayList<Pos> of candidates which have the most unassigned neighbors among the candidates returned by MRV
    public static ArrayList<Pos> degreeHeuristic(ArrayList<Pos> candidates, Pos[][] variableArray) {
        ArrayList<Pos> superCandidates = new ArrayList<>();
        int maxUnassignedNeighbor = 0;
        for(Pos p : candidates) {
            int n = countUnassignedNeighbors(p, variableArray);
            if(n > maxUnassignedNeighbor) {
                maxUnassignedNeighbor = n;
            }
        }
        for(Pos p : candidates) {
            if(countUnassignedNeighbors(p, variableArray) == maxUnassignedNeighbor) {
                superCandidates.add(p);
            }
        }
        return superCandidates;
    }

    //Helper function for degreeHeuristic. Return the number of unassigned neighbor of a variable
    public static int countUnassignedNeighbors(Pos variable, Pos[][] variableArray) {
        if(variable.isAssigned) {
            return -1;
        }
        HashSet<Pos> checkDup = new HashSet<>();
        int unassignedNeighbors = 0;
        int targetRow = variable.row;
        int targetCol = variable.col;
        //Row check
        for(int col = 0; col < Program.COL; col++) {
            if(variableArray[targetRow][col] != variable && !variableArray[targetRow][col].isAssigned) {
                unassignedNeighbors++;
                checkDup.add(variableArray[targetRow][col]);
            }
        }
        //Col check
        for(int row = 0; row < Program.ROW; row++) {
            if(variableArray[row][targetCol] != variable && !variableArray[row][targetCol].isAssigned) {
                unassignedNeighbors++;
                checkDup.add(variableArray[row][targetCol]);
            }
        }
        //Find its belonging non-overlapping area and area check
        if(targetRow < 3) {
            // #1
            if(targetCol < 3) {
                for(int row = 0; row < 3; row++) {
                    for(int col = 0; col < 3; col++) {
                        if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                            unassignedNeighbors++;
                        }
                    }
                }
            }
            // #2
            if(targetCol >= 3 && targetCol < 6) {
                for(int row = 0; row < 3; row++) {
                    for(int col = 3; col < 6; col++) {
                        if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                            unassignedNeighbors++;
                        }
                    }
                }
            }
            // #3
            if(targetCol >= 6) {
                for(int row = 0; row < 3; row++) {
                    for(int col = 6; col < 9; col++) {
                        if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                            unassignedNeighbors++;
                        }
                    }
                }

            }

        }

        if(targetRow >= 3 && targetRow < 6) {
            // #4
            if(targetCol < 3) {
                for(int row = 3; row < 6; row++) {
                    for(int col = 0; col < 3; col++) {
                        if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                            unassignedNeighbors++;
                        }
                    }
                }

            }
            // #5
            if(targetCol >= 3 && targetCol < 6) {
                for(int row = 3; row < 6; row++) {
                    for(int col = 3; col < 6; col++) {
                        if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                            unassignedNeighbors++;
                        }
                    }
                }

            }
            // #6
            if(targetCol >= 6) {
                for(int row = 3; row < 6; row++) {
                    for(int col = 6; col < 9; col++) {
                        if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                            unassignedNeighbors++;
                        }
                    }
                }

            }
        }

        if(targetRow >= 6) {
            // #7
            if(targetCol <= 2) {
                for(int row = 6; row < 9; row++) {
                    for(int col = 0; col < 3; col++) {
                        if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                            unassignedNeighbors++;
                        }
                    }
                }

            }
            // #8
            if(targetCol >= 3 && targetCol < 6) {
                for(int row = 6; row < 9; row++) {
                    for(int col = 3; col < 6; col++) {
                        if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                            unassignedNeighbors++;
                        }
                    }
                }

            }
            // #9
            if(targetCol >= 6) {
                for(int row = 6; row < 9; row++) {
                    for(int col = 6; col < 9; col++) {
                        if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                            unassignedNeighbors++;
                        }
                    }
                }

            }

        }
        //Find its belonging overlapping area and area check
        // #1
        if(targetRow >= 1 && targetRow < 4 && targetCol >= 1 && targetCol < 4) {
            for(int row = 1; row < 4; row++) {
                for(int col = 1; col < 4; col++) {
                    if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                        unassignedNeighbors++;
                    }
                }
            }
        }
        // #2
        if(targetRow >= 1 && targetRow < 4 && targetCol >= 5 && targetCol < 8) {
            for(int row = 1; row < 4; row++) {
                for(int col = 5; col < 8; col++) {
                    if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                        unassignedNeighbors++;
                    }
                }
            }
        }
        // #3
        if(targetRow >= 5 && targetRow < 8 && targetCol >= 1 && targetCol < 4) {
            for(int row = 5; row < 8; row++) {
                for(int col = 1; col < 4; col++) {
                    if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                        unassignedNeighbors++;
                    }
                }
            }
        }
        // #4
        if(targetRow >= 5 && targetRow < 8 && targetCol >= 5 && targetCol < 8) {
            for(int row = 5; row < 8; row++) {
                for(int col = 5; col < 8; col++) {
                    if(variableArray[row][col] != variable && !variableArray[row][col].isAssigned && checkDup.add(variableArray[row][col])) {
                        unassignedNeighbors++;
                    }
                }
            }
        }
        return unassignedNeighbors;
    }

    //Return a ArrayList<Integer> of ordered values. Default order is increasing order.
    public static ArrayList<Integer> orderDomainValues(Pos variable) {
        ArrayList<Integer> result = variable.domain;
        Collections.sort(result);
        return result;
    }
}

