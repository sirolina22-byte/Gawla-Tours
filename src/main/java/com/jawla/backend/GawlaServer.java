package com.jawla.backend;

import java.io.*;
import java.net.*;

public class GawlaServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Gawla Server is READY (Waiting for connection...)");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Connected!");
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    public ClientHandler(Socket socket) { this.socket = socket; }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            String request = in.readLine();
            if (request == null) return;

            System.out.println("Received Request: " + request);
            String[] parts = request.split(":");
            String command = parts[0];

            if (command.equals("LOGIN")) {
                // مؤقتاً: أي حد بيدخل بنقوله SUCCESS ونبعتله رتبة "admin" عشان يشوف كل التابات
                out.println("SUCCESS:admin"); 
            } 
            else if (command.equals("SIGNUP")) {
                // مؤقتاً: بنقوله تمام سجلناك
                out.println("REGISTERED");
            }
            
        } catch (Exception e) {
            System.out.println("Client Disconnected.");
        }
    }
}