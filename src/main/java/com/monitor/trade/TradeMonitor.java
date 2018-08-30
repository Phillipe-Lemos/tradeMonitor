package com.monitor.trade;

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@SpringBootApplication
public class TradeMonitor extends Application {

    private ConfigurableApplicationContext springContext;    
    
    private Parent root;
    
   
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/monitor.css");
        stage.setTitle("Trading simulator");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            WindowEvent windowsEvent = (WindowEvent)event;
            ((Stage)windowsEvent.getSource()).close();
            System.exit(0);
        });
        stage.setMaximized(Boolean.TRUE);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * 
     * @throws Exception 
     */
    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(TradeMonitor.class);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/monitor.fxml"));
        loader.setControllerFactory(springContext::getBean);
        loader.setResources(ResourceBundle.getBundle("application_en", Locale.ENGLISH));
        root = loader.load();
    }
    
    @Override
    public void stop(){
        springContext.close();
    }

}
