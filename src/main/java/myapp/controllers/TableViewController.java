package myapp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import myapp.DBManager;
import myapp.data.*;

import java.io.IOException;

@SuppressWarnings("unchecked")
public class TableViewController {

    @FXML private TableView<Object> tableView;
    @FXML private TableColumn<Object, ?> col1;
    @FXML private TableColumn<Object, ?> col2;
    @FXML private TableColumn<Object, ?> col3;
    @FXML private TableColumn<Object, ?> col4;

    private DBManager manager;
    private Stage dialogStage;
    private String tableName;

    public void initialize(Stage dialogStage, DBManager manager, String tableName) {
        this.dialogStage = dialogStage;
        this.manager = manager;
        this.tableName = tableName;

        loadData();
    }

    private void loadData() {
        if (manager == null || manager.getConnection() == null) {
            System.err.println("Соединение с БД не установлено!");
            return;
        }

        try {
            switch (tableName) {
                case "buyers":
                    loadBuyers();
                    break;
                case "product_group":
                    loadProductGroups();
                    break;
                case "product":
                    loadProducts();
                    break;
                default:
                    System.err.println("Неизвестная таблица: " + tableName);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBuyers() {
        ObservableList<Buyer> data = FXCollections.observableArrayList(manager.getBuyers());

        col1.setText("ID");
        col2.setText("Наименование организации");
        col3.setVisible(false);
        col4.setVisible(false);

        col1.setCellValueFactory(new PropertyValueFactory<>("id_buyer"));
        col2.setCellValueFactory(new PropertyValueFactory<>("organization_name"));

        tableView.setItems((ObservableList<Object>) (ObservableList<?>) data);

        System.out.println("Загружено покупателей: " + data.size());
    }

    private void loadProductGroups() {
        ObservableList<ProductGroup> data = FXCollections.observableArrayList(manager.getProductGroups());

        col1.setText("Код группы");
        col2.setText("Наименование");
        col3.setText("НДС %");
        col4.setVisible(false);

        col1.setCellValueFactory(new PropertyValueFactory<>("group_code"));
        col2.setCellValueFactory(new PropertyValueFactory<>("group_name"));
        col3.setCellValueFactory(new PropertyValueFactory<>("NDS_percent"));

        tableView.setItems((ObservableList<Object>) (ObservableList<?>) data);

        System.out.println("Загружено групп товаров: " + data.size());
    }

    private void loadProducts() {
        ObservableList<Product> data = FXCollections.observableArrayList(manager.getProducts());

        col1.setText("Код товара");
        col2.setText("Наименование");
        col3.setText("Цена без НДС");
        col4.setText("Группа");

        col1.setCellValueFactory(new PropertyValueFactory<>("product_code"));
        col2.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        col3.setCellValueFactory(new PropertyValueFactory<>("price_per_unit_without_NDS"));
        col4.setCellValueFactory(new PropertyValueFactory<>("group_code"));

        tableView.setItems((ObservableList<Object>) (ObservableList<?>) data);

        System.out.println("Загружено товаров: " + data.size());
    }

    // ========== МЕТОДЫ РЕДАКТИРОВАНИЯ (ТЕПЕРЬ ЗДЕСЬ) ==========

    // Добавление нового покупателя
    // --- Кнопка ДОБАВИТЬ ---
    @FXML private void handleNewBuyer() {
        try {
            if ("buyers".equals(tableName)) {
                Buyer newBuyer = new Buyer(0, "");
                if (showBuyerEditDialog(newBuyer)) tableView.getItems().add(newBuyer);
            } else if ("product_group".equals(tableName)) {
                ProductGroup newGroup = new ProductGroup(0, "", 0.0);
                if (showGroupEditDialog(newGroup)) tableView.getItems().add(newGroup);
            } else if ("product".equals(tableName)) {
                Product newProd = new Product(0, "", 0.0, 0);
                if (showProductEditDialog(newProd)) tableView.getItems().add(newProd);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- Кнопка ИЗМЕНИТЬ ---
    @FXML private void handleEditBuyer() {
        try {
            Object selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if ("buyers".equals(tableName)) {
                    if (showBuyerEditDialog((Buyer) selected)) tableView.refresh();
                } else if ("product_group".equals(tableName)) {
                    if (showGroupEditDialog((ProductGroup) selected)) tableView.refresh();
                } else if ("product".equals(tableName)) {
                    if (showProductEditDialog((Product) selected)) tableView.refresh();
                }
            } else {
                myapp.gui.Dialogs.showDialog("Предупреждение", "Выберите строку!", javafx.scene.control.Alert.AlertType.WARNING, dialogStage);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- Кнопка УДАЛИТЬ ---
    @FXML private void handleDeleteBuyer() {
        int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            if (!myapp.gui.Dialogs.showConfirmDialog("Удалить запись?", dialogStage)) return;

            Object selected = tableView.getSelectionModel().getSelectedItem();
            boolean success = false;

            if ("buyers".equals(tableName)) {
                success = manager.deleteBuyer(((Buyer) selected).getId_buyer());
            } else if ("product_group".equals(tableName)) {
                success = manager.deleteGroup(((ProductGroup) selected).getGroup_code());
            } else if ("product".equals(tableName)) {
                success = manager.deleteProduct(((Product) selected).getProduct_code());
            }

            if (success) tableView.getItems().remove(selectedIndex);
        } else {
            myapp.gui.Dialogs.showDialog("Предупреждение", "Выберите строку!", javafx.scene.control.Alert.AlertType.WARNING, dialogStage);
        }
    }

    // --- Методы открытия окон редактирования ---
    private boolean showBuyerEditDialog(Buyer buyer) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/BuyerEd.fxml"));
            javafx.scene.layout.AnchorPane page = loader.load();
            Stage editStage = new Stage(); editStage.setTitle("Покупатель"); editStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            editStage.setScene(new javafx.scene.Scene(page));
            BuyerEdController controller = loader.getController();
            controller.initialize(editStage, manager, buyer);
            editStage.showAndWait(); return controller.isOkClicked();
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private boolean showGroupEditDialog(ProductGroup group) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/GroupEd.fxml"));
            javafx.scene.layout.AnchorPane page = loader.load();
            Stage editStage = new Stage(); editStage.setTitle("Группа товаров"); editStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            editStage.setScene(new javafx.scene.Scene(page));
            GroupEdController controller = loader.getController();
            controller.initialize(editStage, manager, group);
            editStage.showAndWait(); return controller.isOkClicked();
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private boolean showProductEditDialog(Product prod) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/ProductEd.fxml"));
            javafx.scene.layout.AnchorPane page = loader.load();
            Stage editStage = new Stage(); editStage.setTitle("Товар"); editStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            editStage.setScene(new javafx.scene.Scene(page));
            ProductEdController controller = loader.getController();
            controller.initialize(editStage, manager, prod);
            editStage.showAndWait(); return controller.isOkClicked();
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}