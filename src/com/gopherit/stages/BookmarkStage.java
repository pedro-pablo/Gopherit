package com.gopherit.stages;

import com.gopherit.GopherEntity;
import com.gopherit.GopherEntityType;
import com.gopherit.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

class BookmarkStage extends ParentStage {

    private static final String BOOKMARKS_FILENAME = "bookmarks.db";

    private ObservableList<GopherEntity> bookmarks;

    private GopherEntity selectedBookmark;
    private final TextField txtHost;
    private final TextField txtPort;
    private final TextField txtSelector;
    private final TextField txtName;
    private final ChoiceBox<GopherEntityType> cbType;
    private final ListView<GopherEntity> listViewBookmarks;

    BookmarkStage() {
        this.bookmarks = FXCollections.observableArrayList();
        this.loadBookmarksFromDb();

        GridPane mainGridPane = new GridPane();
        mainGridPane.setPadding(new Insets(5.0));
        mainGridPane.setVgap(5.0);
        mainGridPane.setHgap(10.0);

        Label lbHost = new Label("Host:");
        this.txtHost = new TextField();

        Label lbPort = new Label("Port:");
        this.txtPort = new TextField();

        Label lbSelector = new Label("Selector:");
        this.txtSelector = new TextField();

        Label lbType = new Label("Type:");
        this.cbType = new ChoiceBox<>(FXCollections.observableArrayList(
                GopherEntityType.BINARY, GopherEntityType.DOS_BINARY, GopherEntityType.GIF,
                GopherEntityType.HTML, GopherEntityType.IMAGE, GopherEntityType.MAC_BINHEX,
                GopherEntityType.MENU, GopherEntityType.PHONEBOOK, GopherEntityType.SEARCH_SERVER,
                GopherEntityType.SOUND_FILE, GopherEntityType.TELNET, GopherEntityType.TEXT_FILE,
                GopherEntityType.TN3270_TELNET, GopherEntityType.UUENCODED
        ));
        this.cbType.setPrefWidth(300);

        Label lbName = new Label("Name:");
        this.txtName = new TextField();

        Button btnAddBookmark = new Button("Add bookmark");
        btnAddBookmark.setOnAction(this::handleAddBookmark);
        btnAddBookmark.setPrefWidth(500);

        this.listViewBookmarks = new ListView<>(this.bookmarks);
        this.listViewBookmarks.setOnMouseClicked(this::handleListMouseSelection);
        this.listViewBookmarks.setOnKeyPressed(this::handleListKeySelection);

        mainGridPane.add(lbHost, 0, 0);
        mainGridPane.add(this.txtHost, 1, 0);
        mainGridPane.add(lbPort, 2, 0);
        mainGridPane.add(this.txtPort, 3, 0);
        mainGridPane.add(lbSelector, 0, 1);
        mainGridPane.add(this.txtSelector, 1, 1, 3, 1);
        mainGridPane.add(lbName, 0, 2);
        mainGridPane.add(this.txtName, 1, 2);
        mainGridPane.add(lbType, 2, 2);
        mainGridPane.add(this.cbType, 3, 2);
        mainGridPane.add(btnAddBookmark, 0, 3, 4, 1);
        mainGridPane.add(this.listViewBookmarks, 0, 4, 4, 1);

        ColumnConstraints labelColumn1 = new ColumnConstraints();
        labelColumn1.setPercentWidth(11);
        labelColumn1.setHgrow(Priority.NEVER);

        ColumnConstraints hostColumn = new ColumnConstraints();
        hostColumn.setPercentWidth(59);
        hostColumn.setHgrow(Priority.ALWAYS);

        ColumnConstraints labelColumn2 = new ColumnConstraints();
        labelColumn2.setPercentWidth(7);
        labelColumn2.setHgrow(Priority.NEVER);

        ColumnConstraints portColumn = new ColumnConstraints();
        portColumn.setPercentWidth(23);
        portColumn.setHgrow(Priority.ALWAYS);

        RowConstraints normalRow = new RowConstraints();
        normalRow.setPercentHeight(5);

        RowConstraints listViewRow = new RowConstraints();
        listViewRow.setPercentHeight(80);

        mainGridPane.getColumnConstraints().addAll(labelColumn1, hostColumn, labelColumn2, portColumn);
        mainGridPane.getRowConstraints().addAll(normalRow, normalRow, normalRow, normalRow, listViewRow);

        Scene mainScene = new Scene(mainGridPane);
        mainScene.getStylesheets().add("style.css");

        this.setWidth(500.0);
        this.setHeight(700.0);
        this.setResizable(false);
        this.setTitle("Gopherit - Bookmarks");
        this.setScene(mainScene);
    }

