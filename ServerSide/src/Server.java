import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.scene.control.TableView;
//import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDate;

public class Server {
    private ArrayList<ItemCatalog> catalog;
    private ArrayList<Socket> clients;

    private HashMap<String, String> userMap;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private TableView<ItemCatalog> tableView;

    public static void main(String[] args) {
        try {
            new Server().startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Server() {
        catalog = new ArrayList<ItemCatalog>();
        clients = new ArrayList<Socket>();
        try {
            serverSocket = new ServerSocket(4999);
        } catch (IOException ignored) {
        }
        isRunning = false;

        // read in JSON file and create ArrayList of ItemCatalog objects
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<ItemCatalog>>() {
        }.getType();
        try {
            FileReader reader = new FileReader("src/library.json");
            catalog = gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        userMap = new HashMap<>();

    }

    private void startServer() {
        ExecutorService executor = Executors.newFixedThreadPool(100);

        while (true) {
            try {
                System.out.println("Waiting for new client connection...");
                Socket clientSocket = serverSocket.accept();
                clients.add(clientSocket);
                System.out.println("New client connected");

                executor.submit(new ClientHandler(clientSocket));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                // send catalog to client
//                Gson gson = new Gson();
//                String catalogJson = gson.toJson(catalog);
//                writer.println(catalogJson);

                while (true) {
                    // read incoming messages from client
//                    LocalDate today = LocalDate.now();
//                    for(ItemCatalog c : catalog){
//                        if(c.getDueDate().equals(today)){
//                            boolean renewed = c.renew();
//                            if(!renewed) {
//                                processCommands("Return", c.getTitle(), c.getCurrUser(), writer);
//                            }
//                        }
//                    }

                    String message = reader.readLine();
                    if (message == null) {
                        // client disconnected
                        clients.remove(clientSocket);
                        break;
                    }
                    System.out.println("Received message: " + message);
                    String[] parts = message.split(" ", 3);
                    String command = parts[0];
                    String username = parts[1];
                    String title = parts[2];
                    processCommands(command, title, username, writer);
                    sendCatalogUpdate();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clients.remove(clientSocket);
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private synchronized void processCommands(String command, String title, String username, PrintWriter writer) {
            switch (command) {
                case ("Authenticate"):
                    if (userMap.containsKey(username)) {
                        if (userMap.get(username).equals(title)) {
                            System.out.println("Authenticated");
                            writer.println("Authenticated");
                            sendCatalogUpdate();
                        } else {
                            System.out.println("Invalid password");
                            writer.println("Invalid");
                        }
                    } else {
                        System.out.println("User does not exist");
                        writer.println("Invalid");
                    }
                    break;
                case ("NewAccount"):
                    userMap.put(username, title);
                    System.out.println("New account created: " + username + " " + title);
                    break;
                case ("Checkout"):
                    for (ItemCatalog c : catalog) {
                        if (c.getTitle().equals(title)) {
                            c.checkOut(username);
                            System.out.println("Item checked out: " + title);
                            break;
                        }
                    }
                    sendCatalogUpdate();
                    break;
                case ("Return"):
                    for (ItemCatalog c : catalog) {
                        if (c.getTitle().equals(title)) {
                            if(c.isHeld()){
                                c.processHold();
                                System.out.println("Item returned: " + title);
                                System.out.println("Item checked out to hold user");
                            }
                            else {
                                String prevUser = c.returnItem();
                                System.out.println("Item returned: " + title);
                                break;
                            }
                        }
                    }
                    sendCatalogUpdate();
                    break;
                case ("Hold"):
                    for (ItemCatalog c : catalog) {
                        if (c.getTitle().equals(title)) {
                            c.holdItem(username);
                            System.out.println("Item held: " + title);
                            break;
                        }
                    }
                    sendCatalogUpdate();
                    break;
                default:
                    System.out.println("Invalid command: " + command);
                    break;
            }
        }

        private void sendCatalogUpdate() {
            Gson gson = new Gson();
            String catalogJson = gson.toJson(catalog);

            // send updated catalog to all connected clients
            for (Socket clientSocket : clients) {
                try {
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println(catalogJson);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
