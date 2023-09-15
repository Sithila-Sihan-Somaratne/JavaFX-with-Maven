import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/*
* JDK 11
* Build System - IntelliJ
* jfoenix -9.0.1
* javafx-SDK 19.0.2.1
* JavaFx Project*/

public class AppInitializer extends Application { //javafx.application  // alt+enter --> implement methods
    public static void main(String[] args) { //psvm -> tab
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("view/DashBoard.fxml")))));
        stage.setResizable(false);
        stage.show();
        System.err.close();
        System.setErr(System.out);
    }
}
