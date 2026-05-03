package myapp;

import myapp.data.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private Connection con = null;

    public Connection getConnection() {
        return con;
    }

    public void setConnection(Connection con) {
        this.con = con;
    }

    // Метод тестирования соединения
    public String getVersion() {
        String ver = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT VERSION()");
            if (rset.next())
                ver = rset.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(rset != null) rset.close();
                if(stmt != null) stmt.close();
            } catch (SQLException ignored) {}
        }
        return ver;
    }

    // ========== МЕТОДЫ ДЛЯ BUYERS ==========

    // Получение списка покупателей
    public List<Buyer> getBuyers() {
        List<Buyer> buyers = new ArrayList<>();
        String query = "SELECT id_buyer, organization_name FROM taxes.buyers ORDER BY id_buyer";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                buyers.add(new Buyer(
                        rs.getInt("id_buyer"),
                        rs.getString("organization_name")
                ));
            }
        } catch (SQLException e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Не удалось загрузить покупателей: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
        }

        return buyers;
    }

    // Добавление нового покупателя (с использованием sequence)
    public boolean addBuyer(Buyer buyer) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "INSERT INTO taxes.buyers (organization_name) VALUES (?)";

        try {
            pst = con.prepareStatement(stm);
            pst.setString(1, buyer.getOrganization_name());
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException ex) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка добавления данных",
                    ex.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { return false; }
        }
    }

    // Обновление покупателя
    public boolean updateBuyer(Buyer buyer, int key) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "UPDATE taxes.buyers SET organization_name = ? WHERE id_buyer = ?";

        try {
            pst = con.prepareStatement(stm);
            pst.setString(1, buyer.getOrganization_name());
            pst.setInt(2, key);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException ex) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка изменения данных",
                    ex.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { return false; }
        }
    }

    // Удаление покупателя
    public boolean deleteBuyer(int kod) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "DELETE FROM taxes.buyers WHERE id_buyer = ?";

        try {
            pst = con.prepareStatement(stm);
            pst.setInt(1, kod);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException ex) {
            myapp.gui.Dialogs.showDialog("Ошибка удаления данных",
                    ex.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            RollBack();
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { return false; }
        }
    }

    // ========== МЕТОДЫ ДЛЯ PRODUCT_GROUP ==========

    public List<ProductGroup> getProductGroups() {
        List<ProductGroup> groups = new ArrayList<>();
        String query = "SELECT group_code, group_name, NDS_percent FROM taxes.product_group ORDER BY group_code";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                groups.add(new ProductGroup(
                        rs.getInt("group_code"),
                        rs.getString("group_name"),
                        rs.getDouble("NDS_percent")
                ));
            }
        } catch (SQLException e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Не удалось загрузить группы товаров: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
        }

        return groups;
    }

    // ========== МЕТОДЫ ДЛЯ PRODUCT ==========

    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT product_code, product_name, price_per_unit_without_NDS, group_code FROM taxes.product ORDER BY product_code";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("product_code"),
                        rs.getString("product_name"),
                        rs.getDouble("price_per_unit_without_NDS"),
                        rs.getInt("group_code")
                ));
            }
        } catch (SQLException e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Не удалось загрузить товары: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
        }

        return products;
    }

    // Метод отката транзакции
    private void RollBack() {
        try {
            if (con != null) con.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Группы товаров ---
    public boolean addGroup(ProductGroup group) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "INSERT INTO taxes.product_group (group_code, group_name, NDS_percent) VALUES (?, ?, ?)";
        try {
            pst = con.prepareStatement(stm);
            pst.setInt(1, group.getGroup_code());
            pst.setString(2, group.getGroup_name());
            pst.setDouble(3, group.getNDS_percent());
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException ex) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { return false; }
        }
    }

    public boolean updateGroup(ProductGroup group, int key) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "UPDATE taxes.product_group SET group_name=?, NDS_percent=? WHERE group_code=?";
        try {
            pst = con.prepareStatement(stm);
            pst.setString(1, group.getGroup_name());
            pst.setDouble(2, group.getNDS_percent());
            pst.setInt(3, key);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException ex) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { return false; }
        }
    }

    public boolean deleteGroup(int kod) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "DELETE FROM taxes.product_group WHERE group_code=?";
        try {
            pst = con.prepareStatement(stm);
            pst.setInt(1, kod);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException ex) {
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            RollBack();
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { return false; }
        }
    }

    // --- Товары ---
    public boolean addProduct(Product prod) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "INSERT INTO taxes.product (product_code, product_name, price_per_unit_without_NDS, group_code) VALUES (?, ?, ?, ?)";
        try {
            pst = con.prepareStatement(stm);
            pst.setInt(1, prod.getProduct_code());
            pst.setString(2, prod.getProduct_name());
            pst.setDouble(3, prod.getPrice_per_unit_without_NDS());
            pst.setInt(4, prod.getGroup_code());
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException ex) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { return false; }
        }
    }

    public boolean updateProduct(Product prod, int key) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "UPDATE taxes.product SET product_name=?, price_per_unit_without_NDS=?, group_code=? WHERE product_code=?";
        try {
            pst = con.prepareStatement(stm);
            pst.setString(1, prod.getProduct_name());
            pst.setDouble(2, prod.getPrice_per_unit_without_NDS());
            pst.setInt(3, prod.getGroup_code());
            pst.setInt(4, key);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException ex) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { return false; }
        }
    }

    public boolean deleteProduct(int kod) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "DELETE FROM taxes.product WHERE product_code=?";
        try {
            pst = con.prepareStatement(stm);
            pst.setInt(1, kod);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException ex) {
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            RollBack();
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { return false; }
        }
    }
    // ========== ГЛАВНАЯ ТАБЛИЦА: sales_book ==========

    // Загрузка списка накладных
    public java.util.List<SalesInvoice> loadInvoices() {
        java.util.List<SalesInvoice> list = new java.util.ArrayList<>();
        // JOIN с buyers, чтобы сразу получить имя покупателя
        String sql = "SELECT s.id_invoice, s.sell_date, s.id_buyer, b.organization_name, s.selling_price, s.payment_cost " +
                "FROM taxes.sales_book s LEFT JOIN taxes.buyers b ON s.id_buyer = b.id_buyer " +
                "ORDER BY s.id_invoice";
        try (java.sql.Statement stmt = con.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SalesInvoice inv = new SalesInvoice();
                inv.setId_invoice(rs.getInt("id_invoice"));
                inv.setSell_date(rs.getDate("sell_date").toLocalDate());
                inv.setId_buyer(rs.getInt("id_buyer"));
                inv.setBuyer_name(rs.getString("organization_name"));
                inv.setSelling_price(rs.getDouble("selling_price"));
                inv.setPayment_cost(rs.getDouble("payment_cost"));
                list.add(inv);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Добавление накладной (Суррогатный ключ через RETURNING)
    public boolean addInvoice(SalesInvoice inv) {
        String sql = "INSERT INTO taxes.sales_book (sell_date, id_buyer, selling_price, payment_cost) " +
                "VALUES (?, ?, ?, ?) RETURNING id_invoice";
        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, java.sql.Date.valueOf(inv.getSell_date()));
            pst.setInt(2, inv.getId_buyer());
            pst.setDouble(3, inv.getSelling_price());
            pst.setDouble(4, inv.getPayment_cost());

            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                inv.setId_invoice(rs.getInt(1)); // Записываем сгенерированный ключ обратно в объект
            }
            con.commit();
            return true;
        } catch (Exception e) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Error", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
    }

    // Обновление накладной
    public boolean updateInvoice(SalesInvoice inv, int oldKey) {
        String sql = "UPDATE taxes.sales_book SET sell_date=?, id_buyer=?, selling_price=?, payment_cost=? WHERE id_invoice=?";
        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, java.sql.Date.valueOf(inv.getSell_date()));
            pst.setInt(2, inv.getId_buyer());
            pst.setDouble(3, inv.getSelling_price());
            pst.setDouble(4, inv.getPayment_cost());
            pst.setInt(5, oldKey);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (Exception e) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Error", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
    }

    // Удаление накладной
    public boolean deleteInvoice(int id) {
        String sql = "DELETE FROM taxes.sales_book WHERE id_invoice=?";
        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (Exception e) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Erro", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
    }

    // ========== ПОДЧИНЕННАЯ ТАБЛИЦА: sold_product ==========
    
    // Загрузка товаров для накладной
    public java.util.List<SoldItem> loadSoldItems(int invoiceId) {
        java.util.List<SoldItem> list = new java.util.ArrayList<>();
        String sql = "SELECT sp.id_product, sp.id_invoice, sp.product_code, p.product_name, " +
                "sp.sold_product_count, sp.price_without_nds, sp.nds_summ " +
                "FROM taxes.sold_product sp LEFT JOIN taxes.product p ON sp.product_code = p.product_code " +
                "WHERE sp.id_invoice = ?";
        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, invoiceId);
            java.sql.ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                SoldItem item = new SoldItem();
                item.setId_product(rs.getInt("id_product"));
                item.setId_invoice(rs.getInt("id_invoice"));
                item.setProduct_code(rs.getInt("product_code"));
                item.setProduct_name(rs.getString("product_name"));
                item.setSold_product_count(rs.getInt("sold_product_count"));
                item.setPrice_without_nds(rs.getDouble("price_without_nds"));
                item.setNds_summ(rs.getDouble("nds_summ"));
                list.add(item);
            }
        } catch (Exception e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Не удалось загрузить товары накладной: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
        return list;
    }

    // Добавление товара в накладную (Суррогатный ключ через RETURNING)
    public boolean addSoldItem(SoldItem item) {
        if (con == null) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Нет соединения с базой данных",
                    javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }

        String sql = "INSERT INTO taxes.sold_product (id_invoice, product_code, sold_product_count, price_without_nds, nds_summ) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id_product";
        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            if (item.getId_invoice() == 0) {
                throw new Exception("ID накладной не установлен");
            }

            pst.setInt(1, item.getId_invoice());
            pst.setInt(2, item.getProduct_code());
            pst.setInt(3, item.getSold_product_count());
            pst.setDouble(4, item.getPrice_without_nds());
            pst.setDouble(5, item.getNds_summ());

            try (java.sql.ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    item.setId_product(rs.getInt(1));
                }
            }
            con.commit();
            return true;
        } catch (Exception e) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка добавления товара", e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
    }

    // Обновление товара в накладной
    public boolean updateSoldItem(SoldItem item, int oldKey) {
        String sql = "UPDATE taxes.sold_product SET product_code=?, sold_product_count=?, price_without_nds=?, nds_summ=? WHERE id_product=?";
        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, item.getProduct_code());
            pst.setInt(2, item.getSold_product_count());
            pst.setDouble(3, item.getPrice_without_nds());
            pst.setDouble(4, item.getNds_summ());
            pst.setInt(5, oldKey);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (Exception e) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка обновления товара", e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
    }

    // Удаление товара из накладной
    public boolean deleteSoldItem(int id) {
        String sql = "DELETE FROM taxes.sold_product WHERE id_product=?";
        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (Exception e) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка удаления товара", e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
    }

    // Получение списка покупателей для ComboBox
    public java.util.List<Buyer> loadBuyersForCombo() {
        java.util.List<Buyer> list = new java.util.ArrayList<>();
        try (java.sql.Statement stmt = con.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT id_buyer, organization_name FROM taxes.buyers ORDER BY organization_name")) {
            while (rs.next()) {
                list.add(new Buyer(rs.getInt("id_buyer"), rs.getString("organization_name")));
            }
        } catch (Exception e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Не удалось загрузить покупателей: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
        return list;
    }

    public void refreshInvoice(SalesInvoice inv) {
        if (inv == null || inv.getId_invoice() == 0) return;
        String sql = "SELECT selling_price FROM taxes.sales_book WHERE id_invoice = ?";
        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, inv.getId_invoice());
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                inv.setSelling_price(rs.getDouble("selling_price"));
            }
        } catch (Exception e) {
            myapp.gui.Dialogs.showDialog("Ошибка", "Не удалось обновить сумму накладной: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }
}
