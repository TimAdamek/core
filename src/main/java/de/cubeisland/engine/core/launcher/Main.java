package de.cubeisland.engine.core.launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/resources/launcher/launcherMain.fxml"));
        primaryStage.setTitle("Launcher");//TODO
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.setHeight(400);
        primaryStage.setWidth(600);
        primaryStage.setResizable(false);
        primaryStage.show();

        // add checkboxes as proof of concept TODO be replaced with data from API
        VBox checkBoxPane = (VBox) primaryStage.getScene().lookup("#checkBoxPane");
        ArrayList<CheckBox> checkBoxes= new ArrayList();
        for (int i=0; i<100;i++){
            CheckBox cb = new CheckBox(Integer.toString(i));
            checkBoxes.add(cb);
        }
        checkBoxPane.getChildren().addAll(checkBoxes);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
