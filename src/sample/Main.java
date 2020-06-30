package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
int xOff = 470;
int yOff = 680;
int w =1200;
int h = 1200;
    @Override
    public void start(Stage stage) {
        Label message = new Label("Click on the area to add new site!");
        message.setPadding(new Insets(5, 5, 5, 5));
        Canvas canvas = new Canvas(w, h);
FileReader fr = new FileReader();

        FileChooser fc = new FileChooser();

        List<File> retVal = new ArrayList<File>();
       retVal=  fc.showOpenMultipleDialog(null);

        GraphicsContext gc = canvas.getGraphicsContext2D();
gc.strokeLine(0,yOff,w,yOff);
        gc.strokeLine(xOff,0,xOff,h);
        gc.strokeText("X",w-30,yOff-20);
        gc.strokeText("Y",xOff+ 10,30);

        gc.strokeLine(30,30,100,30);
        gc.strokeText("1 metrs",30,20);

        if(retVal!= null) {
            for (File f:retVal
                 ) {
                fr.drawFromFile(gc, xOff, yOff, f.getAbsolutePath());
           allRoleChanges[fr.id]=fr.rolesChanged;
            }

        }

        BorderPane root = new BorderPane(canvas);

        root.setTop(message);
        Scene scene = new Scene(root, w, h);
        stage.setScene(scene);
        stage.setTitle("vsmRoomba");
        stage.show();
        drawPlotInWindow();
    }

    ArrayList<Pair<Integer,String>>[] allRoleChanges= new ArrayList[20];

    int getMinTime(){
        int min = Integer.MAX_VALUE;
        for (ArrayList<Pair<Integer,String>> a:allRoleChanges) {
            if(a!=null){
                for (int i = 0; i < a.size(); i++) {
                    Pair<Integer,String> p = a.get(i);
                if(p.getKey()<min)min=p.getKey();
                }

            }
        }
      return min;
    }

    void drawPlotInWindow() {

//  Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
        Stage stage = new Stage();
        stage.setX(100);
        stage.setY(100);

        StackPane root = new StackPane();

        Canvas canvas = new Canvas(1000, 800);

        root.getChildren().add(canvas);
        stage.setTitle("canvas");

        stage.setScene(new Scene(root));
        stage.show();
      int startTime = getMinTime();

        ArrayList<Pair<Integer,String>> rolesChanged;
        int nr = 0;
        int h =1;
        for (ArrayList<Pair<Integer,String>> a:allRoleChanges) {

            if(a!=null) {
                FileReader.drawTimeAxis(startTime, a, canvas, h,nr);
                System.out.println("drawing "+nr);
            h++;
            }
            nr++;
            }
            FileReader.drawTimeAxisTimeLabels(0,canvas,h);
            }

    public static void main(String[] args) {
        launch(args);
    }
}
