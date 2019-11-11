package com.gopherit;

import com.gopherit.stages.MainStage;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) {
        primaryStage = new MainStage();
        primaryStage.show();
    }

    public static void main(String[] args) {
        Main.launch(args);
    }
}