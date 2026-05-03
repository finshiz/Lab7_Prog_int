package myapp.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import myapp.DBManager;
import myapp.data.Product;
import myapp.data.SoldItem;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SoldProductEdController implements Initializable {
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField codeField;
    @FXML private TextField countField;
    @FXML private TextField priceField;
    @FXML private TextField ndsField;

    private Stage dialogStage;
    private DBManager manager;
    private SoldItem item;
    private boolean isOk = false, isNew = true;
    private int oldProductKey; // Переименовано для ясности

    @Override public void initialize(URL url, ResourceBundle rb) {}

    public void initialize(Stage dialogStage, DBManager manager, SoldItem item) {
        this.dialogStage = dialogStage;
        this.manager = manager;
        this.item = item;

        ArrayList<Product> products = new ArrayList<>(manager.getProducts());
        productCombo.setItems(FXCollections.observableArrayList(products));

        productCombo.setCellFactory(lv -> new ListCell<Product>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(p == null ? null : p.getProduct_name());
            }
        });
        productCombo.setButtonCell(new ListCell<Product>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(p == null ? null : p.getProduct_name());
            }
        });

        productCombo.setOnAction(e -> {
            Product p = productCombo.getValue();
            if (p != null) {
                codeField.setText(String.valueOf(p.getProduct_code()));
                priceField.setText(String.valueOf(p.getPrice_per_unit_without_NDS()));
                calcNds();
            }
        });
        countField.textProperty().addListener((obs, oldV, newV) -> calcNds());

        if (item.getId_product() != 0) {
            isNew = false;
            oldProductKey = item.getId_product();
            codeField.setText(String.valueOf(item.getProduct_code()));
            countField.setText(String.valueOf(item.getSold_product_count()));
            priceField.setText(String.valueOf(item.getPrice_without_nds()));
            ndsField.setText(String.valueOf(item.getNds_summ()));
            for(Product p : products) {
                if(p.getProduct_code() == item.getProduct_code()) {
                    productCombo.setValue(p);
                    break;
                }
            }
        }
    }

    private void calcNds() {
        try {
            double price = priceField.getText().isEmpty() ? 0 : Double.parseDouble(priceField.getText());
            int count = countField.getText().isEmpty() ? 0 : Integer.parseInt(countField.getText());
            double nds = (price * count) * 0.20;
            ndsField.setText(String.format("%.2f", nds));
        } catch (Exception e) {}
    }
    @FXML private void handleOk() {
        if (productCombo.getValue() == null || countField.getText().isEmpty()) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Выберите товар и укажите количество",
                    Alert.AlertType.ERROR, dialogStage);
            return;
        }

        try {
            Product p = productCombo.getValue();

            item.setProduct_code(p.getProduct_code());
            item.setProduct_name(p.getProduct_name());
            item.setSold_product_count(Integer.parseInt(countField.getText()));
            item.setPrice_without_nds(p.getPrice_per_unit_without_NDS());
            item.setNds_summ(Double.parseDouble(ndsField.getText()));

            if (item.getId_invoice() == 0) {
                myapp.gui.Dialogs.showDialog("Ошибка", "ID накладной не установлен! Невозможно добавить товар.", Alert.AlertType.ERROR, dialogStage);
                return;
            }

            boolean success = false;
            if (isNew) {
                success = manager.addSoldItem(item);
            } else {
                success = manager.updateSoldItem(item, oldProductKey);
            }
            
            if (success) {
                isOk = true;
                dialogStage.close();
            }

        } catch (NumberFormatException e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Неверный формат числа", Alert.AlertType.ERROR);
        } catch (Exception e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Неизвестная ошибка: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML private void handleCancel() {
        isOk = false;
        dialogStage.close();
    }

    public boolean isOkClicked() { return isOk; }
}