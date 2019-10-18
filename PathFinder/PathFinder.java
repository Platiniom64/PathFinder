/*This program was created by Vladimir Hanin on the 15/10/2019
Please use Java jdk 8
*/ 

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class PathFinder{

    //this is the size of the window in pixel, the more pixels the more time the computer takes to write the file
    static int size = 600;

    //this is the number of boxes per line, make sure that it is a factor of the number of pixels
    static int boxes_per_line = 30;
    static int pixels_per_box = size/boxes_per_line;
    static int offset = size * pixels_per_box;

    //start coordinates:
    static int startCoord = (3*offset)/2 + (3*pixels_per_box)/2;                    //this is the middle of the box in the second row and column
    static int endCoord = startCoord+(boxes_per_line-3)*(pixels_per_box+offset);

    //this is the list for the 4 possible moves that the program can execute to make a new path
    static String[] listOfMoves = {"UP", "RIGHT", "DOWN", "LEFT"};

    //list that holds all the obstacles
    static ArrayList<Integer> listObstacles = new ArrayList<>();
    //this list contains the "walls" of the map (these don't get displayed)
    static ArrayList<Integer> listLimits = new ArrayList<>();

    //this will record all the paths that are abbandonned when they hit a wall or an obstacle
    static ArrayList<Path> listofabadonnedPaths = new ArrayList<>();
    

    //This sets the value of the three fixels
    static float[] green = new float[size*size*size];
    static float[] red = new float[size*size*size];
    static float[] blue = new float[size*size*size];


    //this method takes a position on the map and sets the colour of that point accordingly
    static void set(int pos, float r, float g, float b){
        red[pos] = r;
        green[pos] = g;
        blue[pos] = b;
    }

    //this converts the value from set to a value that the PPM file can read (we write 1 for on and 0 for off but the ppm file read them as 255 and 0 respectively)
    static int colourToInt(float c){
        return(int)(c*255);
    }

    //this method writes the file, by creating the PPM file with the colours
    static void write(String fileName) throws FileNotFoundException{
        //This creates the File
        PrintStream ps = new PrintStream(fileName);

        //These are the parameters to create the PPM picture
        ps.println("P3");
        ps.println(size + " " + size);
        ps.println(255);

        //this sets the value for each fixel
        for (int i = 0; i < size*size; ++i){
            ps.println(colourToInt(red[i]) + " ");
            ps.println(colourToInt(green[i]) + " ");
            ps.println(colourToInt(blue[i]) + " ");
        }
        ps.close();
    }

    //this creates a box at the given coordinate (must be the middle of the box)
    static void createBox(int middleBox, int red, int green, int blue){
        for (Integer z = middleBox - (offset/2)+1+size; z < middleBox + (offset/2) -1; z = z+size){
            for (Integer y = -(pixels_per_box/2); y < (pixels_per_box/2)-1; y++){
                set(z+y, red,green,blue);
            }
        }
    }

    static void create1Path (Path path, int movement, String direction, ArrayList<Path> ListofPaths){
        //a new path is created and as its coordinates it takes the pevious ones and adjusts its move
        Path newPath = new Path(path);
        newPath.coord = path.coord + movement;
                    
        //checks if the new path created has as coordinate one of the obstacles
        boolean hitobstacle = false;
        for (Integer i : listObstacles){
            if (newPath.coord == i){
                //as an obstacle has been hit, the path is abbandonned
                listofabadonnedPaths.add(newPath);
                hitobstacle = true;
            }
        }
        if (hitobstacle == false){
        newPath.listMoves.add(direction);         //as no obstacles have been hit, the new path is added to the list of new paths
        ListofPaths.add(newPath);
        listObstacles.add(newPath.coord);         //this is to avoid that a future path comes back where other paths have alrady been created
        }
    }


    //this method will take one path and creates 4 new paths adding the 4 moves to it
    static void MoveDirections(Path path, ArrayList<Path> ListofPaths, ArrayList<Integer> listObstacles){
        for (String move : listOfMoves){
            if (move == "UP"){
                create1Path(path, -offset, "up", ListofPaths);
            }
            
            if (move == "DOWN"){
                create1Path(path, offset, "down", ListofPaths);
            }

            if (move == "LEFT"){
                create1Path(path, -pixels_per_box, "left", ListofPaths);
            }

            if (move == "RIGHT"){
                create1Path(path, pixels_per_box, "right", ListofPaths);
            }   
        }
    }

    static void createPaths(ArrayList<Path> ListofoldPaths, ArrayList<Path> ListofnewPaths) throws FileNotFoundException{
        
        //this loop takes the previously made paths and for each one of them creates 4 new ones and adds them to the new list of paths
        for (Path path : ListofoldPaths){
            MoveDirections(path, ListofnewPaths, listObstacles);
        }

        //check if the previous step has created new paths, if not then it means that all the current paths are at deadends, so display all of them
        if (ListofnewPaths.size() == 0) {
            for (Path path : listofabadonnedPaths){
                int createPathEndCoord = startCoord;
                for (String move : path.listMoves){
                    if (move == "up"){
                        createPathEndCoord = createPathEndCoord - offset;
                        createBox(createPathEndCoord, 1, 1, 0);
                    }
                    if (move == "down"){
                        createPathEndCoord = createPathEndCoord + offset;
                        createBox(createPathEndCoord, 1, 1, 0);
                    }
                    if (move == "right"){
                        createPathEndCoord = createPathEndCoord + pixels_per_box;
                        createBox(createPathEndCoord, 1, 1, 0);
                    }
                    if (move == "left"){
                        createPathEndCoord = createPathEndCoord - pixels_per_box;
                        createBox(createPathEndCoord, 1, 1, 0);
                    }
                }
            }
            //this is only so that when no paths are found, you still display the image and exit the program
            write("PathFinderMap.ppm");
            System.exit(0);
        }

        //this clears the list of the old paths, we are only intersted in the new paths that have been created so we only keep the new ones
        ListofoldPaths.clear();

        //this takes the elements form the new list of paths and add them to the list of the old paths
        for (Path path : ListofnewPaths) {
            ListofoldPaths.add(path);
        }
        
        //this clears the list of new paths so that next time the "movedirectoin" method is called then there will be no erros
        ListofnewPaths.clear();
    }

    static void algorithm(ArrayList<Path> ListofoldPaths, ArrayList<Path> ListofnewPaths) throws FileNotFoundException {
        //this variable helps to stop the program when a path has been found
        boolean pathFound = false;

        //this will loop endlessly untill a path has been found
        while (pathFound == false){
            //creates more paths witht the ones already created
            createPaths(ListofoldPaths, ListofnewPaths);

            //this loops searches for a path with the coordinate of the endCoord, after the new paths have been created
            for (Path path : ListofoldPaths){
                if (path.coord == endCoord){
                    path.listMoves.remove(path.listMoves.size()-1);  //this is sothat the last move does not make a box at the end hence the end box stays red

                    //this colours the path of the one that has found the endCoord
                    int createPathEndCoord = startCoord;
                    for (String move : path.listMoves){
                        if (move == "up"){
                            createPathEndCoord = createPathEndCoord - offset;
                            createBox(createPathEndCoord, 1, 0, 1);
                        }
                        if (move == "down"){
                            createPathEndCoord = createPathEndCoord + offset;
                            createBox(createPathEndCoord, 1, 0, 1);
                        }
                        if (move == "right"){
                            createPathEndCoord = createPathEndCoord + pixels_per_box;
                            createBox(createPathEndCoord, 1, 0, 1);
                        }
                        if (move == "left"){
                            createPathEndCoord = createPathEndCoord - pixels_per_box;
                            createBox(createPathEndCoord, 1, 0, 1);
                        }
                    }
                    //this will break the while loop
                    pathFound = true;

                    //write out the File once a path has been found
                    write("PathFinderMap.ppm");

                    //this ensures that once the path has been found, the program stops checking for the other ones it breaks the for loop, it breaks the for loop
                    break;
                }
            }
        }
    }



    public static void main(String[] args) throws FileNotFoundException {

        //--------------------Here we create the picture-------------------------

        //this turns every pixel to white
        for (int i = 0; i < size*size; i++){
            set (i, 1, 1, 1);
        }

        //this sets vertical lines
        for (int i = 0; i < size*size; i = i+(size/boxes_per_line)){
            set(i, 0, 0, 0);
        }

        //this sets horizontal lines
        for (int i = offset; i < size*size; i = i+offset){
            //this loop creates the horizontal line for a given size on the picture
            for (int y = 0; y<size; y++){
                set(i+y, 0,0,0);
            }
        }

        //set the starting box
        createBox(startCoord, 0, 1, 0);
        //sets the ending box
        createBox(endCoord, 1, 0, 0);

        //--------------------now we have obstacles---------------------

        //this are the borders of the map to the left
        for (int y = startCoord-offset-2*pixels_per_box; y<startCoord-offset-2*pixels_per_box + offset*(boxes_per_line+1)+1; y = y+offset){
            listLimits.add(y);
        }
        //this are the borders of the map to the top
        for (int y = startCoord-2*offset-2*pixels_per_box; y<startCoord-2*offset-2*pixels_per_box+pixels_per_box*(boxes_per_line+1)+1; y = y+pixels_per_box){
            listLimits.add(y);
        }
        //this are the borders of the map to the right
        for (int y = startCoord-offset+(boxes_per_line-1)*pixels_per_box; y<startCoord-offset+(boxes_per_line-1)*pixels_per_box+offset*(boxes_per_line+1)+1; y = y+offset){
            listLimits.add(y);
        }
        //this are the borders of the map to the bottom
        for (int y = startCoord-pixels_per_box+ (boxes_per_line-1)*offset; y<startCoord-pixels_per_box+ (boxes_per_line)*offset + pixels_per_box*(boxes_per_line+1)+1; y = y+pixels_per_box){
            listLimits.add(y);
        }
        
        //this is to add the obstacles in the way of the path, randomly chosen
        Random i = new Random();
        for (int x = 0; x <= boxes_per_line-1; x++){
            for (int y = 0; y <= boxes_per_line-1; y++){
                int output = i.nextInt(3);      //change this value of the random number to make more or less obstacles
                if ((output == 0) && ((x != 1) || (y != 1)) && ((x != (boxes_per_line-2)) || (y != (boxes_per_line-2)))) {
                    listObstacles.add(startCoord - offset - pixels_per_box + x*pixels_per_box + y*offset);
                }
            }
        }

        //this displays the obstacles
        for (Integer obstacle : listObstacles){
            createBox(obstacle, 0,0,0);
        }

        //now we can add the list of limits of the maps to the list of obstacles (now that the actuals obstacles have been displayed)
        for (int y : listLimits){
            listObstacles.add(y);
        }
        
        //this is sothat a path can't go back to the start
        listObstacles.add(startCoord);

        //----------------------start of algorithm----------------------------------
        
        //this creates the first path which helps for the next paths to be created
        Path pathStart = new Path(startCoord);
        pathStart.listMoves.add("start");


        //old paths contain all the paths that have been created, and new paths contains the ones that have just been created (avoid loop error)
        ArrayList<Path> ListofoldPaths = new ArrayList<>();
        ListofoldPaths.add(pathStart);                          //this enables the program to start, it must have a start coordinate
        ArrayList<Path> ListofnewPaths = new ArrayList<>();
        
        //this launches the search of the path to the endcoordinate
        algorithm(ListofoldPaths, ListofnewPaths);
    }
}


//this class is for each path, it holds the end coordinate of each path, and also the list of the moves it did to get to that end coordinate
class Path{
    public int coord;                                           
    public ArrayList<String> listMoves = new ArrayList<>();

    public Path(int coord){
        this.coord = coord;
        this.listMoves = new ArrayList<String>();

    }
    public Path(Path lastpath){
        this.coord = lastpath.coord;
        this.listMoves = new ArrayList<String>(lastpath.getListMoves());
    }
    //this is sothat when we create a new path is gets the moves from the previous one and gets the moves independently
    public ArrayList<String> getListMoves(){
        return (new ArrayList<String>(this.listMoves));
    }
}