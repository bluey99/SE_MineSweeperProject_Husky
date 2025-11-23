# Minesweeper
<p>
<img src="https://github.com/tingying-he/minesweeper/blob/main/src/img/readme-screenshot.png" alt="screenshot" width="800"/>
</p>

Video: https://vimeo.com/user93477673

#### General Info
This project is an interactive minesweeper game.<br/>
It is the course project of Programming of Interactive System in University Paris Saclay.

#### Technologies
java version 11.0.8 <br>
javafx-sdk-11.0.2

#### Setup
To run this project, you should first setup Javafx:<br/>
How to get JavaFX and Java 11 working in IntelliJ IDEA:<br/>
https://www.jetbrains.com/help/idea/javafx.html<br/>

Where is the .jar:<br> minesweeper -> out -> artifacts -> MineSweeper_jar -> MineSweeper.jar <br/>
How to run the jar:<br/>
java --module-path %PATH_TO_YOUR_JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web -jar %PATH_TO_JAR%

## About the game
### Implemented functionalities
#### Basic minesweeper function
* The user can left click to open a cell. If there is a mine underneath, then the game over. If not, it shows how many mines are around. If there are no mines around, automatically open all adjacent cells with no neighboring mines.
* The user can right-click to flag/unflag a cell if she/he thinks there is a mine underneath.
* The user can start a new game or exit.

#### State indicator
* “Your Stars” label shows the number of stars the user has collected.
* The “Remaining time” label shows the remaining time.
    * There is a countdown timer, and if time is up, then the game is over. If the remaining time is less than 20s, the color of the text will change to red to warn the user.
* A label shows the number of flagged mines out of all mines.
* A label shows the number of opened cells out of all cells.
* If the user wins or loses, there will be a dialog box shows.
    * The user can see collected stars number, time spent in the game, and lose reason(just for lose) on the dialog box.

#### Advanced function
##### Collect stars 
* The user needs to collect at least 5 stars to win the game. If the star number is less than 5, its color is red, else its color is black.
* If the user drags a star icon in the minefield, the number of collected star +1.
* The user will see the animation on the right sidebar when dragging the star.

##### Gain time 
* If the user clicks a clock icon in the minefield, the remaining time +15s.
* The user will see the animation on the right sidebar when clicking the clock.
* If the remaining time is less than 20s, the system will remind the user to pick up clocks. The clock icons in the minefield will start flashing.

## Reference
Some ideas of developing the basic minesweeper game was refer to this java swing project:
https://github.com/liuyubobobo/Play-with-Algorithm-Visualization/tree/master/07-Mine-Sweeper/Chapter-07-Completed-Codes/Mine-Sweeper-Core




