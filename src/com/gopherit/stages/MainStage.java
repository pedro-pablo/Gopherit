package com.gopherit.stages;

import com.gopherit.Gopher;
import com.gopherit.GopherEntity;
import com.gopherit.GopherEntityType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Optional;

public class MainStage extends ParentStage {

    private final LinkedList<GopherEntity> history;
    private final Gopher gopher;

    private final BookmarkStage bookmarkStage;
    private final TextField txtHost;
    private final TextField txtPort;
    private final TextField txtSelector;
    private final Label lbStatus;
    private final ListView<Object> entityListView;
    private final ObservableList<Object> entitiesOnDisplay = FXCollections.observableArrayList();

    public MainStage() {
        this.gopher = new Gopher();
        this.history = new LinkedList<>();
        this.bookmarkStage = new BookmarkStage();
        this.bookmarkStage.initOwner(this);

        GridPane mainGridPane = new GridPane();
        mainGridPane.setAlignment(Pos.BASELINE_LEFT);
        mainGridPane.setPadding(new Insets(5.0));
        mainGridPane.setVgap(5.0);
        mainGridPane.setHgap(10.0);
        mainGridPane.setPrefSize(800.0, 600.0);

        Button btnBack = new Button("Back");
        btnBack.setPrefWidth(50.0);
        btnBack.setMaxWidth(100.0);
        btnBack.setOnAction(this::backButtonHandler);

        Label lbHost = new Label("Host:");
        this.txtHost = new TextField();
        this.txtHost.setOnAction(this::handleEntityUserInput);

        Label lbPort = new Label("Port:");
        this.txtPort = new TextField("70");
        this.txtPort.setOnAction(this::handleEntityUserInput);

        Label lbSelector = new Label("Selector:");
        this.txtSelector = new TextField();
        this.txtSelector.setOnAction(this::handleEntityUserInput);

        Button btnGo = new Button("Open menu");
        btnGo.setPrefWidth(100.0);
        btnGo.setMaxWidth(100.0);
        btnGo.setOnAction(this::handleEntityUserInput);

        this.lbStatus = new Label();

        Button btnSaveText = new Button("Save as text...");
        btnSaveText.setOnAction(this::saveAsTextHandler);

        Button btnBookmarks = new Button("Bookmarks");
        btnBookmarks.setOnAction(this::showBookmarksScene);

        HBox bottomLineBox = new HBox(10.0, this.lbStatus, btnSaveText, btnBookmarks);
        HBox.setHgrow(this.lbStatus, Priority.ALWAYS);
        bottomLineBox.setAlignment(Pos.CENTER_RIGHT);

        mainGridPane.add(lbHost, 0, 0);
        mainGridPane.add(this.txtHost, 1, 0);
        mainGridPane.add(lbPort, 2, 0);
        mainGridPane.add(this.txtPort, 3, 0);
        mainGridPane.add(lbSelector, 4, 0);
        mainGridPane.add(this.txtSelector, 5, 0);
        mainGridPane.add(btnGo, 6, 0);
        mainGridPane.add(btnBack, 7, 0);
        mainGridPane.add(this.lbStatus, 0, 2, 4, 1);
        mainGridPane.add(bottomLineBox, 5, 2, 3, 1);

        ColumnConstraints labelColumn = new ColumnConstraints(28.0, 28.0, 28.0, Priority.NEVER, HPos.LEFT, true);
        ColumnConstraints hostColumn = new ColumnConstraints(50.0, 150.0, 500.0, Priority.ALWAYS, HPos.LEFT, true);
        ColumnConstraints portColumn = new ColumnConstraints(50.0, 50.0, 50.0, Priority.NEVER, HPos.LEFT, true);
        ColumnConstraints selectorLabelColumn = new ColumnConstraints(50.0, 50.0, 50.0, Priority.NEVER, HPos.LEFT, true);
        ColumnConstraints selectorColumn = new ColumnConstraints(50.0, 80.0, 500.0, Priority.ALWAYS, HPos.RIGHT, true);
        ColumnConstraints buttonColumn1 = new ColumnConstraints(85.0, 85.0, 85.0, Priority.NEVER, HPos.RIGHT, true);
        ColumnConstraints buttonColumn2 = new ColumnConstraints(50.0, 50.0, 100.0, Priority.SOMETIMES, HPos.RIGHT, true);

        mainGridPane.getColumnConstraints().addAll(labelColumn, hostColumn, labelColumn, portColumn, selectorLabelColumn, selectorColumn, buttonColumn1, buttonColumn2);

        this.entityListView = new ListView<>(this.entitiesOnDisplay);
        this.entityListView.setOnMouseClicked(this::listMouseSelectionHandler);
        this.entityListView.setOnKeyPressed(this::listKeySelectionHandler);

        mainGridPane.add(this.entityListView, 0, 1, 8, 1);

        RowConstraints headerFooterRow = new RowConstraints();
        headerFooterRow.setPercentHeight(3.5);
        headerFooterRow.setVgrow(Priority.NEVER);

        RowConstraints contentRow = new RowConstraints();
        contentRow.setPercentHeight(93.0);
        contentRow.setVgrow(Priority.ALWAYS);

        mainGridPane.getRowConstraints().addAll(headerFooterRow, contentRow, headerFooterRow);

        Scene mainScene = new Scene(mainGridPane);
        mainScene.getStylesheets().add("style.css");

        this.setTitle("Gopherit");
        this.setScene(mainScene);
    }

