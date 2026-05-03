package myapp.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import myapp.DBManager;
import myapp.data.Buyer;
import myapp.data.Product;
import myapp.data.SalesInvoice;
import myapp.data.SoldItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class SalesBookEdController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Buyer> buyerCombo;
    @FXML private TextField totalField;
    @FXML private TextField payField;
    @FXML private Button btnOk, btnCancel;

    @FXML private TableView<SoldItem> detailTable;
    @FXML private TableColumn<SoldItem, String> colProdCode;
    @FXML private TableColumn<SoldItem, String> colProdName;
    @FXML private TableColumn<SoldItem, Integer> colCount;
    @FXML private TableColumn<SoldItem, Double> colPrice;
    @FXML private TableColumn<SoldItem, Double> colNds;
    @FXML private TableColumn<SoldItem, Double> colTotalPrice;
    @FXML private Button btnNewDetail, btnEditDetail, btnDeleteDetail;

    private Stage dialogStage;
    private DBManager manager;
    private SalesInvoice invoice;
    private SalesBookViewController parentCtrl;

    private ObservableList<SoldItem> detailData = FXCollections.observableArrayList();
    private boolean isNew = true;
    private int oldKey;
    private boolean isOk = false;

    private Map<Integer, Product> productsMap = new HashMap<>();

    public void initialize(Stage dialogStage, DBManager manager, SalesInvoice invoice, SalesBookViewController parentCtrl) {
        this.dialogStage = dialogStage;
        this.manager = manager;
        this.invoice = invoice;
        this.parentCtrl = parentCtrl;
        this.isOk = false;

        // Загрузка покупателей
        ObservableList<Buyer> buyers = FXCollections.observableArrayList(manager.loadBuyersForCombo());
        buyerCombo.setItems(buyers);
        setupComboBox(buyerCombo, Buyer::getOrganization_name);

        // Загрузка товаров для маппинга
        for (Product p : manager.getProducts()) {
            productsMap.put(p.getProduct_code(), p);
        }

        // Инициализация полей
        if (invoice.getId_invoice() == 0) {
            isNew = true;
            datePicker.setValue(java.time.LocalDate.now());
            totalField.setText("0.00");
            payField.setText("0.00");
            detailData = FXCollections.observableArrayList();
        } else {
            isNew = false;
            oldKey = invoice.getId_invoice();
            datePicker.setValue(invoice.getSell_date());
            totalField.setText(String.valueOf(invoice.getSelling_price()));
            payField.setText(String.valueOf(invoice.getPayment_cost()));

            for (Buyer b : buyers) {
                if (b.getId_buyer() == invoice.getId_buyer()) {
                    buyerCombo.setValue(b);
                    break;
                }
            }
            detailData = FXCollections.observableArrayList(manager.loadSoldItems(invoice.getId_invoice()));
        }

        detailTable.setItems(detailData);
        setupTableColumns();
        setEditMode(true);
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

    private void setupTableColumns() {
        colProdCode.setCellValueFactory(c -> {
            int kod = c.getValue().getProductCode();
            return new SimpleStringProperty(String.valueOf(kod));
        });

        colProdName.setCellValueFactory(c -> {
            int kod = c.getValue().getProductCode();
            Product p = productsMap.get(kod);
            String name = p != null ? p.getProductName() : "Код: " + kod;
            return new SimpleStringProperty(name);
        });

        colCount.setCellValueFactory(c -> c.getValue().soldProductCountProperty().asObject());
        colPrice.setCellValueFactory(c -> c.getValue().priceWithoutNdsProperty().asObject());
        colNds.setCellValueFactory(c -> c.getValue().ndsSummProperty().asObject());

        colTotalPrice.setCellValueFactory(c -> {
            int count = c.getValue().getSoldProductCount();
            double price = c.getValue().getPriceWithoutNds();
            double nds = c.getValue().getNdsSumm();
            double total = (price * count) + nds;
            return new javafx.beans.property.SimpleObjectProperty<>(total);
        });
    }

    private void recalculateTotal() {
        if (detailData == null || detailData.isEmpty()) {
            totalField.setText("0.00");
            return;
        }

        double total = 0;
        for (SoldItem s : detailData) {
            double price = s.getPriceWithoutNds();
            int count = s.getSoldProductCount();
            double nds = s.getNdsSumm();
            total += (price * count) + nds;
        }

        total = Math.round(total * 100.0) / 100.0;
        totalField.setText(String.format("%.2f", total));
        invoice.setSelling_price(total);
    }

    private void setEditMode(boolean masterMode) {
        if (masterMode) {
            btnOk.setText("Сохранить");
            btnCancel.setText("Отмена");
            datePicker.setDisable(false);
            buyerCombo.setDisable(false);
            payField.setDisable(false);
            totalField.setDisable(true);
            btnNewDetail.setDisable(true);
            btnEditDetail.setDisable(true);
            btnDeleteDetail.setDisable(true);
        } else {
            btnOk.setText("Редактировать накладную");
            btnCancel.setText("Выход");
            datePicker.setDisable(true);
            buyerCombo.setDisable(true);
            payField.setDisable(true);
            totalField.setDisable(true);
            btnNewDetail.setDisable(false);
            btnEditDetail.setDisable(false);
            btnDeleteDetail.setDisable(false);
        }
    }

    @FXML private void handleOk() {
        if (btnOk.getText().equals("Сохранить")) {
            if (!isInputValid()) return;

            invoice.setSell_date(datePicker.getValue());
            if (buyerCombo.getValue() != null) {
                invoice.setId_buyer(buyerCombo.getValue().getId_buyer());
                invoice.setBuyer_name(buyerCombo.getValue().getOrganization_name());
            }
            invoice.setPayment_cost(payField.getText().isEmpty() ? 0 : Double.parseDouble(payField.getText()));

            boolean ok = isNew ? manager.addInvoice(invoice) : manager.updateInvoice(invoice, oldKey);
            if (ok) {
                isOk = true;
                if (parentCtrl != null) {
                    if (isNew) {
                        parentCtrl.getInvoiceTable().getItems().add(invoice);
                        isNew = false;
                        oldKey = invoice.getId_invoice();
                        dialogStage.setTitle("Редактирование накладной");
                    } else {
                        parentCtrl.getInvoiceTable().refresh();
                    }
                }
                setEditMode(false);
            }
        } else {
            setEditMode(true);
        }
    }

    @FXML private void handleCancel() {
        if (btnCancel.getText().equals("Выход")) {
            dialogStage.close();
        } else {
            setEditMode(false);
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (datePicker.getValue() == null)
            errorMessage += "Не выбрана дата!\n";
        if (buyerCombo.getValue() == null)
            errorMessage += "Не выбран покупатель!\n";

        if (errorMessage.isEmpty()) return true;
        else {
            myapp.gui.Dialogs.showDialog("Ошибка", errorMessage, AlertType.ERROR, dialogStage);
            return false;
        }
    }

    public boolean isOkClicked() {
        return isOk;
    }

    @FXML private void handleNewDetail() {
        if (detailData == null) return;
        SoldItem newItem = new SoldItem();
        newItem.setIdInvoice(invoice.getId_invoice());
        if (showDetailDialog(newItem)) {
            detailData.add(newItem);
            recalculateTotal();
        }
    }

    @FXML private void handleEditDetail() {
        if (detailData == null) return;
        SoldItem selected = detailTable.getSelectionModel().getSelectedItem();
        if (selected != null && showDetailDialog(selected)) {
            detailTable.refresh();
            recalculateTotal();
        }
    }

    @FXML private void handleDeleteDetail() {
        if (detailData == null) return;
        int idx = detailTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            if (myapp.gui.Dialogs.showConfirmDialog("Удалить позицию?", dialogStage)) {
                SoldItem s = detailTable.getSelectionModel().getSelectedItem();
                if (manager.deleteSoldItem(s.getIdProduct())) {
                    detailTable.getItems().remove(idx);
                    recalculateTotal();
                }
            }
        }
    }

    private boolean showDetailDialog(SoldItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SoldProductEd.fxml"));
            javafx.scene.layout.AnchorPane page = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(dialogStage);
            stage.setTitle(item.getIdProduct() == 0 ? "Добавление товара" : "Редактирование товара");
            stage.setScene(new Scene(page));
            SoldProductEdController ctrl = loader.getController();
            ctrl.initialize(stage, manager, item, invoice.getId_invoice());
            stage.showAndWait();
            return ctrl.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}