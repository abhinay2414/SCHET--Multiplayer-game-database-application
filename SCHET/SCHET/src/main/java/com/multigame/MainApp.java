package com.multigame;

import com.multigame.util.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * MainApp — the entry point of the JavaFX application.
 *
 * JavaFX lifecycle:
 *   1. main()  → launch(args) triggers JavaFX startup
 *   2. start() → called by JavaFX to build and show the window
 *   3. stop()  → called by JavaFX when the user closes the window
 *
 * The FXML file (main.fxml) defines the entire UI layout.
 * FXMLLoader reads it, builds all the UI nodes, and wires them
 * to MainController automatically via the fx:controller attribute.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Load the FXML layout file from the resources folder
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/multigame/view/main.fxml")
        );

        // Build the scene from the loaded FXML (1000 x 680 pixels)
        Scene scene = new Scene(loader.load(), 1000, 680);

        // Configure the application window
        primaryStage.setTitle(" SCHET ");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(860);
        primaryStage.setMinHeight(560);

        // Show the window
        primaryStage.show();
    }

    /**
     * Called automatically when the user closes the window.
     * We close the MySQL connection here to avoid connection leaks.
     */
    @Override
    public void stop() {
        DBConnection.closeConnection();
    }

    /**
     * Standard Java entry point.
     * launch() hands control to JavaFX which then calls start().
     */
    public static void main(String[] args) {
        launch(args);
    }
}
