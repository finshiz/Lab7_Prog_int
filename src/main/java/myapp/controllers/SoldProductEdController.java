package myapp.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import myapp.DBManager;
import myapp.data.Product;
import myapp.data.SoldItem;

import java.util.ArrayList;

public class SoldProductEdController {
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField codeField;
    @FXML private TextField countField;
    @FXML private TextField priceField;
    @FXML private TextField ndsField;

    private Stage dialogStage;
    private DBManager manager;
    private SoldItem item;
    private boolean isOk = false;
    private int invoiceId;

    @FXML public void initialize() {}

    public void initialize(Stage dialogStage, DBManager manager, SoldItem item, int invoiceId) {
        this.dialogStage = dialogStage;
        this.manager = manager;
        this.item = item;
        this.invoiceId = invoiceId;

        ArrayList<Product> products = new ArrayList<>(manager.getProducts());
        productCombo.setItems(FXCollections.observableArrayList(products));

        setupComboBox(productCombo, Product::getProductName);

        productCombo.setOnAction(e -> {
            Product p = productCombo.getValue();
            if (p != null) {
                codeField.setText(String.valueOf(p.getProduct_code()));
                priceField.setText(String.valueOf(p.getPrice_per_unit_without_NDS()));
                calcNds();
            }
        });
        countField.textProperty().addListener((obs, oldV, newV) -> calcNds());

        // Если редактируем существующий элемент
        if (item.getIdProduct() != 0) {
            codeField.setText(String.valueOf(item.getProductCode()));
            countField.setText(String.valueOf(item.getSoldProductCount()));
            priceField.setText(String.valueOf(item.getPriceWithoutNds()));
            ndsField.setText(String.valueOf(item.getNdsSumm()));
            for(Product p : products) {
                if(p.getProduct_code() == item.getProductCode()) {
                    productCombo.setValue(p);
                    break;
                }
            }
        }
    }

    private <T> void setupComboBox(ComboBox<T> combo, java.util.function.Function<T, String> textFunc) {
        combo.setCellFactory(lv -> new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textFunc.apply(item));
            }
        });
        combo.setButtonCell(new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textFunc.apply(item));
            }
        });
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

            item.setProductCode(p.getProduct_code());
            item.setProductName(p.getProductName());
            item.setSoldProductCount(Integer.parseInt(countField.getText()));
            item.setPriceWithoutNds(p.getPrice_per_unit_without_NDS());
            item.setNdsSumm(Double.parseDouble(ndsField.getText()));
            item.setIdInvoice(invoiceId);

            if (invoiceId == 0) {
                myapp.gui.Dialogs.showDialog("Ошибка", "ID накладной не установлен! Невозможно добавить товар.", Alert.AlertType.ERROR, dialogStage);
                return;
            }

            boolean success = false;
            if (item.getIdProduct() == 0) {
                // Новый элемент - добавляем в БД
                success = manager.addSoldItem(item);
            } else {
                // Существующий элемент - обновляем в БД
                success = manager.updateSoldItem(item, item.getIdProduct());
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
