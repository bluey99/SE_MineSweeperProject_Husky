# SE_MineSweeperProject_Husky
Minesweeper
A two-player JavaFX-based Minesweeper game with added difficulty levels, special cells, and a score/lives system.



Project Description:
This project is part of our Software Engineering course. It extends the classic Minesweeper game with:
- Two-player mode
- Shared score and lives
- Special cell types (Surprise and Question cells)
- Multiple difficulty levels



Current Status — End of Iteration 1

Features Implemented:
- Basic game logic across all three difficulty levels
- Setup screen with:
  - Two-player name input
  - Input validation (empty or invalid names)
- Game screen UI (front screen) implemented
- Question screen implemented
- History screen implemented
- Connection between screens is functional
- Game initializes correctly based on selected difficulty



Work in Progress:
- Special cell logic (Surprise and Question) is partially implemented  
- Cascade reveal (flood fill) needs further development  
- Some bugs remain in gameplay logic  
- QA testing is still ongoing  
- Layout improvements are needed on some scenes



How to Run:

Requirements
- Java 19  
- JavaFX SDK 19  
- Eclipse IDE  
- SceneBuilder (optional, for editing FXML files)

VM Arguments (Eclipse Run Configuration)

--module-path "path/to/javafx-sdk-19/lib" --add-modules javafx.controls,javafx.fxml
Make sure to replace "path/to/..." with the actual path to your local JavaFX SDK installation.



User Instructions (Current Flow):

1. Launch the application  
2. Enter two player names  
3. Select a difficulty level  
4. Game board loads with the selected settings  
5. First player may begin revealing cells

Note: Some features such as question handling, surprise effects, and end-game saving may not be fully functional yet.

Project Structure

src/
├── controller/       JavaFX controllers for each screen
├── model/            Core game logic and data models
├── view/             FXML files and resource folders (images, sounds)
