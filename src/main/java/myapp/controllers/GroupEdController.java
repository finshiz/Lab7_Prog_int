package myapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import myapp.DBManager;
import myapp.data.ProductGroup;
import java.net.URL;
import java.util.ResourceBundle;

public class GroupEdController implements Initializable {
    @FXML private TextField groupCodeField;
    @FXML private TextField groupNameField;
    @FXML private TextField ndsField;

    private Stage dialogStage;
    private DBManager manager;
    private ProductGroup group;
    private boolean isOk = false;
    private boolean isNew = true;
    private int oldKey;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void initialize(Stage dialogStage, DBManager manager, ProductGroup group) {
        this.dialogStage = dialogStage;
        this.manager = manager;
        this.group = group;

        if (group.getGroup_code() == 0) {
            isNew = true;
            groupCodeField.setText("0");
        } else {
            isNew = false;
            groupCodeField.setText(String.valueOf(group.getGroup_code()));
            groupNameField.setText(group.getGroup_name());
            ndsField.setText(String.valueOf(group.getNDS_percent()));
            oldKey = group.getGroup_code();
        }
    }

    @FXML private void handleOk() {
        if (!isInputValid()) return;

        try {
            group.setGroup_code(Integer.parseInt(groupCodeField.getText()));
            group.setGroup_name(groupNameField.getText());
            group.setNDS_percent(Double.parseDouble(ndsField.getText()));

            if (isNew) {
                if (manager.addGroup(group)) isOk = true;
            } else {
                if (manager.updateGroup(group, oldKey)) isOk = true;
            }
        } catch (NumberFormatException e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Неверный формат числа!", Alert.AlertType.ERROR);
            return;
        }

        if (isOk) dialogStage.close();
    }

    @FXML private void handleCancel() {
        isOk = false;
        dialogStage.close();
    }

    private boolean isInputValid() {
        if (groupCodeField.getText().isEmpty() || groupNameField.getText().isEmpty() || ndsField.getText().isEmpty()) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Заполните все поля!", Alert.AlertType.ERROR, dialogStage);
            return false;
        }
        return true;
    }

    public boolean isOkClicked() { return isOk; }
}