    private void handleConnectionError(String message) {
        this.showErrorMessage("Connection error", "An error occurred trying to connect to the specified host.", message);
        this.setStatus("Connection error.");
    }

    private void saveAsTextHandler(ActionEvent actionEvent) {
        GopherEntity entity = gopher.getEntity();
        if (entity == null) {
            return;
        }

        File fileName = showSaveFileDialog(entity);
        if (fileName == null) {
            return;
        }

        StringBuilder fileContentSb = new StringBuilder();
        for (Object line : entitiesOnDisplay) {
            fileContentSb.append(line.toString()).append("\n");
        }

        try {
            Files.writeString(fileName.toPath(), fileContentSb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            this.showErrorMessage("Writing error", "There was an error writing the new text file.",
                    String.format("The text file \"%s\" could not be created because of the following error:\n%s", fileName.getAbsolutePath(), e.toString()));
            return;
        }

        showInfoMessage("File created", "The text file was successfully created.",
                String.format("A new text file from %s was created on \"%s\".", entity.getEntityAddress(), fileName.getAbsolutePath()));
    }

    private void backButtonHandler(ActionEvent actionEvent) {
        if (this.history.size() > 0) {
            GopherEntity lastEntity = this.history.getLast();
            this.handleGopherEntity(lastEntity, true);
            this.history.remove(lastEntity);
        }
    }

    private void listKeySelectionHandler(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            this.listSelectionHandler();
        }
    }

