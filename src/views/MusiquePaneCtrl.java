package views;

import java.io.File;
import java.net.URL;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import modele.Preference;
import utils.MediaUtil;
import utils.WindowApp;

public class MusiquePaneCtrl extends WindowApp {

	public static void main(String[] args) { launch(args); }

    @FXML
    private HBox root;
    @FXML
    private VBox controlPane;
    @FXML
    private Label lblMusique;
    @FXML
    private VBox playPaneSlot;    
	@FXML
    private CheckBox checkRejouer;
    @FXML
    private VBox vboxGauche;
    @FXML
    private VBox vboxDroite;
    @FXML
    private Button btnTimeline;
	
	private AnimationPaneCtrl animationPaneCtrl1 = null;
	private AnimationPaneCtrl animationPaneCtrl2 = null;
	private AnimationPaneCtrl animationPaneCtrl3 = null;
	private AnimationPaneCtrl animationPaneCtrl4 = null;

	private PlayPaneCtrl playPaneCtrl = null;
    private Media media = null;
    private static final int CONTROL_PANE_WIDTH = 240;

    private void initAnimation()
    {
    	animationPaneCtrl1= (new AnimationPaneCtrl()).loadView();
    	animationPaneCtrl2 = (new AnimationPaneCtrl()).loadView();
    	animationPaneCtrl3 = (new AnimationPaneCtrl()).loadView();
    	animationPaneCtrl4 = (new AnimationPaneCtrl()).loadView();
    	
    	vboxGauche.getChildren().add(animationPaneCtrl1.getRoot()); 
    	vboxGauche.getChildren().add(animationPaneCtrl2.getRoot());
    	vboxDroite.getChildren().add(animationPaneCtrl3.getRoot());
    	vboxDroite.getChildren().add(animationPaneCtrl4.getRoot()); 
    	
    }
    
    private void initDragAndDrop()
    {
    	controlPane.setOnDragDropped( event -> {
	    	Dragboard db = event.getDragboard();
	        String erreur = null;
	        if (db.hasFiles())
	        {
	        	File f = db.getFiles().get(0);
	        	try {
		        	Media newMedia = new Media(f.toURI().toString());
		            
		        	if( newMedia.getError()!= null )
		        	{
		        		erreur = "Impossible de charger le fichier: " + f.getName();
		        	}
		        	else 
		        	{
		        		playPaneCtrl.bindSong(newMedia, f.getName());
		        	};
	        	}
	        	catch(MediaException e)
	        	{
	        		erreur = "Impossible de charger le fichier: " + f.getName() + " ( " + e.getMessage() + " )";
	        	}
	        }
	        if(erreur != null){
	        	Alert alert = new Alert(AlertType.ERROR, erreur);
	        	alert.showAndWait();        		        	
	        }
	        event.setDropCompleted(erreur != null);
			controlPane.getStyleClass().remove("DragOver");
	        event.consume();    	
	    });
    	
    	controlPane.setOnDragEntered( event -> {
	    	if (event.getDragboard().hasFiles()) 
	    	{
	    		controlPane.getStyleClass().add("DragOver");
	            event.consume();    	
	        }
	    });
    	
    	controlPane.setOnDragExited( event -> {
			controlPane.getStyleClass().remove("DragOver");
			event.consume();    	
	    });
    	
    	controlPane.setOnDragOver( event -> {
			if ( event.getDragboard().hasFiles() ) 
			{
	            event.acceptTransferModes(TransferMode.COPY);
	            event.consume();
	        }
	    });
    }

    @FXML
    private void initialize()
    {
    	initAnimation();
    	initPlayPane();
    	initDragAndDrop();
    	initSize();
    	initBtnTimeline();
    }
    
    private void initSize() {
    	controlPane.setMinWidth(CONTROL_PANE_WIDTH);
    	controlPane.setMaxWidth(CONTROL_PANE_WIDTH);
    	root.sceneProperty().addListener(dontcare -> {
    		root.getScene().windowProperty().addListener(dontcareeither-> {
    			Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
    			getStage().setHeight(screenSize.getHeight() * 0.9);
    			getStage().setOnCloseRequest(event->stockerPreferences());
    			getStage().setOnShown(event->chargerPreferences());
    		});
    	});
    }
    
    private double largeurIdealeDuStage() {
    	double largeurIdeale = CONTROL_PANE_WIDTH + getStage().getHeight();
    	double largeurEcran = Screen.getPrimary().getVisualBounds().getWidth();
    	return Math.min(largeurIdeale, largeurEcran * 0.9);
    }
    
