package myapp.data;

import javafx.beans.property.*;

public class SoldItem {
    private final IntegerProperty idProduct;
    private final IntegerProperty idInvoice;
    private final IntegerProperty productCode;
    private final StringProperty productName;
    private final IntegerProperty soldProductCount;
    private final DoubleProperty priceWithoutNds;
    private final DoubleProperty ndsSumm;

    public SoldItem() {
        this(0, 0, 0, null, 0, 0.0, 0.0);
    }

    public SoldItem(int idProduct, int idInvoice, int productCode, String productName,
                    int count, double price, double nds) {
        this.idProduct = new SimpleIntegerProperty(idProduct);
        this.idInvoice = new SimpleIntegerProperty(idInvoice);
        this.productCode = new SimpleIntegerProperty(productCode);
        this.productName = new SimpleStringProperty(productName);
        this.soldProductCount = new SimpleIntegerProperty(count);
        this.priceWithoutNds = new SimpleDoubleProperty(price);
        this.ndsSumm = new SimpleDoubleProperty(nds);
    }

    // Геттеры и сеттеры
    public int getIdProduct() { return idProduct.get(); }
    public IntegerProperty idProductProperty() { return idProduct; }
    public void setIdProduct(int idProduct) { this.idProduct.set(idProduct); }

    public int getIdInvoice() { return idInvoice.get(); }
    public IntegerProperty idInvoiceProperty() { return idInvoice; }
    public void setIdInvoice(int idInvoice) { this.idInvoice.set(idInvoice); }

    public int getProductCode() { return productCode.get(); }
    public IntegerProperty productCodeProperty() { return productCode; }
    public void setProductCode(int productCode) { this.productCode.set(productCode); }

    public String getProductName() { return productName.get(); }
    public StringProperty productNameProperty() { return productName; }
    public void setProductName(String productName) { this.productName.set(productName); }

    public int getSoldProductCount() { return soldProductCount.get(); }
    public IntegerProperty soldProductCountProperty() { return soldProductCount; }
    public void setSoldProductCount(int count) { this.soldProductCount.set(count); }

    public double getPriceWithoutNds() { return priceWithoutNds.get(); }
    public DoubleProperty priceWithoutNdsProperty() { return priceWithoutNds; }
    public void setPriceWithoutNds(double price) { this.priceWithoutNds.set(price); }

    public double getNdsSumm() { return ndsSumm.get(); }
    public DoubleProperty ndsSummProperty() { return ndsSumm; }
    public void setNdsSumm(double nds) { this.ndsSumm.set(nds); }
}