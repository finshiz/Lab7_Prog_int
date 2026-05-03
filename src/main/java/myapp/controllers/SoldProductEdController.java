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
    private int oldKey;

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
            oldKey = item.getId_product();
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
        // 1. Проверка валидности
        if (productCombo.getValue() == null || countField.getText().isEmpty()) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Выберите товар и укажите количество",
                    Alert.AlertType.ERROR, dialogStage);
            return;
        }

        try {
            Product p = productCombo.getValue();

            // 2. Заполнение объекта данными из формы
            item.setProduct_code(p.getProduct_code());
            item.setProduct_name(p.getProduct_name()); // Для отображения в таблице
            item.setSold_product_count(Integer.parseInt(countField.getText()));

            // Берем цену из выбранного товара (справочника)
            item.setPrice_without_nds(p.getPrice_per_unit_without_NDS());

            // Берем НДС из поля
            item.setNds_summ(Double.parseDouble(ndsField.getText()));

            // 3. КРИТИЧЕСКАЯ ПРОВЕРКА ID НАКЛАДНОЙ
            if (item.getId_invoice() == 0) {
                myapp.gui.Dialogs.showDialog("Ошибка", "ID накладной не установлен! " +
                        "Невозможно добавить товар.", Alert.AlertType.ERROR, dialogStage);
                return;
            }

            // 4. Вызов менеджера
            if (isNew) {
                if (manager.addSoldItem(item)) {
                    isOk = true;
                }
            } else {
                if (manager.updateSoldItem(item, oldKey)) {
                    isOk = true;
                }
            }

        } catch (NumberFormatException e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Неверный формат числа", Alert.AlertType.ERROR, dialogStage);
        } catch (Exception e) {
            e.printStackTrace();
            myapp.gui.Dialogs.showDialog("Ошибка", "Неизвестная ошибка: " + e.getMessage(), Alert.AlertType.ERROR, dialogStage);
        }

        // 5. Закрытие окна при успехе
        if (isOk) {
            dialogStage.close();
        }
    }

    @FXML private void handleCancel() {
        isOk = false;
        dialogStage.close();
    }

    public boolean isOkClicked() { return isOk; }
}