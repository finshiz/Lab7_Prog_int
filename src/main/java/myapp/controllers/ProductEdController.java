package myapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import myapp.DBManager;
import myapp.data.Product;
import java.net.URL;
import java.util.ResourceBundle;

public class ProductEdController implements Initializable {
    @FXML private TextField productCodeField;
    @FXML private TextField productNameField;
    @FXML private TextField priceField;
    @FXML private TextField groupCodeField;

    private Stage dialogStage;
    private DBManager manager;
    private Product product;
    private boolean isOk = false;
    private boolean isNew = true;
    private int oldKey;

    @Override public void initialize(URL url, ResourceBundle rb) {}

    public void initialize(Stage dialogStage, DBManager manager, Product product) {
        this.dialogStage = dialogStage;
        this.manager = manager;
        this.product = product;

        if (product.getProduct_code() == 0) {
            isNew = true;
            productCodeField.setText("0");
        } else {
            isNew = false;
            productCodeField.setText(String.valueOf(product.getProduct_code()));
            productNameField.setText(product.getProduct_name());
            priceField.setText(String.valueOf(product.getPrice_per_unit_without_NDS()));
            groupCodeField.setText(String.valueOf(product.getGroup_code()));
            oldKey = product.getProduct_code();
        }
    }

    @FXML private void handleOk() {
        if (!isInputValid()) return;

        try {
            product.setProduct_code(Integer.parseInt(productCodeField.getText()));
            product.setProduct_name(productNameField.getText());
            product.setPrice_per_unit_without_NDS(Double.parseDouble(priceField.getText()));
            product.setGroup_code(Integer.parseInt(groupCodeField.getText()));

            if (isNew) {
                if (manager.addProduct(product)) isOk = true;
            } else {
                if (manager.updateProduct(product, oldKey)) isOk = true;
            }
        } catch (NumberFormatException e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Неверный формат числа!", Alert.AlertType.ERROR);
            return;
        }
        if (isOk) dialogStage.close();
    }

    @FXML private void handleCancel() { isOk = false; dialogStage.close(); }

    private boolean isInputValid() {
        if (productCodeField.getText().isEmpty() || productNameField.getText().isEmpty() ||
                priceField.getText().isEmpty() || groupCodeField.getText().isEmpty()) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Заполните все поля!", Alert.AlertType.ERROR, dialogStage);
            return false;
        }
        return true;
    }
    public boolean isOkClicked() { return isOk; }
}