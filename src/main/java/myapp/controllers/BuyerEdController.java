package myapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import myapp.DBManager;
import myapp.data.Buyer;

import java.net.URL;
import java.util.ResourceBundle;

public class BuyerEdController implements Initializable {

    @FXML private TextField idBuyerField;
    @FXML private TextField orgNameField;

    private Stage dialogStage;
    private DBManager manager;
    private Buyer buyer;
    private boolean isOk = false;
    private boolean isNew = true;
    private int oldKey;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Пустой метод обязателен для интерфейса Initializable
    }

    // Метод инициализации окна
    public void initialize(Stage dialogStage, DBManager manager, Buyer buyer) {
        this.dialogStage = dialogStage;
        this.manager = manager;
        this.buyer = buyer;

        // Определение режима работы (добавление или редактирование)
        if (buyer.getId_buyer() == 0) {
            isNew = true;
            idBuyerField.setText("Новый");
        } else {
            isNew = false;
            idBuyerField.setText(String.valueOf(buyer.getId_buyer()));
            orgNameField.setText(buyer.getOrganization_name());
            oldKey = buyer.getId_buyer();
        }
    }

    // Обработчик кнопки "Сохранить"
    @FXML
    private void handleOk() {
        if (!isInputValid()) return;

        try {
            // 1. ЧИТАЕМ ID ИЗ ПОЛЯ И ЗАПИСЫВАЕМ В ОБЪЕКТ
            String idText = idBuyerField.getText().trim();
            if (idText.isEmpty() || idText.equals("Новый")) idText = "0";
            int newId = Integer.parseInt(idText);
            buyer.setId_buyer(newId);

            // 2. ЧИТАЕМ НАИМЕНОВАНИЕ
            buyer.setOrganization_name(orgNameField.getText().trim());

            // 3. Выполняем операцию в БД
            if (isNew) {
                if (manager.addBuyer(buyer)) isOk = true;
            } else {
                if (manager.updateBuyer(buyer, oldKey)) isOk = true;
            }
        } catch (NumberFormatException e) {
            myapp.gui.Dialogs.showDialog("Ошибка ввода",
                    "ID должен быть целым числом!",
                    Alert.AlertType.ERROR, dialogStage);
            return;
        }

        // 4. Закрываем окно при успехе
        if (isOk) dialogStage.close();
    }

    // Обработчик кнопки "Отмена"
    @FXML
    private void handleCancel() {
        isOk = false;
        dialogStage.close();
    }

    // Проверка корректности ввода
    private boolean isInputValid() {
        String errorMessage = "";

        if (orgNameField.getText() == null ||
                orgNameField.getText().trim().length() == 0) {
            errorMessage += "Не заполнено наименование организации!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.initOwner(dialogStage);
            alert.showAndWait();
            return false;
        }
    }

    // Метод возврата флага успешности
    public boolean isOkClicked() {
        return isOk;
    }
}