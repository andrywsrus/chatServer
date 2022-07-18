package server;

import server.handlers.ClientHandler;
import server.services.AuthenticationService;
import server.services.impl.SimpleAuthenticationServiceImpl;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MyServer {

    private final ServerSocket serverSocket;
    private final AuthenticationService authenticationService;
    private final ArrayList<ClientHandler> clients;

    private ArrayList<String> clientsNameOnline;


    public MyServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        authenticationService = new SimpleAuthenticationServiceImpl();
        clients = new ArrayList<>();
        clientsNameOnline = new ArrayList<>();
    }

    public void start() {
        System.out.println("CЕРВЕР ЗАПУЩЕН!");
        System.out.println("---------------");
        while (true) {
            try {
                waitAndProcessNewClientConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitAndProcessNewClientConnection() throws IOException {
        System.out.println("Ожидание клиента...");
        Socket socket = serverSocket.accept();
        System.out.println("Клиент подключился!");

        processClientConnection(socket);
    }

    private void processClientConnection(Socket socket) throws IOException {
        ClientHandler handler = new ClientHandler(this, socket);
        handler.handle();
    }

    public boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler handler) {
        clients.add(handler);
        clientsNameOnline.add(handler.getUsername());
    }

    public synchronized void unSubscribe(ClientHandler handler) {
        clients.remove(handler);
        clientsNameOnline.remove(handler.getUsername());
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void stop() {
        System.out.println("------------------");
        System.out.println("------------------");
        System.out.println("ЗАВЕРШЕНИЕ РАБОТЫ");
        System.exit(0);
    }

    public synchronized void broadcastMessage(ClientHandler sender, String message) throws IOException {
        for (ClientHandler client : clients) {
            /*if (client == sender) {
                continue;
            }*/
            client.sendMessage(sender.getUsername(), message);
        }
    }

    public synchronized void privateBroadcastMessage(ClientHandler sender, String recipient, String message) throws IOException {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            if (client.getUsername().equals(recipient)) {
                client.sendMessage(sender.getUsername(), message, true);
            }
        }
        System.out.println(clientsNameOnline.toString());
    }

    public synchronized void broadcastServerMessage(ClientHandler sender, String message) throws IOException {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendServerMessage(message);
        }
    }

    public synchronized void broadcastOnlineClients() throws IOException {
        for (ClientHandler client : clients) {
            client.sendServerOnlineClients(String.join(";",clientsNameOnline));
        }
    }
}
