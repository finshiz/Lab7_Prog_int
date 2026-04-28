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

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                buyers.add(new Buyer(
                        rs.getInt("id_buyer"),
                        rs.getString("organization_name")
                ));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке покупателей: " + e.getMessage());
            e.printStackTrace();
        }

        return buyers;
    }

    // Добавление нового покупателя
    public boolean addBuyer(Buyer buyer) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "INSERT INTO taxes.buyers (id_buyer, organization_name) VALUES (?, ?)";

        try {
            System.out.println(">>> [DB] Добавление покупателя: id=" + buyer.getId_buyer() + ", name=" + buyer.getOrganization_name());
            pst = con.prepareStatement(stm);
            pst.setInt(1, buyer.getId_buyer());
            pst.setString(2, buyer.getOrganization_name());
            int rows = pst.executeUpdate();
            System.out.println(">>> [DB] INSERT выполнен, строк затронуто: " + rows);
            con.commit();
            System.out.println(">>> [DB] COMMIT выполнен для покупателя");
            return true;
        } catch (SQLException ex) {
            RollBack();
            System.err.println(">>> [DB] ОШИБКА добавления покупателя: " + ex.getMessage());
            ex.printStackTrace();
            myapp.gui.Dialogs.showDialog("Oshibka dobavleniya dannih",
                    ex.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    // Обновление покупателя
    public boolean updateBuyer(Buyer buyer, int key) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "UPDATE taxes.buyers SET organization_name = ? WHERE id_buyer = ?";

        try {
            System.out.println(">>> [DB] Обновление покупателя: oldKey=" + key + ", newId=" + buyer.getId_buyer());
            pst = con.prepareStatement(stm);
            pst.setString(1, buyer.getOrganization_name());
            pst.setInt(2, key);
            int rows = pst.executeUpdate();
            System.out.println(">>> [DB] UPDATE выполнен, строк затронуто: " + rows);
            con.commit();
            System.out.println(">>> [DB] COMMIT выполнен для обновления покупателя");
            return true;
        } catch (SQLException ex) {
            RollBack();
            System.err.println(">>> [DB] ОШИБКА обновления покупателя: " + ex.getMessage());
            ex.printStackTrace();
            myapp.gui.Dialogs.showDialog("Oshibka izmeneniya dannih",
                    ex.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    // Удаление покупателя
    public boolean deleteBuyer(int kod) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "DELETE FROM taxes.buyers WHERE id_buyer = ?";

        try {
            System.out.println(">>> [DB] Удаление покупателя: id=" + kod);
            pst = con.prepareStatement(stm);
            pst.setInt(1, kod);
            int rows = pst.executeUpdate();
            System.out.println(">>> [DB] DELETE выполнен, строк затронуто: " + rows);
            con.commit();
            System.out.println(">>> [DB] COMMIT выполнен для удаления покупателя");
            return true;
        } catch (SQLException ex) {
            myapp.gui.Dialogs.showDialog("Oshibka udaleniya dannih",
                    ex.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            System.err.println(">>> [DB] ОШИБКА удаления покупателя: " + ex.getMessage());
            ex.printStackTrace();
            RollBack();
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    // ========== МЕТОДЫ ДЛЯ PRODUCT_GROUP ==========

    public List<ProductGroup> getProductGroups() {
        List<ProductGroup> groups = new ArrayList<>();
        String query = "SELECT group_code, group_name, NDS_percent FROM taxes.product_group ORDER BY group_code";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                groups.add(new ProductGroup(
                        rs.getInt("group_code"),
                        rs.getString("group_name"),
                        rs.getDouble("NDS_percent")
                ));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке групп товаров: " + e.getMessage());
            e.printStackTrace();
        }

        return groups;
    }

    // ========== МЕТОДЫ ДЛЯ PRODUCT ==========

    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT product_code, product_name, price_per_unit_without_NDS, group_code FROM taxes.product ORDER BY product_code";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("product_code"),
                        rs.getString("product_name"),
                        rs.getDouble("price_per_unit_without_NDS"),
                        rs.getInt("group_code")
                ));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке товаров: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }

    // Метод отката транзакции
    private void RollBack() {
        try {
            if (con != null)
                con.rollback();
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
            System.out.println(">>> [DB] Добавление группы: code=" + group.getGroup_code() + ", name=" + group.getGroup_name());
            pst = con.prepareStatement(stm);
            pst.setInt(1, group.getGroup_code());
            pst.setString(2, group.getGroup_name());
            pst.setDouble(3, group.getNDS_percent());
            int rows = pst.executeUpdate();
            System.out.println(">>> [DB] INSERT выполнен, строк затронуто: " + rows);
            con.commit();
            System.out.println(">>> [DB] COMMIT выполнен для группы");
            return true;
        } catch (SQLException ex) {
            RollBack();
            System.err.println(">>> [DB] ОШИБКА добавления группы: " + ex.getMessage());
            ex.printStackTrace();
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if(pst != null) pst.close(); } catch(SQLException e) { /* ignore */ }
        }
    }

    public boolean updateGroup(ProductGroup group, int key) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "UPDATE taxes.product_group SET group_code=?, group_name=?, NDS_percent=? WHERE group_code=?";
        try {
            System.out.println(">>> [DB] Обновление группы: oldKey=" + key + ", newCode=" + group.getGroup_code());
            pst = con.prepareStatement(stm);
            pst.setInt(1, group.getGroup_code());
            pst.setString(2, group.getGroup_name());
            pst.setDouble(3, group.getNDS_percent());
            pst.setInt(4, key);
            int rows = pst.executeUpdate();
            System.out.println(">>> [DB] UPDATE выполнен, строк затронуто: " + rows);
            con.commit();
            System.out.println(">>> [DB] COMMIT выполнен для обновления группы");
            return true;
        } catch (SQLException ex) {
            RollBack();
            System.err.println(">>> [DB] ОШИБКА обновления группы: " + ex.getMessage());
            ex.printStackTrace();
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if(pst != null) pst.close(); } catch(SQLException e) { /* ignore */ }
        }
    }

    public boolean deleteGroup(int kod) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "DELETE FROM taxes.product_group WHERE group_code=?";
        try {
            System.out.println(">>> [DB] Удаление группы: code=" + kod);
            pst = con.prepareStatement(stm);
            pst.setInt(1, kod);
            int rows = pst.executeUpdate();
            System.out.println(">>> [DB] DELETE выполнен, строк затронуто: " + rows);
            con.commit();
            System.out.println(">>> [DB] COMMIT выполнен для удаления группы");
            return true;
        } catch (SQLException ex) {
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            System.err.println(">>> [DB] ОШИБКА удаления группы: " + ex.getMessage());
            ex.printStackTrace();
            RollBack();
            return false;
        } finally {
            try { if(pst != null) pst.close(); } catch(SQLException e) { /* ignore */ }
        }
    }

    // --- Товары ---
    public boolean addProduct(Product prod) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "INSERT INTO taxes.product (product_code, product_name, price_per_unit_without_NDS, group_code) VALUES (?, ?, ?, ?)";
        try {
            System.out.println(">>> [DB] Добавление товара: code=" + prod.getProduct_code() + ", name=" + prod.getProduct_name());
            pst = con.prepareStatement(stm);
            pst.setInt(1, prod.getProduct_code());
            pst.setString(2, prod.getProduct_name());
            pst.setDouble(3, prod.getPrice_per_unit_without_NDS());
            pst.setInt(4, prod.getGroup_code());
            int rows = pst.executeUpdate();
            System.out.println(">>> [DB] INSERT выполнено, строк затронуто: " + rows);
            con.commit();
            System.out.println(">>> [DB] COMMIT выполнен для товара");
            return true;
        } catch (SQLException ex) {
            RollBack();
            System.err.println(">>> [DB] ОШИБКА добавления товара: " + ex.getMessage());
            ex.printStackTrace();
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if(pst != null) pst.close(); } catch(SQLException e) { /* ignore */ }
        }
    }

    public boolean updateProduct(Product prod, int key) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "UPDATE taxes.product SET product_code=?, product_name=?, price_per_unit_without_NDS=?, group_code=? WHERE product_code=?";
        try {
            System.out.println(">>> [DB] Обновление товара: oldKey=" + key + ", newCode=" + prod.getProduct_code());
            pst = con.prepareStatement(stm);
            pst.setInt(1, prod.getProduct_code());
            pst.setString(2, prod.getProduct_name());
            pst.setDouble(3, prod.getPrice_per_unit_without_NDS());
            pst.setInt(4, prod.getGroup_code());
            pst.setInt(5, key);
            int rows = pst.executeUpdate();
            System.out.println(">>> [DB] UPDATE выполнено, строк затронуто: " + rows);
            con.commit();
            System.out.println(">>> [DB] COMMIT выполнен для обновления товара");
            return true;
        } catch (SQLException ex) {
            RollBack();
            System.err.println(">>> [DB] ОШИБКА обновления товара: " + ex.getMessage());
            ex.printStackTrace();
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if(pst != null) pst.close(); } catch(SQLException e) { /* ignore */ }
        }
    }

    public boolean deleteProduct(int kod) {
        PreparedStatement pst = null;
        Connection con = this.getConnection();
        String stm = "DELETE FROM taxes.product WHERE product_code=?";
        try {
            System.out.println(">>> [DB] Удаление товара: code=" + kod);
            pst = con.prepareStatement(stm);
            pst.setInt(1, kod);
            int rows = pst.executeUpdate();
            System.out.println(">>> [DB] DELETE выполнено, строк затронуто: " + rows);
            con.commit();
            System.out.println(">>> [DB] COMMIT выполнен для удаления товара");
            return true;
        } catch (SQLException ex) {
            myapp.gui.Dialogs.showDialog("Ошибка", ex.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            System.err.println(">>> [DB] ОШИБКА удаления товара: " + ex.getMessage());
            ex.printStackTrace();
            RollBack();
            return false;
        } finally {
            try { if(pst != null) pst.close(); } catch(SQLException e) { /* ignore */ }
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
        java.sql.PreparedStatement pst = null;
        java.sql.ResultSet rs = null;
        try {
            pst = con.prepareStatement(sql);
            pst.setDate(1, java.sql.Date.valueOf(inv.getSell_date()));
            pst.setInt(2, inv.getId_buyer());
            pst.setDouble(3, inv.getSelling_price());
            pst.setDouble(4, inv.getPayment_cost());

            rs = pst.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                inv.setId_invoice(generatedId);
                System.out.println(">>> [DB] Sgenerirovan id_invoice: " + generatedId);
            }
            
            // Закрываем ResultSet перед коммитом
            rs.close();
            rs = null;
            
            con.commit();
            System.out.println(">>> [DB] COMMIT vipolnen dlya invoice");
            return true;
        } catch (Exception e) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Error", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (pst != null) pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    // Обновление накладной
    public boolean updateInvoice(SalesInvoice inv, int oldKey) {
        String sql = "UPDATE taxes.sales_book SET sell_date=?, id_buyer=?, selling_price=?, payment_cost=? WHERE id_invoice=?";
        java.sql.PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(sql);
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
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    // Удаление накладной
    public boolean deleteInvoice(int id) {
        String sql = "DELETE FROM taxes.sales_book WHERE id_invoice=?";
        java.sql.PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (Exception e) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Erro", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { /* ignore */ }
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
        System.out.println(">>> DBManager: Vipolnyayetsa SELECT dlya invoiceId=" + invoiceId);
        java.sql.ResultSet rs = pst.executeQuery();
        int count = 0;
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
            count++;
        }
        System.out.println(">>> DBManager: SELECT vernul strok: " + count);
    } catch (Exception e) {
        System.err.println(">>> DBManager ERROR loadSoldItems: " + e.getMessage());
        e.printStackTrace();
    }
    return list;
}
    // Добавление товара в накладную (Суррогатный ключ через RETURNING)
    public boolean addSoldItem(SoldItem item) {
        if (con == null) {
            System.err.println(">>> [DB] Oshibka: Soedineniya s BD net!");
            return false;
        }

        String sql = "INSERT INTO taxes.sold_product (id_invoice, product_code, sold_product_count, price_without_nds, nds_summ) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id_product";
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            System.out.println(">>> [DB] INSERT: invoice=" + item.getId_invoice() +
                    ", prod=" + item.getProduct_code() +
                    ", count=" + item.getSold_product_count());

            if (item.getId_invoice() == 0) {
                throw new Exception("id_invoice raven 0!");
            }

            pst = con.prepareStatement(sql);
            pst.setInt(1, item.getId_invoice());
            pst.setInt(2, item.getProduct_code());
            pst.setInt(3, item.getSold_product_count());
            pst.setDouble(4, item.getPrice_without_nds());
            pst.setDouble(5, item.getNds_summ());

            rs = pst.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                item.setId_product(generatedId);
                System.out.println(">>> [DB] Sgenerirovan id_product: " + generatedId);
            }
            
            // Закрываем ResultSet перед коммитом
            rs.close();
            rs = null;
            
            con.commit();
            System.out.println(">>> [DB] COMMIT vipolnen dlya sold item");

            return true;
        } catch (Exception e) {
            RollBack();
            System.err.println(">>> [DB] OSHIBKA: " + e.getMessage());
            e.printStackTrace();
            myapp.gui.Dialogs.showDialog("Oshibka", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pst != null) pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Обновление товара в накладной
    public boolean updateSoldItem(SoldItem item, int oldKey) {
        String sql = "UPDATE taxes.sold_product SET product_code=?, sold_product_count=?, price_without_nds=?, nds_summ=? WHERE id_product=?";
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(sql);
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
            myapp.gui.Dialogs.showDialog("Ошибка", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Удаление товара из накладной
    public boolean deleteSoldItem(int id) {
        String sql = "DELETE FROM taxes.sold_product WHERE id_product=?";
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            con.commit();
            return true;
        } catch (Exception e) {
            RollBack();
            myapp.gui.Dialogs.showDialog("Ошибка", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { e.printStackTrace(); }
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
        } catch (Exception e) { e.printStackTrace(); }
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
            System.err.println(">>> DBManager ERROR refreshInvoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

}