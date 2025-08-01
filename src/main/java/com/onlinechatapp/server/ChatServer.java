package com.onlinechatapp.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final int PORT = 12345;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);
            DatabaseHelper.initializeDatabase();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String username;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                this.out = out;
                out.println("Welcome to the chat! Please enter your username:");
                username = in.readLine();

                DatabaseHelper.saveUser(username);
                broadcast(username + " has joined the chat.", this);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }
                    String formattedMessage = username + ": " + message;
                    DatabaseHelper.saveMessage(username, message);
                    System.out.println(formattedMessage);
                    broadcast(formattedMessage, this);
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
                removeClient(this);
                broadcast(username + " has left the chat.", this);
            }
        }

        void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}
