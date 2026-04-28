package myapp.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.Optional;

public class Dialogs {
    public static void showDialog(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showDialog(String title, String message, AlertType type, Stage stage) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        if (stage != null) alert.initOwner(stage);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean showConfirmDialog(String question, Stage stage) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        if (stage != null) alert.initOwner(stage);
        alert.setHeaderText(null);
        alert.setContentText(question);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}