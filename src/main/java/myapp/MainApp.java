package myapp;
import myapp.gui.Dialogs;
import myapp.controllers.LoginDialogController;
import myapp.controllers.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;


public class MainApp extends Application {
    private DBManager manager = null;

    @Override
    public void start(Stage arg0) throws Exception {
        if (showLoginDialog()) {
            showMainWnd();
        } else {
            System.exit(0);
        }
    }

    private boolean showLoginDialog() {
        manager = new DBManager();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/LoginDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Авторизация");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            InputStream iconStream = getClass().getResourceAsStream("/images/connect.png");
            if (iconStream != null) dialogStage.getIcons().add(new Image(iconStream));

            LoginDialogController controller = loader.getController();
            controller.initialize(dialogStage, manager);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showMainWnd() {
        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/MainWindow.fxml"));

            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Учет продаж и налогообложения");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            InputStream iconStream = getClass().getResourceAsStream("/images/main_icon.png");
            if (iconStream != null) {
                Image image = new Image(iconStream);
                dialogStage.getIcons().add(image);
            }

            MainWindowController controller = loader.getController();

            controller.initialize(dialogStage, manager);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Dialogs.showDialog("Ошибка", "Не удалось загрузить главное окно: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        launch(args);
    }
}