    private void initBtnTimeline() {
    	btnTimeline.setOnAction(action -> {
    		double largeurActuelle = getStage().getWidth();
    		double largeurCible = largeurActuelle > largeurIdealeDuStageSansAnimation() ?
    				largeurIdealeDuStageSansAnimation() : this.largeurIdealeDuStage();
			DoubleProperty stageWidthProxyProperty = new SimpleDoubleProperty(largeurActuelle);
			stageWidthProxyProperty.addListener(dontcare-> getStage().setWidth(stageWidthProxyProperty.get()));
			double opacityStart = largeurActuelle > largeurIdealeDuStageSansAnimation() ?
					1 : 0;
			double opacityEnd = largeurActuelle > largeurIdealeDuStageSansAnimation() ?
					0 : 1;
			DoubleProperty opacityProxyProperty = new SimpleDoubleProperty(opacityStart);
			opacityProxyProperty.addListener(dontcare-> {
				animationPaneCtrl1.setOpacity(opacityProxyProperty.get());
				animationPaneCtrl2.setOpacity(opacityProxyProperty.get());
				animationPaneCtrl3.setOpacity(opacityProxyProperty.get());
				animationPaneCtrl4.setOpacity(opacityProxyProperty.get());
			});
			
    		Timeline timeline = new Timeline(
			new KeyFrame(Duration.millis(0),
					new KeyValue(lblMusique.translateYProperty(), 0),
					new KeyValue(stageWidthProxyProperty, largeurActuelle),
					new KeyValue(opacityProxyProperty, opacityStart)),					
			new KeyFrame(Duration.millis(1000),
					new KeyValue(lblMusique.translateYProperty(), 50)),
			new KeyFrame(Duration.millis(2000),
					new KeyValue(lblMusique.translateYProperty(), 0),
					new KeyValue(lblMusique.scaleXProperty(), 1)),
			new KeyFrame(Duration.millis(3000),
					new KeyValue(lblMusique.scaleXProperty(), 1.5)),
			new KeyFrame(Duration.millis(4000),
					new KeyValue(stageWidthProxyProperty, largeurCible),
					new KeyValue(opacityProxyProperty, opacityEnd),
					new KeyValue(lblMusique.scaleXProperty(), 1))
			);
    		timeline.playFromStart();
    	});
    }
    
    private double largeurIdealeDuStageSansAnimation() {
    	return CONTROL_PANE_WIDTH 
    			+ root.getPadding().getLeft()
    			+ root.getPadding().getRight();
    }
    
    private Stage getStage() {
    	return (Stage) root.getScene().getWindow();
    }
    
    private void initPlayPane()
    {
    	URL musique = getClass().getResource("/ressources/All Along The Watchtower.mp3");
    	media = new Media(musique.toExternalForm());
    	playPaneCtrl = (new PlayPaneCtrl()).loadView();
    	playPaneSlot.getChildren().add(playPaneCtrl.getRoot());
    	VBox.setVgrow(playPaneCtrl.getRoot(), Priority.SOMETIMES);
    	playPaneCtrl.config( 
    			newStatus->onPlayingStatusChange(newStatus), 
    			()->onEndOfMedia());
    	playPaneCtrl.bindSong(media, "All Along The Watchtower");
    	animationPaneCtrl1.bindRate(playPaneCtrl.volumeProperty());
    	animationPaneCtrl2.bindRate(playPaneCtrl.volumeProperty());
    	animationPaneCtrl3.bindRate(playPaneCtrl.volumeProperty());
    	animationPaneCtrl4.bindRate(playPaneCtrl.volumeProperty());
    }
    
    private void onEndOfMedia()
    {
		if(checkRejouer.isSelected()) playPaneCtrl.bindAndPlay(media, "All Along The Watchtower"); 
    }
    
    private void onPlayingStatusChange(Status newStatus)
    {
		if(newStatus == Status.PLAYING) { 
			animationPaneCtrl1.play();
			animationPaneCtrl2.play();
			animationPaneCtrl3.play();
			animationPaneCtrl4.play();
		}
		else {
			animationPaneCtrl1.pause();
			animationPaneCtrl2.pause();
			animationPaneCtrl3.pause();
			animationPaneCtrl4.pause();
		}
    }
    
    private void stockerPreferences() {
    	Preference pref = new Preference() {{
    		stageWidth = getStage().getWidth();
    		stageHeight = getStage().getHeight();
    		Media media = playPaneCtrl.getBindedMedia();
    		songFile = media == null ? null : media.getSource();
    		volumeMusic = playPaneCtrl.volumeProperty().get();
    		isEnBoucle = checkRejouer.isSelected();
    	}};
    	pref.stocker();
    }
    
    private void chargerPreferences() {
    	Preference pref = Preference.charger();
    	if (pref == null) return;
		if(pref.stageHeight > 0) getStage().setHeight(
				Math.min(pref.stageHeight, Screen.getPrimary().getVisualBounds().getHeight()));
		if(pref.stageWidth > 0) getStage().setWidth(
				Math.min(pref.stageWidth, Screen.getPrimary().getVisualBounds().getWidth()));
		if(pref.songFile != null)
			bindSongFile(pref.songFile);
		else
			playPaneCtrl.bindNoSong();
    	playPaneCtrl.volumeProperty().set(pref.volumeMusic);
    	checkRejouer.setSelected(pref.isEnBoucle);
    	
    }
    
    private void bindSongFile(String songFile) {
    	Media media = new Media(songFile);
    	playPaneCtrl.bindSong(media, MediaUtil.getMediaFileName(media));
  
    }
    
    @Override protected ContextMenu getTestMenu() {
    	return new ContextMenu (
    			new MenuItem("Sauver préférences") {{ setOnAction (event->stockerPreferences()); }},
			    new MenuItem("Charger préférences") {{ setOnAction (event->chargerPreferences()); }},
			    new MenuItem("Unbind Media") {{ setOnAction (event->playPaneCtrl.bindNoSong()); }},
			    new MenuItem("Quitter") {{ setOnAction (event->{ }); }}
    			);
    }

   
    @Override
	protected String getFxml() { return "MusiquePane.fxml"; }
    @Override
	protected Parent getRoot() { return root; }
	@Override
	protected String getTitle() { return "TP3 - Musique - Dyden Ung"; }
	@Override
	protected Image getIcon() {
		return new Image("/ressources/icon.png");
	}
    
}

