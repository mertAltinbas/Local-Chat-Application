import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);

            // Introduction
            out.println("YOUR FIRST MESSAGE WILL BE YOUR USERNAME");
            out.println("⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛");
            out.println("If you want to use a command, you have to start your sentence with '/'\nHere commands that you can use.");
            out.println("/bannedWords, /msg <userName> <message>, /mMsg <userName, userName,...> <message>, /eMsg <userName> <message>");
            out.println("⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛⁛");
            out.println("Enter your name:");
            clientName = in.readLine();
            out.println("Hello " + clientName + "!");
            out.println("Connected Users:");
            for (String userName : server.getUsers()) {
                out.println(userName);
            }

            // notify all other clients
            server.addClient(clientName, this);

            String message;
            while ((message = in.readLine()) != null) {
                if (server.containsBannedPhrase(message)) {
                    out.println("DOES NOT ALLOWED! Check bad words => /bannedWords");
                } else if (message.startsWith("/msg")) {
                    handlePrivateMessage(message);
                } else if (message.startsWith("/mMsg")) {
                    handleManyPrivateMessage(message);
                } else if (message.startsWith("/eMsg")) {
                    handleExceptionMessage(message);
                } else if (message.startsWith("/bannedWords")) {
                    showBannedWords();
                } else {
                    server.broadcast(clientName + ": " + message, this);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // notify disconnect someone
            server.removeClient(clientName);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showBannedWords() {
        for (String word : server.getBannedPhrases()) {
            out.println(word);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void handlePrivateMessage(String message) {
        String[] words = message.split(" ", 3);
        String userName = words[1];
        String msg = words[2];
        server.privateMessage(userName, clientName + " (PM): " + msg);
    }

    private void handleManyPrivateMessage(String message) {
        String[] words = message.split(" ", 3);
        String[] userNames = words[1].split(",");
        String msg = words[2];

        for (String userName : userNames) {
            userName = userName.trim(); // Remove leading and trailing spaces
            server.privateMessage(userName, clientName + " (PM): " + msg);
        }
    }

    private void handleExceptionMessage(String message) {
        String[] words = message.split(" ", 3);
        List<String> allUsernames = server.getUsers();

        String targetUserName = words[1];
        String msg = words[2];

        for (String userName : allUsernames) {
            if (!userName.equals(targetUserName)) { // Don't send
                server.privateMessage(userName, clientName + " (PM): " + msg);
            }
        }
    }
}