    private void listMouseSelectionHandler(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            this.listSelectionHandler();
        }
    }

    private void listSelectionHandler() {
        try {
            GopherEntity selectedEntity = (GopherEntity) this.entityListView.getSelectionModel().getSelectedItem();
            GopherEntityType entityType = selectedEntity.getType();
            if (entityType != GopherEntityType.INFORMATION && entityType != GopherEntityType.ERROR) {
                this.handleGopherEntity(selectedEntity, false);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    private void handleEntityUserInput(ActionEvent actionEvent) {
        String host = this.txtHost.getText().trim();
        if (host.isBlank()) {
            return;
        }

        String port = this.txtPort.getText().trim();
        if (port.isBlank()) {
            return;
        }

        String selector = this.txtSelector.getText();
        try {
            this.handleGopherEntity(new GopherEntity(host, port, selector, GopherEntityType.MENU), false);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            this.showErrorMessage("Validation error", "Port number is invalid.", e.getMessage());
        }
    }

    private void handleGopherEntity(GopherEntity entity, boolean back) {
        try {
            GopherEntity currentEntity = this.gopher.getEntity();
            GopherEntityType type = entity.getType();
            if (!(currentEntity == null || back || type != GopherEntityType.MENU && type != GopherEntityType.TEXT_FILE && type != GopherEntityType.SEARCH_SERVER)) {
                this.history.addLast(currentEntity);
                if (this.history.size() > 25) {
                    this.history.removeFirst();
                }
            }

            String newGopherAddress = entity.getEntityAddress();

            switch (type) {

                case SEARCH_SERVER:
                    if (!back) {
                        TextInputDialog searchDialog = new TextInputDialog();
                        searchDialog.setTitle("Search");
                        searchDialog.setHeaderText("Searching on " + newGopherAddress);
                        searchDialog.setContentText("Query:");

                        Optional<String> searchString = searchDialog.showAndWait();
                        if (searchString.isEmpty()) {
                            break;
                        }
                        entity.setSearchSelector(searchString.get());
                    }

                case MENU:
                    this.displayEntityContents(entity, type);
                    this.setStatus("Connected to " + newGopherAddress);
                    break;

                case TEXT_FILE:
                    this.displayEntityContents(entity, type);
                    this.setStatus("Showing contents of text file on " + newGopherAddress);
                    break;

                default:
                    this.downloadEntity(entity);

            }
        } catch (Exception e) {
            e.printStackTrace();
            this.entitiesOnDisplay.clear();
            this.handleConnectionError(e.toString());
        }
    }

    private void downloadEntity(GopherEntity entity) {
        File fileName = showSaveFileDialog(entity);

        if (fileName != null) {
            this.setStatus("Downloading file, please wait...");

            try {
                this.gopher.connect(entity);
            } catch (IOException e) {
                e.printStackTrace();
                this.handleConnectionError(e.toString());
                return;
            }

            String entityAddress = entity.getEntityAddress();

            try {
                this.gopher.saveContentOnDisk(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                this.showErrorMessage("Download error", "There was an error downloading the requested file.",
                        String.format("The file on %s could not be downloaded to %s because of the following error:\n%s",
                                entityAddress, fileName.getAbsolutePath(), e.toString()));
            }
            this.showInfoMessage("Download complete", "The requested file was successfully downloaded.",
                    String.format("The file on %s was saved to \"%s\"", entityAddress, fileName.getAbsolutePath()));
            this.setStatus(String.format("Download complete (%s).", fileName.toString()));
        }
    }

    private File showSaveFileDialog(GopherEntity entity) {
        String initialFileName = "";
        String selector = entity.getSelector();
        if (selector != null && !selector.equals("")) {
            String[] fileNameArr = entity.getSelector().split("/");
            initialFileName = fileNameArr[fileNameArr.length - 1];
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Saving " + entity.getEntityAddress());
        fileChooser.setInitialFileName(initialFileName);
        return fileChooser.showSaveDialog(this);
    }

    private void displayEntityContents(GopherEntity entity, GopherEntityType type) throws IOException {
        this.gopher.connect(entity);

        this.entitiesOnDisplay.clear();
        this.txtHost.setText(entity.getHost());
        this.txtPort.setText("" + entity.getPort());
        this.txtSelector.setText(entity.getSelector());

        switch (type) {

            case SEARCH_SERVER:
            case MENU:
                try {
                    this.entitiesOnDisplay.addAll(this.gopher.getContentFromMenu());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    this.showErrorMessage("Parsing error", "Error parsing the Gopher entity.",
                            "Please make sure the address points to a Gopher Menu.");
                }
                break;

            case TEXT_FILE:
                this.entitiesOnDisplay.addAll(this.gopher.getContentFromTextFile());
        }

        this.entityListView.scrollTo(0);
    }

    private void setStatus(String status) {
        this.lbStatus.setText(status);
    }

    private void showBookmarksScene(ActionEvent actionEvent) {
        this.bookmarkStage.setOnHidden(this::handleBookmark);
        this.bookmarkStage.show(gopher.getEntity());
    }

    private void handleBookmark(WindowEvent event) {
        GopherEntity selectedBookmark = ((BookmarkStage)event.getSource()).getSelectedBookmark();
        if (selectedBookmark != null) {
            this.handleGopherEntity(selectedBookmark, false);
        }
    }

}
