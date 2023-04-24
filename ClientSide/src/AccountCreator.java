import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class AccountCreator extends Application {
    String[] accountDetails = new String[2];
    private Socket socket;
    private PrintWriter writer;

    public void start(Stage primaryStage) {
        connectToServer();

        primaryStage.setTitle("Welcome");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Welcome to Siddharth's Library");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Image image = new Image("file:///Users/siddharthiyer/Documents/GitHub/422C_Final_Project_ssi325/picture.jpg");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        grid.add(imageView, 0, 1, 2, 1);

        Label userName = new Label("Username:");
        // Ensure label is wide enough to display text
        userName.setMinWidth(60);
        grid.add(userName, 0, 2);
        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 2);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 3);
        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 3);

        Button createAccountButton = new Button("Create Account");
        // Ensure button is wide enough to display text
        createAccountButton.setMinWidth(110);
        Button createAnotherAccountButton = new Button("Create Another Account");
        createAnotherAccountButton.setMinWidth(150);
        createAnotherAccountButton.setVisible(false);

        Text accountCreatedText = new Text("Account Created!");
        accountCreatedText.setVisible(false);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(createAccountButton, accountCreatedText, createAnotherAccountButton);
        grid.add(buttonBox, 1, 4);

        createAccountButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = userTextField.getText();
                String password = pwBox.getText();

                if (!username.isEmpty() && !password.isEmpty()) {
                    sendAccountToServer(username, password);
                    createAccountButton.setVisible(false);
                    accountCreatedText.setVisible(true);
                    createAnotherAccountButton.setVisible(true);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Account Creation Failed");
                    alert.setContentText("Please enter a username and password.");
                    alert.showAndWait();
                }
            }
        });

        createAnotherAccountButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createAccountButton.setVisible(true);
                createAnotherAccountButton.setVisible(false);
                accountCreatedText.setVisible(false);
                userTextField.clear();
                pwBox.clear();
            }
        });

        Scene scene = new Scene(grid, 550, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

        private void connectToServer() {
        try {
            socket = new Socket("localhost", 4999);
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAccountToServer(String username, String hashedPassword) {
        writer.println("NewAccount " + username + " " + hashedPassword);
    }

}
