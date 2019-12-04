package broker;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class BrokerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        final Logger logger = LoggerFactory.getLogger(getClass());


        URL url  = getClass().getClassLoader().getResource( "broker.fxml");
        if (url != null) {
            Parent root = FXMLLoader.load(url);
            primaryStage.setTitle("Broker");

            primaryStage.setOnCloseRequest(t -> {
                Platform.exit();
                System.exit(0);
            });
            primaryStage.setScene(new Scene(root, 500, 300));
            primaryStage.show();
        }else {
            //System.err.println("Error: Could not load frame from broker.fxml");
            logger.error("Could not load frame from broker.fxml");
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