    GopherEntity getSelectedBookmark() {
        return this.selectedBookmark;
    }

    private void handleListKeySelection(KeyEvent keyEvent) {
        KeyCode eventCode = keyEvent.getCode();
        switch (eventCode) {
            case ENTER:
                handleListSelection();
                break;
            case DELETE:
                deleteBookmark();
                break;
            default:
                break;
        }
    }

    private void handleListMouseSelection(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            handleListSelection();
        }
    }

    private void handleListSelection() {
        this.selectedBookmark = this.listViewBookmarks.getSelectionModel().getSelectedItem();
        this.close();
    }

    private void handleAddBookmark(ActionEvent actionEvent) {
        String host = txtHost.getText().trim();
        String port = txtPort.getText().trim();
        String selector = txtSelector.getText();
        GopherEntityType type = cbType.getValue();
        String name = txtName.getText().trim();

        if (host.isBlank()) {
            this.showErrorMessage("Validation error", "Host is required",
                    "Please enter a valid host address.");
            return;
        }

        if (port.isBlank()) {
            this.showErrorMessage("Validation error", "Port is required",
                    "Please enter a valid port number.");
            return;
        }

        if (name.isBlank()) {
            this.showErrorMessage("Validation error", "Name is required",
                    "Please enter a name to identify the new bookmark.");
            return;
        }

        if (type == null) {
            this.showErrorMessage("Validation error", "Type is required",
                    "Please choose a type from the menu.");
            return;
        }

        GopherEntity newBookmark;
        try {
            newBookmark = new GopherEntity(host, port, selector, type, name);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            this.showErrorMessage("Validation error", "Port number is invalid.", e.getMessage());
            return;
        }

        Path bookmarksFile = Path.of(BOOKMARKS_FILENAME);
        try {
            if (Files.notExists(bookmarksFile)) {
                DatabaseConnection.createTables();
            }
            newBookmark.insert();
        } catch (SQLException e) {
            e.printStackTrace();
            this.showErrorMessage("Database error", "There was an error writing to the bookmarks file.",
                    String.format("The bookmarks database at \"%s\" could not be written to because of the following error:\n%s",
                            bookmarksFile.toAbsolutePath().toString(), e.toString()));
        }

        this.bookmarks.add(newBookmark);
    }

    private void deleteBookmark() {
        GopherEntity selectedBookmark = this.listViewBookmarks.getSelectionModel().getSelectedItem();
        Path bookmarksFile = Path.of(BOOKMARKS_FILENAME);
        if (Files.exists(bookmarksFile)) {
            try {
                selectedBookmark.delete();
                this.bookmarks.remove(selectedBookmark);
            } catch (SQLException e) {
                e.printStackTrace();
                this.showErrorMessage("Writing error", "There was an error writing changes to the" +
                        "bookmarks file.", String.format("The following error prevented the bookmarks file " +
                        "from being updated:\n%s", e));
            }
        }
    }

    private void loadBookmarksFromDb() {
        Path bookmarksFile = Path.of(BOOKMARKS_FILENAME);

        if (!Files.exists(bookmarksFile)) {
            return;
        }

        try {
            this.bookmarks.addAll(GopherEntity.selectAll());
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Database error", "There was an error reading from the bookmarks file.",
                    String.format("The bookmarks file at \"%s\" could not be read because of the following error:\n%s",
                            bookmarksFile.toAbsolutePath().toString(), e.toString()));
        }

    }

    void show(GopherEntity currentEntity) {
        this.selectedBookmark = null;
        if (currentEntity != null) {
            this.txtHost.setText(currentEntity.getHost());
            this.txtPort.setText("" + currentEntity.getPort());
            this.txtSelector.setText(currentEntity.getSelector());
            this.cbType.getSelectionModel().select(currentEntity.getType());
            this.txtName.setText(currentEntity.getDisplay());
        }
        this.show();
    }

}
