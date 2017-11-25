/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package breakoutDisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javax.swing.JFrame;


/**
 *
 * @author Edward Newkirk
 */
public class CuttingBall {
    int SQUARE_SIZE;
    int LATTICE_WIDTH;
    int LATTICE_HEIGHT;
    boolean[][] Horizontals, Verticals; // to track which horizontal/vertical walls are still solid
    int i,j,k; // for use in for loops
    int[] lastEncounter;// for tracking which wall was last encountered - should have three elements, third = 0 if hor, 1 if vert
    int[][] tunnelCheck, tempCheck;// for looping over to check whether we're tunneling properly 
    int[][] timesEntered;// for tracking how often each square's been entered
    double x,y; //will be used to track the ball's position
    double startX, startY; // so I can mark the origin
    int xDir,yDir; // 1 means UR, -1 means DL
    int slope;
    public int currentStage;
    public long totalEncounters;
    public int hWallsHit, vWallsHit; // for tracking how many walls have been erased of each type
    public int timesReturned; // for counting the number of times it reentered the starting square
    int[] left, right, top, bottom; // for tracking the most extreme square the ball's hit in each direction
    public int stepsTaken; // tracks number of steps taken
    public boolean latticeSizeUpdated, tunnelFound; // to be set when the lattice size has been increased/when the slope-146 tunnel happens
    boolean wallHit; // for checking whether we've actually hit a wall
    int startingStage;//
    boolean moveByEncounters;
    int TUNNEL_PERIOD, TUNNEL_H, TUNNEL_V;
    
