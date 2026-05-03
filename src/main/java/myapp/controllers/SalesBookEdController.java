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

    // Переключение режимов: true = редактируем накладную, false = редактируем товары
    private void setEditMode(boolean masterMode) {
        if (masterMode) {
            btnOk.setText("Сохранить");
            btnCancel.setText("Отмена");
            datePicker.setDisable(false);
            buyerCombo.setDisable(false);
            payField.setDisable(false); // Поле оплаты можно менять
            totalField.setDisable(true); // Сумма считается автоматически (или триггером)
            btnNewDetail.setDisable(true);
            btnEditDetail.setDisable(true);
            btnDeleteDetail.setDisable(true);
            detailTable.setDisable(true);
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
            detailTable.setDisable(false);
        }
    }

    @FXML
    private void handleOk() {
        System.out.println("[LOG] handleOk called. Button text: " + btnOk.getText());
        if (btnOk.getText().equals("Сохранить")) {
            System.out.println("[LOG] Saving invoice...");
            // Сохранение главной таблицы
            invoice.setSell_date(datePicker.getValue());
            if (buyerCombo.getValue() != null) {
                invoice.setId_buyer(buyerCombo.getValue().getId_buyer());
                invoice.setBuyer_name(buyerCombo.getValue().getOrganization_name());
            }
            invoice.setPayment_cost(payField.getText().isEmpty() ? 0 : Double.parseDouble(payField.getText()));
            invoice.setSelling_price(totalField.getText().isEmpty() ? 0 : Double.parseDouble(totalField.getText()));

            if (isNew) {
                System.out.println("[LOG] Calling addInvoice (NEW)...");
                if (manager.addInvoice(invoice)) {
                    System.out.println("[LOG] Invoice added. New ID: " + invoice.getId_invoice());
                    isNew = false;
                    oldKey = invoice.getId_invoice();
                    // После создания накладной загружаем пустой список товаров
                    detailData.clear();
                    setEditMode(false); // Переходим в режим редактирования товаров
                } else {
                    System.out.println("[LOG] addInvoice returned FALSE");
                }
            } else {
                System.out.println("[LOG] Calling updateInvoice with oldKey=" + oldKey);
                if (manager.updateInvoice(invoice, oldKey)) {
                    System.out.println("[LOG] Invoice updated");
                    setEditMode(false);
                } else {
                    System.out.println("[LOG] updateInvoice returned FALSE");
                }
            }
        } else {
            // Кнопка "Редактировать накладную" - возвращаемся в режим редактирования шапки
            System.out.println("[LOG] Switching to master edit mode");
            setEditMode(true);
        }
    }

    @FXML private void handleCancel() {
        if (btnCancel.getText().equals("Выход")) {
            dialogStage.close();
        } else {
            setEditMode(false); // Кнопка "Отмена" при редактировании шапки просто переключает режим
        }
    }

    // --- ОПЕРАЦИИ С ПОДЧИНЕННОЙ ТАБЛИЦЕЙ ---
    @FXML private void handleNewDetail() {
        System.out.println("[LOG] handleNewDetail START");
        System.out.println("[LOG] invoice object: " + invoice);
        if (invoice != null) {
            System.out.println("[LOG] invoice.id_invoice = " + invoice.getId_invoice());
        } else {
            System.out.println("[LOG] invoice is NULL!");
        }
        
        if (invoice == null || invoice.getId_invoice() == 0) {
            System.out.println("[LOG] BLOCKED: Invoice not saved yet");
            myapp.gui.Dialogs.showDialog("Ошибка", "Накладная еще не сохранена. Сначала сохраните накладную.",
                    javafx.scene.control.Alert.AlertType.ERROR, dialogStage);
            return;
        }
        
        System.out.println("[LOG] Creating new SoldItem with id_invoice=" + invoice.getId_invoice());
        SoldItem newItem = new SoldItem();
        newItem.setId_invoice(invoice.getId_invoice());
        
        System.out.println("[LOG] Calling showDetailDialog...");
        if (showDetailDialog(newItem)) {
            System.out.println("[LOG] Dialog returned TRUE, reloading data...");
            detailData.setAll(manager.loadSoldItems(invoice.getId_invoice()));
            updateTotalPrice();
            System.out.println("[LOG] Data reloaded. Detail table size: " + detailData.size());
        } else {
            System.out.println("[LOG] Dialog returned FALSE or cancelled");
        }
    }

    @FXML
    private void handleEditDetail() {
        System.out.println("[LOG] handleEditDetail START");
        SoldItem sel = detailTable.getSelectionModel().getSelectedItem();
        System.out.println("[LOG] Selected item: " + sel);
        
        if (sel != null) {
            System.out.println("[LOG] Creating copy for edit. id_product=" + sel.getId_product() + ", id_invoice=" + sel.getId_invoice());
            // Создаем копию для редактирования - ВАЖНО: id_invoice должен быть установлен!
            SoldItem itemToEdit = new SoldItem(
                sel.getId_product(),
                sel.getId_invoice(),
                sel.getProduct_code(),
                sel.getProduct_name(),
                sel.getSold_product_count(),
                sel.getPrice_without_nds(),
                sel.getNds_summ()
            );
            System.out.println("[LOG] Calling showDetailDialog for edit...");
            if (showDetailDialog(itemToEdit)) {
                System.out.println("[LOG] Edit dialog returned TRUE, reloading data...");
                // Перезагружаем данные из БД после редактирования
                detailData.setAll(manager.loadSoldItems(invoice.getId_invoice()));
                updateTotalPrice();
                System.out.println("[LOG] Data reloaded. Detail table size: " + detailData.size());
            } else {
                System.out.println("[LOG] Edit dialog returned FALSE or cancelled");
            }
        } else {
            System.out.println("[LOG] No item selected");
            myapp.gui.Dialogs.showDialog("Внимание", "Выберите товар для редактирования",
                    javafx.scene.control.Alert.AlertType.WARNING, dialogStage);
        }
    }

    @FXML
    private void handleDeleteDetail() {
        System.out.println("[LOG] handleDeleteDetail START");
        SoldItem sel = detailTable.getSelectionModel().getSelectedItem();
        System.out.println("[LOG] Selected item for delete: " + (sel != null ? "id_product=" + sel.getId_product() : "NULL"));
        
        if (sel != null) {
            System.out.println("[LOG] Showing confirm dialog...");
            if (myapp.gui.Dialogs.showConfirmDialog("Удалить товар?", dialogStage)) {
                System.out.println("[LOG] User confirmed. Calling deleteSoldItem for id_product=" + sel.getId_product());
                if (manager.deleteSoldItem(sel.getId_product())) {
                    System.out.println("[LOG] Delete successful, reloading data...");
                    // Перезагружаем данные из БД после удаления
                    detailData.setAll(manager.loadSoldItems(invoice.getId_invoice()));
                    updateTotalPrice();
                    System.out.println("[LOG] Data reloaded. Detail table size: " + detailData.size());
                } else {
                    System.out.println("[LOG] Delete returned FALSE");
                }
            } else {
                System.out.println("[LOG] User cancelled delete");
            }
        } else {
            System.out.println("[LOG] No item selected for delete");
            myapp.gui.Dialogs.showDialog("Внимание", "Выберите товар для удаления",
                    javafx.scene.control.Alert.AlertType.WARNING, dialogStage);
        }
    }

    private void updateTotalPrice() {
        // Пересчитываем сумму или берем из БД (если триггер)
        // В твоей схеме есть триггер trg_update_sellpr, он обновит sales_book.selling_price
        // Но нам нужно обновить поле в UI. Перезагрузим данные накладной.
        // Самый простой способ - взять сумму из loaded invoice
        // Но так как мы не перезагружали invoice объект, посчитаем вручную для UI
        double total = 0;
        for(SoldItem item : detailData) {
            total += (item.getPrice_without_nds() * item.getSold_product_count()) + item.getNds_summ();
        }
        invoice.setSelling_price(total); // Обновляем объект
        totalField.setText(String.format("%.2f", total));
    }

    private boolean showDetailDialog(SoldItem item) {
        System.out.println("[LOG] showDetailDialog START. item.id_product=" + item.getId_product() + ", item.id_invoice=" + item.getId_invoice());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SoldProductEd.fxml"));
            javafx.scene.layout.AnchorPane page = loader.load();
            Stage editStage = new Stage();
            editStage.setTitle(item.getId_product() == 0 ? "Добавление товара" : "Редактирование товара");
            editStage.initModality(Modality.WINDOW_MODAL);
            editStage.initOwner(dialogStage);
            editStage.setScene(new Scene(page));
            SoldProductEdController ctrl = loader.getController();
            System.out.println("[LOG] Initializing SoldProductEdController...");
            ctrl.initialize(editStage, manager, item);
            System.out.println("[LOG] Showing dialog...");
            editStage.showAndWait();
            boolean result = ctrl.isOkClicked();
            System.out.println("[LOG] Dialog closed. isOkClicked=" + result);
            return result;
        } catch (Exception e) {
            System.out.println("[LOG] Exception in showDetailDialog: " + e.getMessage());
            e.printStackTrace();
            myapp.gui.Dialogs.showDialog("Ошибка", "Не удалось открыть диалог товара: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR, dialogStage);
            return false;
        }
    }
}