package myapp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import myapp.DBManager;
import myapp.data.Buyer;
import myapp.data.SalesInvoice;
import myapp.data.SoldItem;
import java.util.ArrayList;

public class SalesBookEdController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Buyer> buyerCombo;
    @FXML private TextField totalField;
    @FXML private TextField payField;
    @FXML private Button btnOk, btnCancel;

    @FXML private TableView<SoldItem> detailTable;
    @FXML private TableColumn<SoldItem, Integer> colProdCode;
    @FXML private TableColumn<SoldItem, String> colProdName;
    @FXML private TableColumn<SoldItem, Integer> colCount;
    @FXML private TableColumn<SoldItem, Double> colPrice;
    @FXML private TableColumn<SoldItem, Double> colNds;
    @FXML private Button btnNewDetail, btnEditDetail, btnDeleteDetail;

    private Stage dialogStage;
    private DBManager manager;
    private SalesInvoice invoice;
    private SalesBookViewController parentCtrl; // Ссылка на родителя для обновления

    private ObservableList<SoldItem> detailData = FXCollections.observableArrayList();
    private boolean isNew = true;
    private int oldKey;

    public void initialize(Stage dialogStage, DBManager manager, SalesInvoice invoice, SalesBookViewController parentCtrl) {
        this.dialogStage = dialogStage;
        this.manager = manager;
        this.invoice = invoice;
        this.parentCtrl = parentCtrl;

        // Загрузка покупателей в ComboBox
        ArrayList<Buyer> buyers = new ArrayList<>(manager.loadBuyersForCombo());
        buyerCombo.setItems(FXCollections.observableArrayList(buyers));

        buyerCombo.setCellFactory(lv -> new ListCell<Buyer>() {
            @Override protected void updateItem(Buyer item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getOrganization_name());
            }
        });

        buyerCombo.setButtonCell(new ListCell<Buyer>() {
            @Override protected void updateItem(Buyer item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getOrganization_name());
            }
        });

        // Инициализация полей главной таблицы
        if (invoice.getId_invoice() == 0) {
            isNew = true;
            datePicker.setValue(java.time.LocalDate.now());
            totalField.setText("0.00");
            payField.setText("0.00");
        } else {
            isNew = false;
            oldKey = invoice.getId_invoice();
            datePicker.setValue(invoice.getSell_date());
            totalField.setText(String.valueOf(invoice.getSelling_price()));
            payField.setText(String.valueOf(invoice.getPayment_cost()));

            // Выбрать покупателя в комбобоксе
            for(Buyer b : buyers) {
                if(b.getId_buyer() == invoice.getId_buyer()) {
                    buyerCombo.setValue(b);
                    break;
                }
            }
        }

        // Инициализация подчиненной таблицы
        if (!isNew) {
            detailData.setAll(manager.loadSoldItems(invoice.getId_invoice()));
        }

        detailTable.setItems(detailData);

        colProdCode.setCellValueFactory(new PropertyValueFactory<>("product_code"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        colCount.setCellValueFactory(new PropertyValueFactory<>("sold_product_count"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price_without_nds"));
        colNds.setCellValueFactory(new PropertyValueFactory<>("nds_summ"));

        // Режим редактирования главной таблицы (по умолчанию)
        setEditMode(true);
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
            invoice.setSell_date(datePicker.getValue());
            if (buyerCombo.getValue() != null) {
                invoice.setId_buyer(buyerCombo.getValue().getId_buyer());
                invoice.setBuyer_name(buyerCombo.getValue().getOrganization_name());
            }
            invoice.setPayment_cost(payField.getText().isEmpty() ? 0 : Double.parseDouble(payField.getText()));
            invoice.setSelling_price(totalField.getText().isEmpty() ? 0 : Double.parseDouble(totalField.getText()));

            if (isNew) {
                if (manager.addInvoice(invoice)) {
                    isNew = false;
                    oldKey = invoice.getId_invoice();
                    setEditMode(false);
                }
            } else {
                if (manager.updateInvoice(invoice, oldKey)) {
                    setEditMode(false);
                }
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

    // --- ОПЕРАЦИИ С ПОДЧИНЕННОЙ ТАБЛИЦЕЙ ---
    @FXML private void handleNewDetail() {
        SoldItem newItem = new SoldItem();
        newItem.setId_invoice(invoice.getId_invoice());
        if (showDetailDialog(newItem)) {
            if (manager.addSoldItem(newItem)) {
                detailData.add(newItem);
                updateTotalPrice();
            }
        }
    }

    @FXML private void handleEditDetail() {
        SoldItem sel = detailTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            int oldKey = sel.getId_product();
            if (showDetailDialog(sel)) {
                if (manager.updateSoldItem(sel, oldKey)) {
                    detailTable.refresh();
                    updateTotalPrice();
                }
            }
        }
    }

    @FXML private void handleDeleteDetail() {
        int idx = detailTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && myapp.gui.Dialogs.showConfirmDialog("Удалить товар?", dialogStage)) {
            SoldItem item = detailData.get(idx);
            if (manager.deleteSoldItem(item.getId_product())) {
                detailData.remove(idx);
                updateTotalPrice();
            }
        }
    }

    private void updateTotalPrice() {
        double total = 0;
        for(SoldItem item : detailData) {
            total += (item.getPrice_without_nds() * item.getSold_product_count()) + item.getNds_summ();
        }
        invoice.setSelling_price(total);
        totalField.setText(String.format("%.2f", total));
    }

    private boolean showDetailDialog(SoldItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SoldProductEd.fxml"));
            javafx.scene.layout.AnchorPane page = loader.load();
            Stage editStage = new Stage();
            editStage.setTitle(item.getId_product() == 0 ? "Добавление товара" : "Редактирование товара");
            editStage.initModality(Modality.WINDOW_MODAL);
            editStage.initOwner(dialogStage);
            editStage.setScene(new Scene(page));
            SoldProductEdController ctrl = loader.getController();
            ctrl.initialize(editStage, manager, item);
            editStage.showAndWait();
            return ctrl.isOkClicked();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}