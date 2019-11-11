package com.gopherit.stages;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

class ParentStage extends Stage {

    void showInfoMessage(String title, String header, String message) {
        this.showMessage(title, header, message, Alert.AlertType.INFORMATION);
    }

    void showWarningMessage(String title, String header, String message) {
        this.showMessage(title, header, message, Alert.AlertType.WARNING);
    }

    void showErrorMessage(String title, String header, String message) {
        this.showMessage(title, header, message, Alert.AlertType.ERROR);
    }

    private void showMessage(String title, String header, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setAlertType(alertType);
        alert.setHeaderText(header);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
