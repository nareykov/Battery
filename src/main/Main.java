package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.beans.EventHandler;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("window.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        primaryStage.setTitle("Battery Manager");
        primaryStage.setScene(new Scene(root, 633, 453));
        primaryStage.setOnCloseRequest(windowEvent -> {
            controller.close();
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
