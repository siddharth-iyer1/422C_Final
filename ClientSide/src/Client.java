import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.util.Callback;


import java.net.InetAddress;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.*;
import java.util.ArrayList;

public class Client extends Application {

    Socket sock;
    String username;
    PrintWriter writer;
    BufferedReader reader;


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Welcome");

        // ESTABLISH CONNECTION WITH SERVER
        try {
            setUpNetworking();
        } catch (IOException ex) {
        }

        showLoginScene(primaryStage);
    }

    private void setUpNetworking() throws IOException{
        sock = new Socket(InetAddress.getLocalHost().getHostAddress(), 4999);
        InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
        reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        writer = new PrintWriter(sock.getOutputStream(), true);
        System.out.println("networking established");
    }

    private boolean authenticate(String username, String password) {
        try {
            writer.println("Authenticate " + username + " " + password);
            System.out.println("DEBUG: Sent authentication request to server.");
            String response = reader.readLine();
            System.out.println(response);
            return "Authenticated".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showLoginScene(Stage primaryStage) {
        // Create login scene
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));

        // Add title
        Text scenetitle = new Text("Welcome to Siddharth's Library");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        loginGrid.add(scenetitle, 0, 0, 2, 1);

        // Add username label and text field
        Label userName = new Label("Username:");
        loginGrid.add(userName, 0, 1);
        TextField userTextField = new TextField();
        loginGrid.add(userTextField, 1, 1);

        // Add password label and password field
        Label pw = new Label("Password:");
        loginGrid.add(pw, 0, 2);
        PasswordField pwBox = new PasswordField();
        loginGrid.add(pwBox, 1, 2);

        // Add login button
        Button btn = new Button("Login");
        loginGrid.add(btn, 1, 4);

        // Add error message text
        final Text actiontarget = new Text();
        loginGrid.add(actiontarget, 1, 6);

        // Set action on login button
        btn.setOnAction(e -> {
            username = userTextField.getText();
            String password = pwBox.getText();
            if (authenticate(username, password)) {
                showDatabaseScene(primaryStage);
            } else {
                actiontarget.setText("Invalid username or password.");
            }
        });

        // Create login scene and add grid to it
        Scene loginScene = new Scene(loginGrid, 400, 300);
        primaryStage.setScene(loginScene);

        // Show stage
        primaryStage.show();
    }

    private void showDatabaseScene(Stage primaryStage) {
        primaryStage.setTitle("Item Database");

        // At the top of this view, display the username of the user who is logged in and have a hyperlink next to it to see the user's checked out items.
        Text usernameText = new Text("Username: " + username);
        // Add a logout button that will return the user to the login screen.
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            showLoginScene(primaryStage);
        });

        // Create table columns
        TableColumn<ItemCatalog, String> itemTypeCol = new TableColumn<>("Item Type");
        itemTypeCol.setCellValueFactory(new PropertyValueFactory<>("itemType"));

        TableColumn<ItemCatalog, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<ItemCatalog, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));

        TableColumn<ItemCatalog, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("desc"));

        TableColumn<ItemCatalog, Boolean> checkedOutCol = new TableColumn<>("Checked Out?");
        checkedOutCol.setCellValueFactory(new PropertyValueFactory<>("checkedOut"));

        // Create table and set columns
        TableView<ItemCatalog> table = new TableView<>();
        table.setItems(getItems());
        table.getColumns().addAll(itemTypeCol, titleCol, authorCol, descCol, checkedOutCol);

        // Add Check Out button to each row for items that are not already checked out
        TableColumn<ItemCatalog, Void> actionCol = new TableColumn<>("Action");
        actionCol.setSortable(false);
        Callback<TableColumn<ItemCatalog, Void>, TableCell<ItemCatalog, Void>> cellFactory = new Callback<TableColumn<ItemCatalog, Void>, TableCell<ItemCatalog, Void>>() {
            @Override
            public TableCell<ItemCatalog, Void> call(final TableColumn<ItemCatalog, Void> param) {
                final TableCell<ItemCatalog, Void> cell = new TableCell<ItemCatalog, Void>() {

                    private final Button btn = new Button();

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            ItemCatalog data = getTableView().getItems().get(getIndex());
                            if(data.isCheckedOut() && data.getCurrUser().equals(username)){
                                PrintWriter writer = null;
                                try {
                                    writer = new PrintWriter(sock.getOutputStream(), true);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                // send message to server
                                writer.println("Return " + username + " " + data.getTitle());
                                writer.flush();
                            } else if(!data.isCheckedOut()){
                                PrintWriter writer = null;
                                try {
                                    writer = new PrintWriter(sock.getOutputStream(), true);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                // send message to server
                                writer.println("Checkout " + username + " " + data.getTitle());
                                writer.flush();
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            ItemCatalog data = getTableView().getItems().get(getIndex());
                            if(data.isCheckedOut() && data.getCurrUser().equals(username)){
                                btn.setText("Return");
                                setGraphic(btn);
                            }else if(!data.isCheckedOut()){
                                btn.setText("Check Out");
                                setGraphic(btn);
                            }else{
                                setGraphic(null);
                            }
                        }
                    }
                };
                return cell;
            }
        };
        actionCol.setCellFactory(cellFactory);

        // Add columns to table
        table.getColumns().add(actionCol);

        // Create layout and add table to it
        VBox vbox = new VBox();
        vbox.getChildren().addAll(usernameText, table, logoutBtn);
        Scene scene = new Scene(vbox, 1550, 400);
        primaryStage.setScene(scene);

        // Show stage
        primaryStage.show();
        // Create a listener to update the table when a message is received from the server without using IncomingReader
        Thread readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("reader thread running");
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<ItemCatalog>>(){}.getType();

                    while (true) { // Keep the thread running to constantly read messages
                        String line = reader.readLine();
                        if (line == null) {
                            break; // If the connection is closed or interrupted, break the loop
                        }

                        ArrayList<ItemCatalog> catalog = gson.fromJson(line, type);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                table.setItems(FXCollections.observableArrayList(catalog));
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readerThread.start();
    }

    // Method to get all ItemCatalog objects
    private ObservableList<ItemCatalog> getItems() {
        ObservableList<ItemCatalog> items = FXCollections.observableArrayList();

        try {
            // connect to server
//            Socket socket = new Socket("localhost", 4999);
            BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // read catalog from server and convert to ArrayList of ItemCatalog objects
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ItemCatalog>>(){}.getType();
            ArrayList<ItemCatalog> catalog = gson.fromJson(reader.readLine(), type);

            // add items from catalog to the ObservableList
            items.addAll(catalog);
            for(ItemCatalog i : catalog){
                System.out.println(i.isCheckedOut());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

}