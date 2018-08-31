import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.MapChangeListener;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.io.FilenameFilter;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import java.util.stream.Collectors;

public class MusicPlayer extends Application {
  private FilenameFilter filter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name.endsWith(".mp3");
    }
  };
  private Path currentPath = Paths.get("");
  private String s = currentPath.toAbsolutePath().toString();
  private File directory = new File(s);
  private File[] listOfFiles = directory.listFiles(filter);
  private List<MP3> listOfMusic = new ArrayList<>();
  private MediaPlayer selectedMediaPlayer;
  private ObservableList<MP3> items;
  private List<MP3> backingList;
  private String selected;
  private String artistTemp;
  private String albumTemp;
  private String titleTemp;
  private String searchCategory;
  private String searchTerm;

  @Override
  public void start(Stage primaryStage)
      throws FileNotFoundException, IOException {
    VBox vbox = new VBox();
    HBox hbox = new HBox();
    List<MP3> selectedList = new ArrayList<>();
    TableView<MP3> table = new TableView<>();
    table.setEditable(true);
    table.getSelectionModel().selectedItemProperty().addListener(
        new ChangeListener() {
          @Override
          public void changed(ObservableValue observableValue, Object oldValue,
                              Object newValue) {
            if (table.getSelectionModel().getSelectedItem() != null) {
              MP3 selectedmedia = table.getSelectionModel().getSelectedItem();
              selectedList.clear();
              selectedList.add(selectedmedia);
              selectedMediaPlayer = new MediaPlayer(selectedmedia.getMedia());
            }
          }
        });

    for (File file : listOfFiles) {
      String source = file.toURI().toString();
      Media media = new Media(source);
      MP3 mp3 = new MP3(media, file);
      ObservableMap<String, Object> metaData = media.getMetadata();

      metaData.addListener(new MapChangeListener<String, Object>() {
        @Override
        public void onChanged(Change<? extends String, ? extends Object> ch) {
          if (ch.wasAdded()) {
            if ("artist".equals(ch.getKey())) {
              mp3.setArtist(metaData.get("artist").toString());
            } else if ("title".equals(ch.getKey())) {
              mp3.setTitle(metaData.get("title").toString());
            } else if ("album".equals(ch.getKey())) {
              mp3.setAlbum(metaData.get("album").toString());
            }
          }
        }
      });
      listOfMusic.add(mp3);
    }

    backingList = listOfMusic;

    Button playButton = new Button("Play");
    Button pauseButton = new Button("Pause");
    Button searchButton = new Button("Search Songs");
    Button showButton = new Button("Show all Songs");

    playButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        playButton.setDisable(true);
        pauseButton.setDisable(false);
        MediaPlayer mediaplay = selectedMediaPlayer;
        mediaplay.play();
      }
    });

    pauseButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        pauseButton.setDisable(true);
        playButton.setDisable(false);
        MediaPlayer mediapause = selectedMediaPlayer;
        mediapause.pause();
      }
    });

    pauseButton.setDisable(true);
    showButton.setDisable(true);

    items = FXCollections.observableList(listOfMusic);

    searchButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        searchButton.setDisable(true);
        showButton.setDisable(false);

        String[] arrayChoice = {"Artist", "Album", "Title", "File Name"};
        List<String> dialogData;

        dialogData = Arrays.asList(arrayChoice);

        ChoiceDialog dialog = new ChoiceDialog(dialogData.get(0), dialogData);

        dialog.setHeaderText("Select one");
        dialog.setContentText("Choose an attribute");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
          dialog.close();
          searchCategory = result.get();
        } else {
          dialog.close();
        }

        TextInputDialog textDialog = new TextInputDialog("type here");
        Optional<String> textresult = textDialog.showAndWait();

        textDialog.setTitle("Search");
        textDialog.setHeaderText("Enter " + searchCategory);

        if (textresult.isPresent()) {
          searchTerm = textresult.get();
        }
        if (searchCategory.toLowerCase().equals("artist")) {
          listOfMusic = listOfMusic.stream()
                            .filter(x
                                    -> x.getArtist().toLowerCase().contains(
                                        searchTerm.toLowerCase()))
                            .collect(Collectors.toList());
        } else if (searchCategory.toLowerCase().equals("album")) {
          listOfMusic = listOfMusic.stream()
                            .filter(x -> x.getAlbum() != null)
                            .collect(Collectors.toList());

          listOfMusic = listOfMusic.stream()
                            .filter(x
                                    -> x.getAlbum().toLowerCase().contains(
                                        searchTerm.toLowerCase()))
                            .collect(Collectors.toList());
        } else if (searchCategory.toLowerCase().equals("title")) {
          listOfMusic = listOfMusic.stream()
                            .filter(x
                                    -> x.getTitle().toLowerCase().contains(
                                        searchTerm.toLowerCase()))
                            .collect(Collectors.toList());
        } else if (searchCategory.toLowerCase().equals("file name")) {
          listOfMusic = listOfMusic.stream()
                            .filter(x
                                    -> x.getFile().toLowerCase().contains(
                                        searchTerm.toLowerCase()))
                            .collect(Collectors.toList());
        }

        items = FXCollections.observableList(listOfMusic);
        table.setItems(items);
        table.refresh();
      }
    });
    showButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        searchButton.setDisable(false);
        showButton.setDisable(true);

        items = FXCollections.observableList(backingList);

        table.setItems(items);
        table.refresh();

        listOfMusic = backingList;
      }
    });

    FilteredList<MP3> filteredList = new FilteredList<>(items, p -> true);
    ListView<MP3> listViewOfMedia = new ListView<MP3>(items);

    TableColumn songs = new TableColumn("File Name");
    songs.setCellValueFactory(
        new PropertyValueFactory<MP3, SimpleStringProperty>("file"));
    TableColumn artist = new TableColumn("Artist");
    artist.setCellValueFactory(
        new PropertyValueFactory<MP3, SimpleStringProperty>("artist"));
    TableColumn album = new TableColumn("Album");
    album.setCellValueFactory(
        new PropertyValueFactory<MP3, SimpleStringProperty>("album"));
    TableColumn title = new TableColumn("Title");
    title.setCellValueFactory(
        new PropertyValueFactory<MP3, SimpleStringProperty>("title"));
    TableColumn attributes = new TableColumn("Attributes");

    attributes.getColumns().addAll(artist, title, album);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    attributes.setMaxWidth(1f * Integer.MAX_VALUE * 50);

    hbox.getChildren().addAll(playButton, pauseButton, showButton,
                              searchButton);
    table.setItems(items);
    vbox.getChildren().addAll(table, hbox);
    table.getColumns().addAll(songs, attributes);

    vbox.setFillWidth(true);
    vbox.setVgrow(table, Priority.ALWAYS);
    Scene scene = new Scene(vbox, 500, 500);

    primaryStage.setTitle("Music Player");
    primaryStage.setScene(scene);

    primaryStage.show();
    table.refresh();
  }

  public class MP3 {
    private String file;
    private String artist;
    private String album;
    private String title;
    private String selected;
    private Media media;
    private MediaPlayer mediaPlayer;

    public MP3(Media media, File file)
        throws FileNotFoundException, IOException {
      this.media = media;
      this.file = file.getName();
      this.mediaPlayer = new MediaPlayer(media);
    }

    public void setArtist(String artist) { this.artist = artist; }
    public void setTitle(String title) { this.title = title; }
    public void setAlbum(String album) { this.album = album; }
    public void setSelected(String selected) { this.selected = selected; }
    public String getFile() { return this.file; }
    public String getTitle() { return this.title; }
    public String getArtist() { return this.artist; }
    public String getAlbum() { return this.album; }
    public String getSelected() { return this.selected; }
    public Media getMedia() { return this.media; }
    public MediaPlayer getMediaPlayer() { return this.mediaPlayer; }
  }
}
