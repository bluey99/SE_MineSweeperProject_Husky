package controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.CellModel;
import view.CellView;

/**
 * Cell controller - manages individual cell view and model
 */
public class CellController {
    public CellModel cellModel;
    public CellView cellView;
    
    public CellController() {
        cellModel = new CellModel(this);
        cellView = new CellView(this);
        init();
    }
    
    public void init() {
        cellView.getChildren().clear();
        
        if (cellModel.isOpen()) {
            // Cell is opened - show its content
            if (cellModel.isMine()) {
                // Show mine
                cellView.getChildren().add(drawImg(
                    cellModel.cellSide,
                    cellModel.cellSide,
                    CellModel.mineImgURL
                ));
            } else if (cellModel.isSurprise()) {
                if (cellModel.isActivated()) {
                    // Surprise used - show as empty
                    cellView.getChildren().add(drawImg(
                        cellModel.cellSide,
                        cellModel.cellSide,
                        CellModel.numberImgURL(0)
                    ));
                } else {
                    // Show surprise icon (can be activated)
                    cellView.getChildren().add(drawImg(
                        cellModel.cellSide,
                        cellModel.cellSide,
                        CellModel.surpriseImgURL
                    ));
                }
            } else if (cellModel.isQuestion()) {
                if (cellModel.isActivated()) {
                    // Question answered - show as empty
                    cellView.getChildren().add(drawImg(
                        cellModel.cellSide,
                        cellModel.cellSide,
                        CellModel.numberImgURL(0)
                    ));
                } else {
                    // Show question icon (can be activated)
                    cellView.getChildren().add(drawImg(
                        cellModel.cellSide,
                        cellModel.cellSide,
                        CellModel.questionImgURL
                    ));
                }
            } else {
                // Show number (0-8 neighboring mines)
                cellView.getChildren().add(drawImg(
                    cellModel.cellSide,
                    cellModel.cellSide,
                    CellModel.numberImgURL(cellModel.getNeighborMinesNum())
                ));
            }
        } else {
            // Cell is not opened
            if (cellModel.isFlag()) {
                // Show flag
                cellView.getChildren().add(drawImg(
                    cellModel.cellSide,
                    cellModel.cellSide,
                    CellModel.flagImgURL
                ));
            } else {
                // Show cover
                cellView.getChildren().add(drawImg(
                    cellModel.cellSide,
                    cellModel.cellSide,
                    CellModel.coverImgURL
                ));
            }
        }
    }
    
    private ImageView drawImg(int width, int height, String imgURL) {
        Image img = new Image(imgURL);
        ImageView imgView = new ImageView(img);
        imgView.setFitHeight(height);
        imgView.setFitWidth(width);
        return imgView;
    }
}

//hi test