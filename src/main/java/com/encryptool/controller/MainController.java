package com.encryptool.controller;

import com.encryptool.model.AESUtil;
import com.encryptool.model.RSAUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainController {
    @FXML private ComboBox<String> algorithmCombo;
    @FXML private RadioButton encryptRadio;
    @FXML private RadioButton decryptRadio;
    @FXML private TextField keyFileField;
    @FXML private TextField inputFileField;
    @FXML private TextField outputFileField;
    @FXML private TextArea logArea;
    @FXML private Button generateKeyBtn;

    @FXML
    public void initialize() {
        algorithmCombo.getItems().addAll("AES-256", "RSA-2048");
        algorithmCombo.setValue("AES-256");
        algorithmCombo.setOnAction(e -> generateKeyBtn.setVisible("RSA-2048".equals(algorithmCombo.getValue())));
        generateKeyBtn.setVisible(false);
        log("Ready. Select algorithm and files.");
    }

    @FXML private void browseInput() {
        File f = new FileChooser().showOpenDialog(inputFileField.getScene().getWindow());
        if (f != null) inputFileField.setText(f.getAbsolutePath());
    }

    @FXML private void browseOutput() {
        File f = new FileChooser().showSaveDialog(outputFileField.getScene().getWindow());
        if (f != null) outputFileField.setText(f.getAbsolutePath());
    }

    @FXML private void browseKey() {
        File f = new FileChooser().showOpenDialog(keyFileField.getScene().getWindow());
        if (f != null) keyFileField.setText(f.getAbsolutePath());
    }

    @FXML private void generateKey() throws Exception {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("key");
        File f = fc.showSaveDialog(generateKeyBtn.getScene().getWindow());
        if (f == null) return;
        if ("RSA-2048".equals(algorithmCombo.getValue())) {
            File dir = f.getParentFile();
            String name = f.getName();
            RSAUtil.generateAndSaveKeys(dir.getAbsolutePath(), name);
            keyFileField.setText(new File(dir, name + "_private.key").getAbsolutePath());
            log("RSA key pair generated.");
        } else {
            AESUtil.generateAndSaveKey(f.getAbsolutePath());
            keyFileField.setText(f.getAbsolutePath());
            log("AES key generated.");
        }
    }

    @FXML private void process() {
        String in = inputFileField.getText().trim();
        String out = outputFileField.getText().trim();
        String key = keyFileField.getText().trim();
        if (in.isEmpty() || out.isEmpty()) { log("Select input and output files."); return; }
        boolean encrypt = encryptRadio.isSelected();

        new Thread(() -> {
            try {
                if ("AES-256".equals(algorithmCombo.getValue())) {
                    byte[] k = Files.readAllBytes(Path.of(key));
                    byte[] data = Files.readAllBytes(Path.of(in));
                    byte[] result = encrypt ? AESUtil.encrypt(data, k) : AESUtil.decrypt(data, k);
                    Files.write(Path.of(out), result);
                } else {
                    byte[] data = Files.readAllBytes(Path.of(in));
                    byte[] result;
                    if (encrypt) result = RSAUtil.encrypt(data, RSAUtil.loadPublicKey(key.replace("_private", "_public")));
                    else result = RSAUtil.decrypt(data, RSAUtil.loadPrivateKey(key));
                    Files.write(Path.of(out), result);
                }
                log((encrypt ? "Encrypt" : "Decrypt") + " complete: " + out);
            } catch (Exception e) { log("Error: " + e.getMessage()); }
        }).start();
    }

    private void log(String msg) {
        javafx.application.Platform.runLater(() -> logArea.appendText("[" + java.time.LocalTime.now().toString().substring(0,8) + "] " + msg + "\n"));
    }
}
