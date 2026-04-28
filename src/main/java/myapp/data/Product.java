package myapp.data;

public class Product {
    private int product_code;
    private String product_name;
    private double price_per_unit_without_NDS;
    private int group_code;

    public Product() {
        this.product_code = 0;
        this.product_name = "";
        this.price_per_unit_without_NDS = 0.0;
        this.group_code = 0;
    }

    public Product(int product_code, String product_name,
                   double price_per_unit_without_NDS, int group_code) {
        this.product_code = product_code;
        this.product_name = product_name;
        this.price_per_unit_without_NDS = price_per_unit_without_NDS;
        this.group_code = group_code;
    }

    public int getProduct_code() {
        return product_code;
    }

    public void setProduct_code(int product_code) {
        this.product_code = product_code;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public double getPrice_per_unit_without_NDS() {
        return price_per_unit_without_NDS;
    }

    public void setPrice_per_unit_without_NDS(double price_per_unit_without_NDS) {
        this.price_per_unit_without_NDS = price_per_unit_without_NDS;
    }

    public int getGroup_code() {
        return group_code;
    }

    public void setGroup_code(int group_code) {
        this.group_code = group_code;
    }
}