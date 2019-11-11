package com.gopherit;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;

public class Gopher {
    private GopherEntity entity;
    private Socket socket;
    private BufferedReader socketInput;

    public Gopher() { }

    public GopherEntity getEntity() {
        return this.entity;
    }

    public void connect(GopherEntity entity) throws IOException {
        this.socket = new Socket(entity.getHost(), entity.getPort());
        this.entity = entity;
        this.openStreams();
    }

    private void openStreams() throws IOException {
        this.socketInput = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        OutputStreamWriter socketOutput = new OutputStreamWriter(this.socket.getOutputStream());
        socketOutput.write(this.entity.getSelector() + "\r\n");
        socketOutput.flush();
    }

    public ArrayList<GopherEntity> getContentFromMenu() throws IOException, IllegalArgumentException {
        String line;
        ArrayList<GopherEntity> content = new ArrayList<>();
        while ((line = this.socketInput.readLine()) != null && line.charAt(0) != '.') {
            content.add(new GopherEntity(line));
        }
        this.socket.close();
        return content;
    }

    public ArrayList<String> getContentFromTextFile() throws IOException {
        String line;
        ArrayList<String> content = new ArrayList<>();
        while ((line = this.socketInput.readLine()) != null) {
            content.add(line + "\n");
        }
        this.socket.close();
        return content;
    }

    public void saveContentOnDisk(File fileName) throws IOException {
        byte[] contentBytes = this.socket.getInputStream().readAllBytes();
        Files.write(fileName.toPath(), contentBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        this.socket.close();
    }
}