    public CuttingBall(int s, int side, int width, int height, int start, boolean moveType){
        SQUARE_SIZE = side;
        LATTICE_WIDTH = width;
        LATTICE_HEIGHT = height;
        slope = s;
        moveByEncounters = moveType;
        startingStage = start % (slope+1);
        currentStage = start % (slope+1);
        left = new int[2];
        right = new int[2];
        top = new int[2];
        bottom = new int[2];
        left[0] = width;
        right[0] = 0;
        top[1] = height;
        bottom[1] = 0;
        stepsTaken = 0;
        latticeSizeUpdated = false;
        tunnelFound = false;
        tunnelCheck = new int[10][3];        
        for(i = 0; i < 10; i++){
            for(j=0; j<3;j++){
                tunnelCheck[i][j] = 0;
            }
        }
        totalEncounters = 0;
        hWallsHit = 0;
        vWallsHit = 0;
        timesReturned =0;
        xDir = 1;
        yDir = 1;
        /* create a new array tracking the horizontal walls
         * the wall (i,j) will have its left endpoint at (i*SQUARE_SIZE,j*SQUARE_SIZE)
         */
        Horizontals = new boolean[LATTICE_WIDTH][LATTICE_HEIGHT+1];
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT+1;j++){
                Horizontals[i][j] = true;
            }
        }
        // and ditto for the vertical walls; here (i,j) -> top endpoint
        Verticals = new boolean[LATTICE_WIDTH+1][LATTICE_HEIGHT];
        for(i=0;i<LATTICE_WIDTH+1;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                Verticals[i][j] = true;
            }
        }
        timesEntered = new int[LATTICE_WIDTH][LATTICE_HEIGHT];
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                timesEntered[i][j] = 0;
            }
        }
        lastEncounter = new int[3];
        lastEncounter[0] = LATTICE_WIDTH/2;
        lastEncounter[1] = LATTICE_HEIGHT/2;
        if(start == 0){
            lastEncounter[2] = 1;
        }else{
            lastEncounter[2] = 0;
        }
        startX = (LATTICE_WIDTH/2 + .5)*SQUARE_SIZE;
        startY = (LATTICE_HEIGHT/2 + .5)*SQUARE_SIZE;
        TUNNEL_PERIOD = 122;
        TUNNEL_H = 2;
        TUNNEL_V = 0;
    }
    
    public void display(Graphics2D g2d){
        // should draw the ball + a line behind it showing the current direction of travel
        findLoc();
        g2d.setColor(Color.RED); // want the ball + line to be easily differentiable from the black walls
        Ellipse2D circle = new Ellipse2D.Double(x-2.0,y-2.0,4.0,4.0);
        g2d.fill(circle);
        g2d.draw(new Line2D.Double(x,y,x-3*xDir,y+3*yDir*slope));
    }
    
    public void move(JFrame errorFrame){
        wallHit = false;
        while(!wallHit){
            if(currentStage == slope){
                verEncounter();
                currentStage = 0;
            }
            else{
                horEncounter();
                currentStage++;
            }        
            if(lastEncounter[0] < 5){
                expandLeft();
            }
            if(lastEncounter[1]<5){
                expandUp();
            }
            if(LATTICE_WIDTH-lastEncounter[0] < 5){
                expandRight();
            }
            if(LATTICE_HEIGHT-lastEncounter[1]<5){
                expandDown();
            }
        }
        
        if(lastEncounter[0]<left[0]&&lastEncounter[2]==1){//if we just broke our record for leftmost v-wall hit, reset it
            left[0]=lastEncounter[0];
            left[1]=lastEncounter[1];
        }
        if(lastEncounter[0]>right[0]&&lastEncounter[2]==1){//if we just broke our record for leftmost v-wall hit, reset it
            right[0]=lastEncounter[0];
            right[1]=lastEncounter[1];
        }
        if(lastEncounter[1]>bottom[1]&&lastEncounter[2]==0){//if we just broke our record for leftmost v-wall hit, reset it
            bottom[0]=lastEncounter[0];
            bottom[1]=lastEncounter[1];
        }
        if(lastEncounter[1]<top[1]&&lastEncounter[2]==0){//if we just broke our record for leftmost v-wall hit, reset it
            top[0]=lastEncounter[0];
            top[1]=lastEncounter[1];
        }
        stepsTaken ++;
        /*
        if(stepsTaken % TUNNEL_PERIOD == 0){
            checkForTunnel();
        }
        */
    }
    
    private void checkForTunnel(){
        tempCheck = new int[10][3];
        for(i=0;i<9;i++){
            tempCheck[i] = tunnelCheck[i+1];
        }
        tempCheck[9][0] = lastEncounter[0];
        tempCheck[9][1] = lastEncounter[1];
        tempCheck[9][2] = currentStage;
        tunnelCheck = tempCheck;
        if(stepsTaken > 10*TUNNEL_PERIOD){
            i = tunnelCheck[0][0]-tunnelCheck[1][0];
            if((i == TUNNEL_H) || (i == -1*TUNNEL_H)){
                boolean possible = true;
                j = tunnelCheck[0][1]-tunnelCheck[1][1];
                if((j != TUNNEL_V) && (j != -1*TUNNEL_V)){
                    possible = false;
                }
                if(tunnelCheck[0][2]-tunnelCheck[1][2] != 0){
                    possible = false;
                }
                for(k=1; k<9; k++){
                    if(tunnelCheck[k][1]-tunnelCheck[k+1][1] != j){
                        possible = false;
                    }
                    if(tunnelCheck[k][2]-tunnelCheck[k+1][2] != 0){
                        possible = false;
                    }
                    if(tunnelCheck[k][0]-tunnelCheck[k+1][0] != i){
                        possible = false;
                    }
                }
                tunnelFound = possible;
            }
        }
    }
    
    public void horEncounter(){
        // if the next wall to be encountered is a horizontal one,
        totalEncounters++;
        if (xDir > 0){
            if(yDir > 0){
                if (lastEncounter[2]==0){
                    lastEncounter[1] --; // we're heading up from one horizontal wall to the next, so stay in the same column
                    if(Horizontals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Horizontals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        yDir = -1;
                        hWallsHit ++;
                    }
                }
                if (lastEncounter[2]==1){
                    lastEncounter[2]=0; //we're heading from a vertical wall to a horizontal one but keeping same UL corner
                    if(Horizontals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Horizontals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        yDir = -1;
                        hWallsHit ++;
                    }
                }
            }
            else if(yDir <0){
                if (lastEncounter[2]==0){
                    lastEncounter[1] ++; // we're heading down from one horizontal wall to the next, so stay in the same column
                    if(Horizontals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Horizontals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        yDir = 1;
                        hWallsHit ++;
                    }
                }
                if (lastEncounter[2]==1){
                    lastEncounter[2]=0; //we're heading from a vertical wall to a horizontal one
                    lastEncounter[1]++; //and UR corner is moving down a row
                    if(Horizontals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Horizontals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        yDir = 1;
                        hWallsHit ++;
                    }
                }
            }
            if (lastEncounter[0]==LATTICE_WIDTH/2 && lastEncounter[1]==LATTICE_HEIGHT/2){
                timesReturned++;
            }
        }
        else if (xDir < 0){
            if(yDir > 0){
                if (lastEncounter[2]==0){
                    lastEncounter[1] --; // we're heading up from one horizontal wall to the next, so stay in the same column
                    if(Horizontals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Horizontals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        yDir = -1;
                        hWallsHit ++;
                    }
                }
                if (lastEncounter[2]==1){
                    lastEncounter[2]=0; //we're heading from a vertical wall to a horizontal one
                    lastEncounter[0]--; //and moving UL corner one to the left
                    if(Horizontals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Horizontals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        yDir = -1;
                        hWallsHit ++;
                    }
                }
            }
            else if(yDir <0){
                if (lastEncounter[2]==0){
                    lastEncounter[1] ++; // we're heading down from one horizontal wall to the next, so stay in the same column
                    if(Horizontals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Horizontals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        yDir = 1;
                        hWallsHit ++;
                    }
                }
                if (lastEncounter[2]==1){
                    lastEncounter[2]=0; //we're heading from a vertical wall to a horizontal one while UL corner moves DL
                    lastEncounter[1] ++;
                    lastEncounter[0] --;
                    if(Horizontals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Horizontals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        yDir = 1;
                        hWallsHit ++;
                    }
                }
            }
            if (lastEncounter[0]==LATTICE_WIDTH/2+1 && lastEncounter[1]==LATTICE_HEIGHT/2){
                timesReturned++;
            }
        }
        if(xDir > 0 && !wallHit){
            //if we didn't hit a wall, we're entering a new square, so let's increment its counter
            timesEntered[lastEncounter[0]][lastEncounter[1]]++;
        } else if (xDir < 0 && !wallHit){
            //we index the walls by the top left, so the square we're entering may or may not have the same index as the wall encountered
            timesEntered[lastEncounter[0]-1][lastEncounter[1]]++;
        }
        if(moveByEncounters){
            wallHit = true;
        }
        
    }
    
    public void verEncounter(){
        // if the next wall to be encountered is a vertical one,
        totalEncounters++;
        if (xDir > 0){
            if(yDir > 0){
                if (lastEncounter[2]==0){
                    lastEncounter[0] ++;
                    lastEncounter[1]--;
                    lastEncounter[2]=1; //we're heading UR from horizontal to vertical, moving corner UR
                    if(Verticals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Verticals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        xDir = -1;
                        vWallsHit ++;
                    }
                }
                else if (lastEncounter[2]==1){
                    lastEncounter[0]++; //we're heading from one vertical wall to the next, keeping same row
                    if(Verticals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Verticals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        xDir = -1;
                        vWallsHit ++;
                    }
                }
                if (lastEncounter[0]==LATTICE_WIDTH/2 && lastEncounter[1]==LATTICE_HEIGHT/2+1){
                    timesReturned++;
                }
            }
            else if(yDir <0){
                if (lastEncounter[2]==0){
                    lastEncounter[0] ++;
                    lastEncounter[2]=1; //we're heading DR from horizontal to vertical, moving corner R
                    if(Verticals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Verticals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        xDir = -1;
                        vWallsHit ++;
                    }
                }
                else if (lastEncounter[2]==1){
                    lastEncounter[0]++; //we're heading from one vertical wall to the next, keeping same row
                    if(Verticals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Verticals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        xDir = -1;
                        vWallsHit ++;
                    }
                }
                if (lastEncounter[0]==LATTICE_WIDTH/2 && lastEncounter[1]==LATTICE_HEIGHT/2){
                    timesReturned++;
                }
            }
        }
        else if (xDir < 0){
            if(yDir > 0){
                if (lastEncounter[2]==0){
                    lastEncounter[1] --;
                    lastEncounter[2]=1; //we're heading UL from horizontal to vertical, moving corner U
                    if(Verticals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Verticals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        xDir = 1;
                        vWallsHit ++;
                    }
                }
                else if (lastEncounter[2]==1){
                    lastEncounter[0]--; //we're heading from one vertical wall to the next, keeping same row
                    if(Verticals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Verticals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        xDir = 1;
                        vWallsHit ++;
                    }
                }
                if (lastEncounter[0]==LATTICE_WIDTH/2 && lastEncounter[1]==LATTICE_HEIGHT/2+1){
                    timesReturned++;
                }
            }
            else if(yDir <0){
                if (lastEncounter[2]==0){
                    lastEncounter[2]=1; //we're heading DL from horizontal to vertical, keeping same corner
                    if(Verticals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Verticals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        xDir = 1;
                        vWallsHit ++;
                    }
                }
                else if (lastEncounter[2]==1){
                    lastEncounter[0]--; //we're heading from one vertical wall to the next, keeping same row
                    if(Verticals[lastEncounter[0]][lastEncounter[1]]){//if the next wall is still there
                        Verticals[lastEncounter[0]][lastEncounter[1]] = false;
                        wallHit = true;
                        xDir = 1;
                        vWallsHit ++;
                    }
                }
                if (lastEncounter[0]==LATTICE_WIDTH/2 && lastEncounter[1]==LATTICE_HEIGHT/2){
                    timesReturned++;
                }
            }
        }
        if(yDir > 0 && !wallHit){
            //if we didn't hit a wall, we're entering a new square, so let's increment its counter
            timesEntered[lastEncounter[0]][lastEncounter[1]-1]++;
        } else if (yDir < 0 && !wallHit){
            //we index the walls by the top left, so the square we're entering may or may not have the same index as the wall encountered
            timesEntered[lastEncounter[0]][lastEncounter[1]]++;
        }
        if(moveByEncounters){
            wallHit = true;
        }
    }
    
    public int perimeter(){
        // will count isolated edges inside the cleared region
        int p = 0;
        for (i = Math.max(left[0]-2,1); i < Math.min(right[0]+2,LATTICE_WIDTH-1); i++){ // for walls in columns we've gotten near
            for(j = Math.max(top[1]-2,1); j < Math.min(bottom[1]+2,LATTICE_HEIGHT-1); j++){ //and in rows we've gotten near
                if (Horizontals[i][j]){ // for still-solid horizontal walls
                    if(!Horizontals[i][j-1] || !Horizontals[i][j+1] || !Verticals[i][j-1] || !Verticals[i][j] || !Verticals[i+1][j-1] || !Verticals[i+1][j]){ 
                        // if a horizontal wall above or below or a vertical wall sharing an endpoint is missing, count this wall
                        p++;
                    }
                }
                if (Verticals[i][j]){ // and now analogously for vertical walls
                    if(!Verticals[i-1][j] || !Verticals[i+1][j] || !Horizontals[i-1][j] || !Horizontals[i][j] || !Horizontals[i-1][j+1] || !Horizontals[i][j+1]){ 
                        // if a horizontal wall above or below or a vertical wall sharing an endpoint is missing, count this wall
                        p++;
                    }
                }
            }
        }
        return p;
    }
    
    public int area(){
        int a = 0;
        for (i = Math.max(left[0]-2,1); i < Math.min(right[0]+2,LATTICE_WIDTH-1); i++){ // for walls in columns we've gotten near
            for(j = Math.max(top[1]-2,1); j < Math.min(bottom[1]+2,LATTICE_HEIGHT-1); j++){ //and in rows we've gotten near
                if (!Horizontals[i][j] || !Verticals[i][j] || !Horizontals[i][j+1] || !Verticals[i+1][j]){
                    a ++;
                }
            }
        }
        return a;
    }
    
    public int width(){
        return  right[0]-left[0]+2;
    }
    
    public int height(){
        return bottom[1]-top[1]+2;
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    
    public Dimension gridSize (){
        Dimension d = new Dimension(SQUARE_SIZE*LATTICE_WIDTH,LATTICE_HEIGHT*SQUARE_SIZE);
        return d;
    }
    
    public void resizeSquares (int newSize){
        x = x*newSize/SQUARE_SIZE;
        y = y*newSize/SQUARE_SIZE;
        startX = startX*newSize/SQUARE_SIZE;
        startY = startY*newSize/SQUARE_SIZE;
        SQUARE_SIZE = newSize;
    }
    
    
    private void expandUp(){
        //should add five rows above the current lattice
        // first, create new arrays which are all true
        boolean[][] newHorizontals = new boolean[LATTICE_WIDTH][LATTICE_HEIGHT+6];
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT+6;j++){
                newHorizontals[i][j] = true;
            }
        }
        boolean[][] newVerticals = new boolean[LATTICE_WIDTH+1][LATTICE_HEIGHT+5];
        for(i=0;i<LATTICE_WIDTH+1;i++){
            for(j=0;j<LATTICE_HEIGHT+5;j++){
                newVerticals[i][j] = true;
            }
        }
        int[][]newEntered = new int[LATTICE_WIDTH][LATTICE_HEIGHT+5];
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT+5;j++){
                newEntered[i][j] = 0;
            }
        }
        // then set everything below the top 5 rows to match the old;
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT+1;j++){
                newHorizontals[i][j+5] = Horizontals[i][j];
            }
        }
        for(i=0;i<LATTICE_WIDTH+1;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newVerticals[i][j+5] = Verticals[i][j];
            }
        }
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newEntered[i][j+5] = timesEntered[i][j];
            }
        }
        for(i=0;i<10;i++){
            tunnelCheck[i][1] += 5;
        }
        y += 5*SQUARE_SIZE;
        startY += 5*SQUARE_SIZE;
        Horizontals = newHorizontals;
        Verticals = newVerticals;
        timesEntered = newEntered;
        LATTICE_HEIGHT += 5;
        top[1]+=5;
        bottom[1]+=5;
        left[1]+=5;
        right[1]+=5;
        lastEncounter[1]+=5;
        latticeSizeUpdated = true;
    }
    private void expandDown(){
        //should add five rows below the current lattice
        // first, create new arrays which are all true
        boolean[][] newHorizontals = new boolean[LATTICE_WIDTH][LATTICE_HEIGHT+6];
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT+6;j++){
                newHorizontals[i][j] = true;
            }
        }
        boolean[][] newVerticals = new boolean[LATTICE_WIDTH+1][LATTICE_HEIGHT+5];
        for(i=0;i<LATTICE_WIDTH+1;i++){
            for(j=0;j<LATTICE_HEIGHT+5;j++){
                newVerticals[i][j] = true;
            }
        }
        int[][]newEntered = new int[LATTICE_WIDTH][LATTICE_HEIGHT+5];
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT+5;j++){
                newEntered[i][j] = 0;
            }
        }
        // then set everything above the bottom 5 rows to match the old;
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT+1;j++){
                newHorizontals[i][j] = Horizontals[i][j];
            }
        }
        for(i=0;i<LATTICE_WIDTH+1;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newVerticals[i][j] = Verticals[i][j];
            }
        }
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newEntered[i][j] = timesEntered[i][j];
            }
        }
        Horizontals = newHorizontals;
        Verticals = newVerticals;
        timesEntered = newEntered;
        LATTICE_HEIGHT += 5;
        latticeSizeUpdated = true;
    }
    private void expandLeft(){
        // should add five new columns on the left
        
        // first, create new arrays which are all true
        boolean[][] newHorizontals = new boolean[LATTICE_WIDTH+5][LATTICE_HEIGHT+1];
        for(i=0;i<LATTICE_WIDTH+5;i++){
            for(j=0;j<LATTICE_HEIGHT+1;j++){
                newHorizontals[i][j] = true;
            }
        }
        boolean[][] newVerticals = new boolean[LATTICE_WIDTH+6][LATTICE_HEIGHT];
        for(i=0;i<LATTICE_WIDTH+6;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newVerticals[i][j] = true;
            }
        }
        int[][]newEntered = new int[LATTICE_WIDTH+5][LATTICE_HEIGHT];
        for(i=0;i<LATTICE_WIDTH+5;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newEntered[i][j] = 0;
            }
        }
        // then set the right bit to match the old
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT+1;j++){
                newHorizontals[i+5][j] = Horizontals[i][j];
            }
        }
        // and ditto for the vertical walls
        for(i=0;i<LATTICE_WIDTH+1;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newVerticals[5+i][j] = Verticals[i][j];
            }
        }
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newEntered[i+5][j] = timesEntered[i][j];
            }
        }
        for(i=0;i<10;i++){
            tunnelCheck[i][0] += 5;
        }
        x += 5*SQUARE_SIZE;
        startX += 5*SQUARE_SIZE;
        Horizontals = newHorizontals;
        Verticals = newVerticals;
        timesEntered = newEntered;
        LATTICE_WIDTH += 5;
        top[0]+=5;
        bottom[0]+=5;
        left[0]+=5;
        right[0]+=5;
        lastEncounter[0]+=5;
        latticeSizeUpdated = true;
    }
    private void expandRight(){
        // should add five new columns on the right
        
        // first, create new arrays which are all true
        boolean[][] newHorizontals = new boolean[LATTICE_WIDTH+5][LATTICE_HEIGHT+1];
        for(i=0;i<LATTICE_WIDTH+5;i++){
            for(j=0;j<LATTICE_HEIGHT+1;j++){
                newHorizontals[i][j] = true;
            }
        }
        boolean[][] newVerticals = new boolean[LATTICE_WIDTH+6][LATTICE_HEIGHT];
        for(i=0;i<LATTICE_WIDTH+6;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newVerticals[i][j] = true;
            }
        }
        int[][]newEntered = new int[LATTICE_WIDTH+5][LATTICE_HEIGHT];
        for(i=0;i<LATTICE_WIDTH+5;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newEntered[i][j] = 0;
            }
        }
        // then set the left bit to match the old
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT+1;j++){
                newHorizontals[i][j] = Horizontals[i][j];
            }
        }
        // and ditto for the vertical walls
        for(i=0;i<LATTICE_WIDTH+1;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newVerticals[i][j] = Verticals[i][j];
            }
        }
        for(i=0;i<LATTICE_WIDTH;i++){
            for(j=0;j<LATTICE_HEIGHT;j++){
                newEntered[i][j] = timesEntered[i][j];
            }
        }
        Horizontals = newHorizontals;
        Verticals = newVerticals;
        timesEntered = newEntered;
        LATTICE_WIDTH += 5;
        latticeSizeUpdated = true;
    }
    public void findLoc(){
        //should get the exact location of the ball
        if(lastEncounter[2]==0){
            //if the last wall hit was a horizontal one,
            y = lastEncounter[1]*SQUARE_SIZE;
            double xTruncated = (2.0*currentStage-1)/(2.0*slope);
            if (xDir > 0){ // if the ball's moving right, then its position on the square hasn't been reflected
                x = xTruncated + lastEncounter[0];
            }
            if (xDir < 0){ // if the ball's moving left, its position has been reflected
                x = 1-xTruncated + lastEncounter[0];
            }
            x*=SQUARE_SIZE;
        }
        if(lastEncounter[2]==1){
            //if the last wall hit was a vertical one,
            x = lastEncounter[0]*SQUARE_SIZE;
            double yTruncated = 0.5;
            if (yDir < 0){ // if the ball's moving down, then its position on the square hasn't been reflected in terms of the coordinate system
                y = 1-yTruncated + lastEncounter[1];
            }
            if (yDir > 0){ // if the ball's moving up, its position has been reflected
                y = yTruncated + lastEncounter[1];
            }
            y*=SQUARE_SIZE;
        }
    }
    
    public void printTracker(){
        for(i=0;i<10;i++){
            System.out.println("Tracked Step " + i + ": (" + tunnelCheck[i][0] + ", " + tunnelCheck[i][1] + "), stage " + tunnelCheck[i][2]);
        }
    }
    
    public double normalizedX(){
        /* should return the X-coordinate of the ball, normalized so the ball starts at
         * (.5,.5) and squares have side length 1
         */
        findLoc();
        double a = x;
        a -= startX;
        a /= SQUARE_SIZE;
        a += .5;
        int b = (int) (10000000*a);
        return b/10000000.0;
    }
    public double normalizedY(){
        /* should return the Y-coordinate of the ball, normalized so the ball starts at
         * (.5,.5) and squares have side length 1
         */
        findLoc();
        double a = startY;
        a -= y;
        a /= SQUARE_SIZE;
        a += .5;
        int b = (int) (10000000*a);
        return b/10000000.0;
    }
    public int getSquareSize(){
        return SQUARE_SIZE;
    }
    
    public void printStatus(){
        //should print out most of the internal variables for diagnostic purposes
        System.out.println("Last Encounter: (" + lastEncounter[0]+", "+ lastEncounter[1]+", "+ lastEncounter[2]+")");
        System.out.println("Current Stage: " + currentStage);
        System.out.println("X Direction: " + xDir + " | Y Direction: " + yDir);
        System.out.println("-----------------------------");
    }
    
    public String getPrintTitle(){
        // should give an automatic title for saving as a jpg referencing slope, starting spot, and number of steps
        String title;
        title = "Slope " + slope + ", Start " + startingStage + ", Current Stage " + currentStage + ", Steps " + stepsTaken;
        return title;
    }
}