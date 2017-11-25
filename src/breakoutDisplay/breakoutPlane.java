/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package breakoutDisplay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

    


    /**
 *
 * @author Edward Newkirk
 */
public class breakoutPlane extends javax.swing.JPanel {
    int SQUARE_SIZE, LATTICE_WIDTH, LATTICE_HEIGHT;
    int i, j; // for use in 'for' loops
    boolean[][] Horizontal; // to track which horizontal walls are still solid
    boolean[][] Vertical; // to track which vertical walls are still solid
    CuttingBall ball; //it is a ball
    public boolean coloredSquares;
    public int markingInterval; // allows the graphic to mark every 5th line, every 10th line, etc. with a different color
    public Color clearedColor, partialColor, lineColor, markedLineColor;
    /**
     * Creates new form breakoutPlane
     */
    
    public breakoutPlane() {
        initComponents();
        clearedColor = Color.GREEN;
        partialColor = Color.YELLOW;
        lineColor = Color.lightGray;
        markedLineColor = Color.darkGray;
    }
    
    @Override
    public void paintComponent(Graphics g){
         Horizontal = ball.Horizontals;
         Vertical = ball.Verticals;
         Graphics2D g2d = (Graphics2D)g; //casting to Graphics2D so I can locate the ball more precisely
         g2d.setColor(Color.WHITE); // drawing the background; this is so I don't get an afterimage when I call repaint
         g2d.fillRect(0, 0, LATTICE_WIDTH*SQUARE_SIZE, LATTICE_HEIGHT*SQUARE_SIZE);
         SQUARE_SIZE = ball.SQUARE_SIZE;
         LATTICE_WIDTH = ball.LATTICE_WIDTH;
         LATTICE_HEIGHT = ball.LATTICE_HEIGHT;
         if (coloredSquares){ // if the colored squares toggle is set to on
             //iterate through the squares, coloring them green if fully cleared, yellow if partly cleared
             for(i=0;i<LATTICE_WIDTH;i++){
                for(j=0;j<LATTICE_HEIGHT;j++){
                    if (!Horizontal[i][j] & !Vertical[i][j] & !Horizontal[i][j+1] & !Vertical[i+1][j]){
                        //if all four walls of the square with top-left corner [i][j] are missing, color it green
                        g2d.setColor(clearedColor);
                        g2d.fillRect(i*SQUARE_SIZE,j*SQUARE_SIZE,SQUARE_SIZE,SQUARE_SIZE);
                    }
                    else if (!Horizontal[i][j] || !Vertical[i][j] || !Horizontal[i][j+1] || !Vertical[i+1][j]) {
                        // if some but not all are missing, color it yellow
                        g2d.setColor(partialColor);
                        g2d.fillRect(i*SQUARE_SIZE,j*SQUARE_SIZE,SQUARE_SIZE,SQUARE_SIZE);
                    }
                }
             }
             //iterate through the horizontal walls, drawing the ones still present, with every (markingInterval)th in a different color
             for(i=0;i<LATTICE_WIDTH;i++){
                for(j=0;j<LATTICE_HEIGHT+1;j++){
                    g2d.setColor(lineColor);
                    if (j % markingInterval == 0){
                        g2d.setColor(markedLineColor);
                    }
                    if(Horizontal[i][j]){
                        g2d.drawLine(i*SQUARE_SIZE,j*SQUARE_SIZE,(i+1)*SQUARE_SIZE,j*SQUARE_SIZE);
                    }
                }
            }
             //and ditto for the vertical walls
             for(i=0;i<LATTICE_WIDTH+1;i++){
                for(j=0;j<LATTICE_HEIGHT;j++){
                    g2d.setColor(lineColor);
                    if (i % markingInterval == 0){
                        g2d.setColor(markedLineColor);
                    }
                    if(Vertical[i][j]){
                        g2d.drawLine(i*SQUARE_SIZE,j*SQUARE_SIZE,i*SQUARE_SIZE,(j+1)*SQUARE_SIZE);
                    }
                }
            }
         }
         else { //if the colored squares toggle is set to off, no need to bother coloring them
             for(i=0;i<LATTICE_WIDTH;i++){
                for(j=0;j<LATTICE_HEIGHT+1;j++){
                    g2d.setColor(lineColor);
                    if (j % markingInterval == 0){
                        g2d.setColor(markedLineColor);
                    }
                    if(Horizontal[i][j]){
                        g2d.drawLine(i*SQUARE_SIZE,j*SQUARE_SIZE,(i+1)*SQUARE_SIZE,j*SQUARE_SIZE);
                    }
                }
            }
             //and ditto for the vertical walls
             for(i=0;i<LATTICE_WIDTH+1;i++){
                for(j=0;j<LATTICE_HEIGHT;j++){
                    g2d.setColor(lineColor); // make sure whe walls are black
                    if (i % markingInterval == 0){
                        g2d.setColor(markedLineColor);
                    }
                    if(Vertical[i][j]){
                        g2d.drawLine(i*SQUARE_SIZE,j*SQUARE_SIZE,i*SQUARE_SIZE,(j+1)*SQUARE_SIZE);
                    }
                }
            }
         }
         g2d.setColor(Color.BLUE); // mark the place the ball started with a blue circle
         g2d.fillOval((int)ball.startX-2, (int)ball.startY-2, 4, 4);
         ball.display(g2d);
    }
    public void setBall(CuttingBall orb){
        ball = orb;
        this.setPreferredSize(ball.gridSize());
    }
    public void findBall(){
        Rectangle nbd = new Rectangle((int)ball.getX() - 5, (int)ball.getY() - 5, 10, 10);
        scrollRectToVisible(nbd);
    }
    
    


/**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
     @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
