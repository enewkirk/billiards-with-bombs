# billiards-with-bombs
Java code to simulate a dynamical system written up as "Billiards with Bombs" in the Journal of Experimental Math

More context on the problem can be found at http://www.math.brown.edu/~enewkirk/research.html. Instructions for the simulator:

This family of simulators are all java programs to simulate the mathematical problem in which a ball bounces around a square grid with some direction (here determined by its slope), erasing every wall it reflects off of. Each simulation allows for slightly different combinations of slope and starting point; in all cases, the ball starts out in the square [0,1]x[0,1] heading to the right. 

The program runs in two windows, a control panel which is always open while the program’s running (and can be closed to close the program) and a display panel showing the most recent simulation. The display panel always shows the ball as a red dot with a red tail behind it and the center of the starting square as a blue dot.

This particular program, called BreakoutCutting, is designed to cover integer slopes from any starting point in the unit square, using the mathematical principle of a cutting sequence to ease calculation.

OVERALL USAGE:

These are the functions which are always available. 

CAMERA CONTROL: 
To move the display so that the ball is on screen, click the “find ball” button. To center the display on a particular point, left-click on it; to center and zoom in (x2 square size), right-click.

ZOOM CONTROL: 
In addition to zooming in by right-clicking, you can also explicitly set the side length of a square in pixels at the “Square Size” spinner. To make your changes take effect, click “Resize Squares”.

MOVING THE BALL: 
When you click “Move Ball”, the ball will go through the specified number of moves. A move normally consists of going in a straight line until it hits a wall, changes direction, and erases the wall. If the checkbox for treating encounters as a separate move is ticked, then a move will just consist of going in a straight line until it reaches somewhere that initially had a wall, whether or not the wall is still there; for more on the distinction between between these two types of step, see my research notes, where they’re explicitly defined as collisions and encounters respectively.

SAVE AS JPEG: 
If you click this button, the program will save a full picture of the current simulation to its folder as a jpeg, with a name automatically derived from the starting point, starting slope, and number of steps. 

START CONDITIONS TAB:
This tab is used for determining the starting location, the initial direction, the size of the initial grid, and whether the ball moves at all at the start.  

DETERMINING STARTING LOCATION:
The starting location is represented as an integer from 0 to n, where n is the chosen slope. To map these numbers onto the unit square, draw a line starting at the bottom left-hand corner with slope n and continuing, wrapping from top to bottom when necessary, until it hits the top left corner. This will divide the unit square into n+1 regions; label the leftmost 0, and keep going from left to right.

INITIAL GRID SIZE: you start out with a grid with (initial rows) rows and (initial columns) columns. The program will automatically add more rows or columns when it gets within five spaces of the boundary.

STEPS AT START: The program will move this many steps before drawing its first picture. Mostly useful when you want to compare the first 10,000 steps across several different starts, as it saves a separate click to move the ball.

COLORED SQUARES:
This tab of the control panel is designed to control the colors of the display panel. It has two different kinds of coloring going on.

SQUARE COLORING: 
If the Toggle Color Mode button is selected (by default it isn’t), then squares whose walls have all been cleared will be marked in one color (default: green) and squares with some walls cleared but not all will be marked in another color (default: yellow). To change either color, select a color up top and then click the appropriate button next to Toggle Color Mode.

LINE COLORING: 
By default, the program makes every tenth vertical/horizontal line darker. You can change the color of the “default” and marked lines by selecting a color and clicking the appropriate button, or change the interval between marked lines.

CURRENT STATISTICS:

This panel tracks a variety of statistics, including:
- the x- and y- coordinates of the ball
- the height and width of the smallest rectangle containing every cleared wall
- the ratio of said height and width
- the total number of steps taken
- how many of those steps hit horizontal walls
- how many hit vertical walls
- how often the ball’s returned to its central square.

THE DIAGNOSTIC REPORT BUTTON: prints to console some internal variables that don't appear anywhere else on the control panel but might be of interest in trouble-shooting.
