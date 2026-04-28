package myapp.controllers;

import myapp.DBManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {
    @FXML private ImageView imgView;
    private DBManager manager;
    private Stage dialogStage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void initialize(Stage dialogStage, DBManager manager) {
        this.dialogStage = dialogStage;
        this.manager = manager;

        InputStream imgStream = getClass().getResourceAsStream("/images/image.jpg");
        if (imgStream != null && imgView != null) {
            imgView.setImage(new Image(imgStream));
            imgView.setPreserveRatio(false);
            imgView.fitWidthProperty().bind(dialogStage.widthProperty());
            imgView.fitHeightProperty().bind(dialogStage.heightProperty().subtract(25));
        }
    }

    // --- Обработчики меню Файл ---
    @FXML private void handleOpen() { System.out.println("Меню: Открыть"); }
    @FXML private void handleSave() { System.out.println("Меню: Сохранить"); }
    @FXML private void handleExit() { dialogStage.close(); }

    // --- Обработчики меню Справочники (только открытие окон) ---
    @FXML private void handleBuyers() {
        openTableWindow("buyers", "Справочник покупателей");
    }
    @FXML private void handleProducts() {
        openTableWindow("product", "Справочник товаров");
    }
    @FXML private void handleGroups() {
        openTableWindow("product_group", "Группы товаров");
    }

    // --- Обработчики меню Отчеты ---
    @FXML private void handleSalesReport() { System.out.println("Отчет: Книга продаж"); }
    @FXML private void handlePaymentsReport() { System.out.println("Отчет: Книга оплат"); }
    @FXML private void handleNDSReport() { System.out.println("Отчет: НДС"); }

    // --- Метод открытия окна таблицы ---
    private void openTableWindow(String tableName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/TableViewWindow.fxml"));
            AnchorPane page = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(page));

            TableViewController controller = loader.getController();
            controller.initialize(stage, manager, tableName);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            myapp.gui.Dialogs.showDialog("Ошибка", "Не удалось открыть окно: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }
    @FXML private void handleSalesBook() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SalesBookView.fxml"));
            javafx.scene.layout.AnchorPane page = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Книга продаж");
            stage.setScene(new javafx.scene.Scene(page));

            SalesBookViewController ctrl = loader.getController();
            ctrl.initialize(stage, manager);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}