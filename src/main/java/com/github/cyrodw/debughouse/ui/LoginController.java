package com.github.cyrodw.debughouse.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField hostnameField;

    public void initialize() {
        Preferences prefs = Preferences.userRoot().node("preferences");
        usernameField.setText(prefs.get("username", ""));
        passwordField.setText(prefs.get("password", ""));
        hostnameField.setText(prefs.get("hostname", ""));
    }

    @FXML
    protected void handleSubmitButtonAction(ActionEvent event) throws Exception {
        Preferences prefs = Preferences.userRoot().node("preferences");
        String username = usernameField.getText();
        String password = passwordField.getText();
        String hostname = hostnameField.getText();
        prefs.put("username", username);
        prefs.put("password", password);
        prefs.put("hostname", hostname);
        final Node source = (Node) event.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
        Client.username = username;
        Client.password = password;
        Client.ip = hostname;
        Client client = new Client();
        client.start(stage);
    }
}
