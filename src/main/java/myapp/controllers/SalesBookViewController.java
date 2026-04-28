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
import myapp.data.SalesInvoice;
import java.time.format.DateTimeFormatter;

public class SalesBookViewController {

    @FXML private TableView<SalesInvoice> invoiceTable;
    @FXML private TableColumn<SalesInvoice, Integer> colId;
    @FXML private TableColumn<SalesInvoice, java.time.LocalDate> colDate;
    @FXML private TableColumn<SalesInvoice, String> colBuyer;
    @FXML private TableColumn<SalesInvoice, Double> colTotal;
    @FXML private TableColumn<SalesInvoice, Double> colPay;

    private DBManager manager;
    private Stage dialogStage;
    private ObservableList<SalesInvoice> data = FXCollections.observableArrayList();

    public void initialize(Stage dialogStage, DBManager manager) {
        this.dialogStage = dialogStage;
        this.manager = manager;
        refreshTable();

        colId.setCellValueFactory(new PropertyValueFactory<>("id_invoice"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("sell_date"));

        colDate.setCellFactory(c -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            @Override protected void updateItem(java.time.LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : fmt.format(item));
            }
        });

        colBuyer.setCellValueFactory(new PropertyValueFactory<>("buyer_name"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("selling_price"));
        colPay.setCellValueFactory(new PropertyValueFactory<>("payment_cost"));
    }

    private void refreshTable() {
        data.setAll(manager.loadInvoices());
        invoiceTable.setItems(data);
    }

    @FXML private void handleNewInvoice() {
        showEditDialog(new SalesInvoice());
    }

    @FXML private void handleEditInvoice() {
        SalesInvoice sel = invoiceTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            showEditDialog(sel);
        } else {
            myapp.gui.Dialogs.showDialog("Внимание", "Выберите строку для редактирования",
                    Alert.AlertType.WARNING, dialogStage);
        }
    }

    @FXML private void handleDeleteInvoice() {
        int idx = invoiceTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            if (myapp.gui.Dialogs.showConfirmDialog("Удалить накладную?", dialogStage)) {
                if (manager.deleteInvoice(data.get(idx).getId_invoice())) {
                    data.remove(idx);
                }
            }
        } else {
            myapp.gui.Dialogs.showDialog("Внимание", "Выберите строку для удаления",
                    Alert.AlertType.WARNING, dialogStage);
        }
    }

    private void showEditDialog(SalesInvoice inv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SalesBookEd.fxml"));
            javafx.scene.Parent page = loader.load();

            Stage editStage = new Stage();
            editStage.setTitle(inv.getId_invoice() == 0 ? "Добавление накладной" : "Редактирование накладной");
            editStage.initModality(Modality.WINDOW_MODAL);
            editStage.initOwner(dialogStage);
            editStage.setScene(new Scene(page));

            SalesBookEdController ctrl = loader.getController();
            ctrl.initialize(editStage, manager, inv, this);

            editStage.showAndWait();
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            myapp.gui.Dialogs.showDialog("Ошибка", "Не удалось открыть окно: " + e.getMessage(),
                    Alert.AlertType.ERROR, dialogStage);
        }
    }
}