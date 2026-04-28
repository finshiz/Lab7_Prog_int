package myapp.controllers;

import myapp.DBManager;
import myapp.gui.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class LoginDialogController implements Initializable {
    @FXML private TextField edLogin;
    @FXML private PasswordField edPwd;

    private Stage dialogStage;
    private boolean okClicked = false;
    private DBManager manager;
    private String url_db;

    public void initialize(URL url, ResourceBundle rb) {}

    public void initialize(Stage dialogStage, DBManager manager) {
        this.dialogStage = dialogStage;
        this.manager = manager;

        Properties properties = new Properties();
        try {
            File file = new File("conf.prop");
            properties.load(new FileReader(file));
            url_db = properties.getProperty("URL_DB");

            edLogin.setText(properties.getProperty("User"));
            edPwd.setText(properties.getProperty("Pwd"));
        } catch (IOException e) {
            System.err.println("ОШИБКА: Файл conf.prop отсутствует!");
        }
    }

    public boolean isOkClicked() { return okClicked; }

    @FXML
    private void handleOk() {
        okClicked = false;
        if (authenticate()) okClicked = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        okClicked = false;
        dialogStage.close();
    }

    protected boolean authenticate() {
        String login = null;
        char[] password = null;
        try {
            login = edLogin.getText().trim();
            password = edPwd.getText().toCharArray();
            if (login.isEmpty() || password.length == 0)
                throw new Exception("Логин и/или пароль не указаны.");
        } catch (Exception e) {
            Dialogs.showDialog("Ошибка ввода данных", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            edPwd.clear();
            return false;
        }

        try {
            Connection con = connect(login, new String(password));
            if (con == null) throw new Exception("Логин, пароль или имя БД указаны неверно");
            manager.setConnection(con);
            System.out.println("DB Version: " + manager.getVersion());
        } catch (Exception e) {
            Dialogs.showDialog("Ошибка соединения с БД", e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            edPwd.clear();
            return false;
        }
        return true;
    }

    private Connection connect(String log, String pass) {
        Locale.setDefault(Locale.ENGLISH);
        try {
            Connection con = DriverManager.getConnection(url_db, log, pass);
            con.setAutoCommit(false);
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}