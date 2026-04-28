package myapp.data;

public class SoldItem {
    private int id_product;       // Суррогатный ключ (SERIAL)
    private int id_invoice;       // Внешний ключ на sales_book
    private int product_code;     // Внешний ключ на product
    private String product_name;  // Имя товара (для отображения)
    private int sold_product_count; // Количество
    private double price_without_nds; // Цена без НДС
    private double nds_summ;      // Сумма НДС

    public SoldItem() {}

    public SoldItem(int id_product, int id_invoice, int product_code, String product_name, int count, double price, double nds) {
        this.id_product = id_product;
        this.id_invoice = id_invoice;
        this.product_code = product_code;
        this.product_name = product_name;
        this.sold_product_count = count;
        this.price_without_nds = price;
        this.nds_summ = nds;
    }

    // GETTERS & SETTERS
    public int getId_product() { return id_product; }
    public void setId_product(int id_product) { this.id_product = id_product; }

    public int getId_invoice() { return id_invoice; }
    public void setId_invoice(int id_invoice) { this.id_invoice = id_invoice; }

    public int getProduct_code() { return product_code; }
    public void setProduct_code(int product_code) { this.product_code = product_code; }

    public String getProduct_name() { return product_name; }
    public void setProduct_name(String product_name) { this.product_name = product_name; }

    public int getSold_product_count() { return sold_product_count; }
    public void setSold_product_count(int sold_product_count) { this.sold_product_count = sold_product_count; }

    public double getPrice_without_nds() { return price_without_nds; }
    public void setPrice_without_nds(double price_without_nds) { this.price_without_nds = price_without_nds; }

    public double getNds_summ() { return nds_summ; }
    public void setNds_summ(double nds_summ) { this.nds_summ = nds_summ; }
}