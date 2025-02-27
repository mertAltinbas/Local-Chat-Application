import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    static String configFileRoad = "server_config.txt";
    static String usersFilePath = "users.txt";
    private int port;
    private String serverName;
    private List<String> bannedPhrases = new ArrayList<>();
    private Map<String, ClientHandler> clients = new HashMap<>();

    public Server(String configFile) throws IOException {
        loadConfig(configFile);
    }

    private void loadConfig(String configFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            port = Integer.parseInt(reader.readLine().trim());
            serverName = reader.readLine().trim();
            String line;
            while ((line = reader.readLine()) != null) {
                bannedPhrases.add(line.trim());
            }
        }
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println(serverName + " started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(clientSocket, this);
            new Thread(handler).start();
        }
    }

    public synchronized void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public synchronized boolean containsBannedPhrase(String message) {
        for (String phrase : bannedPhrases) {
            if (message.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void addClient(String name, ClientHandler client) {
        clients.put(name, client);
        broadcast(name + " has joined the server.", null);
        saveUserToFile(name, port, usersFilePath);
    }

    public synchronized void removeClient(String name) {
        clients.remove(name);
        broadcast(name + " has left the server.", null);
    }

    public synchronized void saveUserToFile(String userName, int userPort, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) { // append=true
            writer.write(userName + " " + userPort);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error while saving user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void privateMessage(String username, String message) {
        ClientHandler clientHandler = clients.get(username);
        if (clientHandler != null) {
            clientHandler.sendMessage(message);
        } else {
            System.out.println("User not found: " + username);
        }
    }

    public synchronized List<String> getUsers() {
        return new ArrayList<>(clients.keySet());
    }

    public synchronized List<String> getBannedPhrases(){
        return new ArrayList<>(bannedPhrases);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(configFileRoad);
        server.start();
    }
}