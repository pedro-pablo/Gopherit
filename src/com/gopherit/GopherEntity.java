package com.gopherit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GopherEntity {

    private static final String SQL_SELECTION_ALL_STATEMENT = "SELECT * FROM bookmark";
    private static final String SQL_INSERTION_STATEMENT = "INSERT INTO bookmark (host, port, selector, type, name) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_DELETION_STATEMENT = "DELETE FROM bookmark WHERE id = ?";
    private static final String PORT_REGEX = "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

    private int id;
    private GopherEntityType type;
    private String display;
    private String selector;
    private String host;
    private int port;

    public GopherEntity(String host, String portStr, String selector, GopherEntityType type) throws IllegalArgumentException {
        setHost(host);
        setPort(portStr);
        setSelector(selector);
        setType(type);
    }

    public GopherEntity(String host, String portStr, String selector, GopherEntityType type, String display) throws IllegalArgumentException {
        this(host, portStr, selector, type);
        setDisplay(display);
    }

    private GopherEntity(String host, int port, String selector, int type, String display, int id) throws IllegalArgumentException {
        setHost(host);
        setPort(port);
        setSelector(selector);
        setType(GopherEntityType.getType((char)type));
        setDisplay(display);
        setId(id);
    }

    GopherEntity(String line) {
        line = line.replace("\r\n", "");
        this.setType(line.charAt(0));
        try {
            int tabChar1 = line.indexOf('\t');
            int tabChar2 = line.indexOf('\t', tabChar1 + 1);
            int tabChar3 = line.lastIndexOf('\t');
            this.setDisplay(line.substring(1, tabChar1));
            if (this.getType() != GopherEntityType.INFORMATION) {
                this.setSelector(line.substring(tabChar1 + 1, tabChar2));
                this.setHost(line.substring(tabChar2 + 1, tabChar3));
                this.setPort(Integer.parseInt(line.substring(tabChar3 + 1)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid entity line:\n" + line, e);
        }
    }

    private int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public GopherEntityType getType() {
        return this.type;
    }

    private void setType(char typeChar) {
        this.type = GopherEntityType.getType(typeChar);
    }

    private void setType(GopherEntityType type) {
        this.type = type;
    }

    public String getDisplay() {
        return this.display;
    }

    private void setDisplay(String display) {
        this.display = display;
    }

    public String getSelector() {
        return this.selector;
    }

    private void setSelector(String selector) {
        this.selector = selector;
    }

    public void setSearchSelector(String searchString) {
        String selector = getSelector();
        if (selector.isEmpty()) {
            setSelector(searchString);
        } else {
            setSelector(selector + '\t' + searchString);
        }
    }

    public String getHost() {
        return this.host;
    }

    private void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    private void setPort(String portStr) {
        if (!portStr.matches(PORT_REGEX)) {
            throw new IllegalArgumentException(String.format("\"%s\" is not a valid port number.", portStr));
        }
        setPort(Integer.parseInt(portStr));
    }

    private void setPort(int port) {
        this.port = port;
    }

    public String toString() {
        return this.getType() == GopherEntityType.INFORMATION ?
                this.getDisplay() :
                String.format("[%s] %s (%s:%d%s)", this.getType(), this.getDisplay(), this.getHost(),
                        this.getPort(), this.getSelector());
    }

    public String getEntityAddress() {
        return String.format("%s:%d%s", this.host, this.port, this.selector);
    }

    public void insert() throws SQLException {
        Connection connection = DatabaseConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement(SQL_INSERTION_STATEMENT);
        statement.setString(1, getHost());
        statement.setInt(2, getPort());
        statement.setString(3, getSelector());
        statement.setInt(4, getType().getAssociatedChar());
        statement.setString(5, getDisplay());

        statement.execute();
    }

    public void delete() throws SQLException {
        Connection connection = DatabaseConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement(SQL_DELETION_STATEMENT);
        statement.setInt(1, getId());

        statement.execute();
    }

    public static List<GopherEntity> selectAll() throws SQLException {
        ArrayList<GopherEntity> entities = new ArrayList<>();

        Connection connection = DatabaseConnection.getInstance();

        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery(SQL_SELECTION_ALL_STATEMENT);

        while (results.next()) {
            GopherEntity gopherEntity = new GopherEntity(
                results.getString("host"),
                results.getInt("port"),
                results.getString("selector"),
                results.getInt("type"),
                results.getString("name"),
                results.getInt("id")
            );
            entities.add(gopherEntity);
        }

        return entities;
    }

}