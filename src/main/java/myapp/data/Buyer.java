package myapp.data;

public class Buyer {
    private int id_buyer;
    private String organization_name;

    public Buyer() {
        this.id_buyer = 0;
        this.organization_name = "";
    }

    public Buyer(int id_buyer, String organization_name) {
        this.id_buyer = id_buyer;
        this.organization_name = organization_name;
    }

    public int getId_buyer() {
        return id_buyer;
    }

    public void setId_buyer(int id_buyer) {
        this.id_buyer = id_buyer;
    }

    public String getOrganization_name() {
        return organization_name;
    }

    public void setOrganization_name(String organization_name) {
        this.organization_name = organization_name;
    }
}