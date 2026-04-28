package myapp.data;

public class ProductGroup {
    private int group_code;
    private String group_name;
    private double NDS_percent;

    public ProductGroup() {
        this.group_code = 0;
        this.group_name = "";
        this.NDS_percent = 0.0;
    }

    public ProductGroup(int group_code, String group_name, double NDS_percent) {
        this.group_code = group_code;
        this.group_name = group_name;
        this.NDS_percent = NDS_percent;
    }

    public int getGroup_code() {
        return group_code;
    }

    public void setGroup_code(int group_code) {
        this.group_code = group_code;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public double getNDS_percent() {
        return NDS_percent;
    }

    public void setNDS_percent(double NDS_percent) {
        this.NDS_percent = NDS_percent;
    }
}