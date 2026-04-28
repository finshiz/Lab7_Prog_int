package myapp.data;

import java.time.LocalDate;

public class SalesInvoice {
    private int id_invoice;       // Суррогатный ключ (SERIAL)
    private LocalDate sell_date;  // Дата
    private int id_buyer;         // Внешний ключ на покупателей
    private String buyer_name;    // Имя покупателя (для отображения в таблице, не сохраняется в БД напрямую)
    private double selling_price; // Общая сумма продажи
    private double payment_cost;  // Сумма оплаты

    public SalesInvoice() {}

    public SalesInvoice(int id_invoice, LocalDate sell_date, int id_buyer, String buyer_name, double selling_price, double payment_cost) {
        this.id_invoice = id_invoice;
        this.sell_date = sell_date;
        this.id_buyer = id_buyer;
        this.buyer_name = buyer_name;
        this.selling_price = selling_price;
        this.payment_cost = payment_cost;
    }

    // GETTERS & SETTERS
    public int getId_invoice() { return id_invoice; }
    public void setId_invoice(int id_invoice) { this.id_invoice = id_invoice; }

    public LocalDate getSell_date() { return sell_date; }
    public void setSell_date(LocalDate sell_date) { this.sell_date = sell_date; }

    public int getId_buyer() { return id_buyer; }
    public void setId_buyer(int id_buyer) { this.id_buyer = id_buyer; }

    public String getBuyer_name() { return buyer_name; }
    public void setBuyer_name(String buyer_name) { this.buyer_name = buyer_name; }

    public double getSelling_price() { return selling_price; }
    public void setSelling_price(double selling_price) { this.selling_price = selling_price; }

    public double getPayment_cost() { return payment_cost; }
    public void setPayment_cost(double payment_cost) { this.payment_cost = payment_cost; }
}