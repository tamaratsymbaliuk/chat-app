package com.onlinechatapp.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public ChatClient(String host, int port) {

        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your username: ");
            username = scanner.nextLine();
            writer.write(username);
            writer.newLine();
            writer.flush();

            // Start a thread to listen for messages from the server
            new Thread(new MessageListener()).start();
            // Main thread for sending messages
            while (true) {
                String message = scanner.nextLine();
                writer.write(message);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    private class MessageListener implements Runnable {
        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.err.println("Disconnected from the server.");
                closeResources();
            }
        }
    }

    public static void main(String[] args) {
        String host = "localhost"; // Default host
        int port = 12345; // Default port
        new ChatClient(host, port);
    }
}
