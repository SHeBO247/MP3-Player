package mp3.player;

import java.io.File;
import static java.lang.Math.floor;
import static java.lang.String.format;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MP3Player extends Application {
    
    boolean isPlay = false;
    MediaView view;
    MediaPlayer player;
    Media media;
    
    Button playButton;
    Label musicName;
    Label time;
    Slider seekSlider;
    Duration duration;
    
    @Override
    public void start(Stage primaryStage) {
        AnchorPane root = new AnchorPane();
        
        String path = "C:/Users/SHeBO/Desktop/music.mp3";
        media = new Media(new File(path).toURI().toString());
        player = new MediaPlayer(media);
        player.setAutoPlay(false);
        view = new MediaView(player);
        
        view.setLayoutX(0);
        view.setLayoutY(0);
        
        // PLay Pause
        Image playButtonImage = new Image(getClass().getResourceAsStream("play.png"));
        Image pauseButtonImage = new Image(getClass().getResourceAsStream("pause.png"));
        
        playButton = new Button();
        playButton.setLayoutX(15);
        playButton.setLayoutY(15);
        playButton.setPrefSize(70, 70);
        //playButton.setStyle("-fx-background-color: Transparnt");
        playButton.setGraphic(new ImageView(playButtonImage));

        playButton.setOnAction((ActionEvent e) -> {
            isPlay = !isPlay;
            
            if (isPlay){
                player.play();
                playButton.setGraphic(new ImageView(pauseButtonImage));
            }
            else{
                player.pause();
                playButton.setGraphic(new ImageView(playButtonImage));
            }
            
        });
        playButton.addEventHandler(MouseEvent.MOUSE_ENTERED, (MouseEvent e) -> {
            //playButton.setStyle("-fx-background-color: Black");
            //playButton.setStyle("-fx-body-color: Black");
        });
        playButton.addEventHandler(MouseEvent.MOUSE_EXITED, (MouseEvent e) -> {
            //playButton.setStyle("-fx-background-color: Black");
        });
        
        // File Button
        Image fileButtonImage = new Image(getClass().getResourceAsStream("Files.png"));
        Button fileButton = new Button();
        fileButton.setLayoutX(15);
        fileButton.setLayoutY(95);
        fileButton.setPrefSize(20, 20);
        fileButton.setGraphic(new ImageView(fileButtonImage));
        
        fileButton.setOnAction((ActionEvent event) -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3"));
            
            File audioFile = chooser.showOpenDialog(null);
            if (audioFile != null){
                String filePath = audioFile.getAbsolutePath();
                filePath = filePath.replace("\\", "/");
                primaryStage.setTitle("MP3 Player - " + audioFile.getName());
                
                media = new Media(new File(filePath).toURI().toString());
                // Stop the old music
                player.stop();
                
                player = new MediaPlayer(media);
                isPlay = true;
                player.play();
                //duration = player.getCurrentTime();
                updateValues();
                playButton.setGraphic(new ImageView(pauseButtonImage));
                musicName.setText(audioFile.getName());
                view = new MediaView(player);
            }
            else{
                return;
            }
            
        });
        
        // Retry Button
        Image retryButtonImage = new Image(getClass().getResourceAsStream("Retry.png"));
        Button retryButton = new Button();
        retryButton.setLayoutX(55);
        retryButton.setLayoutY(95);
        retryButton.setPrefSize(20, 20);
        retryButton.setGraphic(new ImageView(retryButtonImage));
        
        retryButton.setOnAction((ActionEvent event) -> {
            player.seek(player.getStartTime());
        });
        
        // Music name Label
        musicName = new Label();
        musicName.setLayoutX(130);
        musicName.setLayoutY(25);
        musicName.setText("music.mp3");
        
        // Slider
        seekSlider = new Slider();
        seekSlider.setLayoutX(130);
        seekSlider.setLayoutY(50);
        seekSlider.setPrefSize(150, 20);
        
        seekSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (seekSlider.isValueChanging()){
                    if (duration != null){
                        player.seek(duration.multiply(seekSlider.getValue() / 100.0));
                    }
                    updateValues();
                }
            }
        });
        
        player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                updateValues();
            }
        });
        
        // Time Label
        time = new Label();
        time.setLayoutX(295);
        time.setLayoutY(50);
        time.setTextFill(Color.BLACK);
        
        player.currentTimeProperty().addListener((Observable ov) -> {
            updateValues();
        }); 

        player.setOnReady(() -> {
            duration = player.getMedia().getDuration();
            updateValues();
        });
        
        root.getChildren().addAll(playButton, fileButton, retryButton, musicName, seekSlider, time);
        Scene scene = new Scene(root, 380, 150);
        
        primaryStage.setTitle("MP3 Player");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    protected void updateValues() {
        if (time != null && seekSlider != null && duration != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = player.getCurrentTime();
                    time.setText(formatTime(currentTime, duration));
                    seekSlider.setDisable(duration.isUnknown());
                    if (!seekSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !seekSlider.isValueChanging()) {
                    seekSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                }

                }
            });
        }
    }
    
    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
            if (durationHours > 0) {
                return format("%d:%02d:%02d/%d:%02d:%02d",
                elapsedHours, elapsedMinutes, elapsedSeconds,
                durationHours, durationMinutes, durationSeconds);
            } else {
                return format("%02d:%02d/%02d:%02d",
                elapsedMinutes, elapsedSeconds, durationMinutes,
                durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return format("%d:%02d:%02d", elapsedHours,
                elapsedMinutes, elapsedSeconds);
            } else {
                return format("%02d:%02d", elapsedMinutes,
                elapsedSeconds);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
