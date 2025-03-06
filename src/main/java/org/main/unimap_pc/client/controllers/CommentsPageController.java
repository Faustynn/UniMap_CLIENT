package org.main.unimap_pc.client.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.Setter;
import org.json.JSONObject;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.CommentsService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class CommentsPageController implements LanguageSupport {
    // 1 - from subject page, 2 - from teacher page
    @Setter
    private int page = 0;
    // subjectid or teacherid
    @Setter
    private String lookingParentID = null;

    public ScrollPane scrollpane;
    public AnchorPane dragArea, commentAnchorInScrolPane;
    public FontAwesomeIcon back_btn;
    public Label comments_text,add_comment_text,set_stars_text;
    public Button add_comments_btn;
    public TextField CommentTextField;
    public FontAwesomeIcon star1,star2,star3,star4,star5,star6,refresh_btn;
    public HBox star_box;
    public ComboBox languageComboBox;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }
    @FXML
    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) dragArea.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    private void handleCloseApp() throws IOException {
        Stage stage = (Stage) back_btn.getScene().getWindow();

        if (page == 1){
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getSubjectsSubPagePath())));
            Scene mainScene = new Scene(root);
            stage.setScene(mainScene);
            stage.show();
        }else if (page == 2){
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getTeachersSubPagePath())));
            Scene mainScene = new Scene(root);
            stage.setScene(mainScene);
            stage.show();
        } else {
            stage.close();
        }
    }

    public void setDatas(Integer numb, String text) {
        if (numb != null && text != null) {
            System.out.println("numb: " + numb + " text: " + text);
            page = numb;
            lookingParentID = text;

            Platform.runLater(this::loadComments);
        } else {
            System.out.println("Input is null");
        }
    }

    @FXML
    public void initialize() {
        try {
            languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
            loadCurrentLanguage();
            LanguageManager.changeLanguage((String) PreferenceServise.get("LANGUAGE"));
            LanguageManager.getInstance().registerController(this);
            updateUILanguage(LanguageManager.getCurrentBundle());

            CommentTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 33) {
                    CommentTextField.setText(newValue.substring(0, 33) + "\n" + newValue.substring(33));
                }
            });

            setupStarRating();

        } catch (Exception e) {
            e.printStackTrace();
        }


        dragArea.setOnMousePressed(this::handleMousePressed);
        dragArea.setOnMouseDragged(this::handleMouseDragged);

        Scene scene = dragArea.getScene();
        if (scene != null) {
            scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                // TODO:Resize logic
            });
            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                // TODO:Resize logic
            });
        }
    }

    private void loadCurrentLanguage() {
        String selectedLanguage = PreferenceServise.get(AppConfig.getLANGUAGE_KEY()).toString();
        languageComboBox.setValue(selectedLanguage);

        // listener for lang. editing
        languageComboBox.setOnAction(event -> {
            try {
                String newLanguage = (String) languageComboBox.getValue();
                String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
                LanguageManager.changeLanguage(languageCode);
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                showErrorDialog("Error changing language: " + e.getMessage());
                loadCurrentLanguage();
            }
        });
    }
    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        languageComboBox.setPromptText(languageBundle.getString("language.combobox"));
        comments_text.setText(languageBundle.getString("comments.text"));
        add_comment_text.setText(languageBundle.getString("add.comment.text"));
        set_stars_text.setText(languageBundle.getString("set.stars.text"));
        add_comments_btn.setText(languageBundle.getString("add.comments.btn"));

    }


    private int currentRating = 0;
    private void setupStarRating() {
        currentRating = 0;

        // Set up star click events
        star1.setOnMouseClicked(event -> setRating(1));
        star2.setOnMouseClicked(event -> setRating(2));
        star3.setOnMouseClicked(event -> setRating(3));
        star4.setOnMouseClicked(event -> setRating(4));
        star5.setOnMouseClicked(event -> setRating(5));
        star6.setOnMouseClicked(event -> setRating(6));

        // Set hover effects for better UX
        star1.setOnMouseEntered(event -> highlightStars(1));
        star2.setOnMouseEntered(event -> highlightStars(2));
        star3.setOnMouseEntered(event -> highlightStars(3));
        star4.setOnMouseEntered(event -> highlightStars(4));
        star5.setOnMouseEntered(event -> highlightStars(5));
        star6.setOnMouseEntered(event -> highlightStars(6));

        // Reset to current rating when mouse leaves star box
        star_box.setOnMouseExited(event -> updateStarAppearance());

        // Initialize star appearance
        updateStarAppearance();
    }

    private void setRating(int rating) {
        currentRating = rating;
        updateStarAppearance();
    }

    private void highlightStars(int count) {
        star1.setFill(count >= 1 ? Color.GOLD : Color.web("#dddddd"));
        star2.setFill(count >= 2 ? Color.GOLD : Color.web("#dddddd"));
        star3.setFill(count >= 3 ? Color.GOLD : Color.web("#dddddd"));
        star4.setFill(count >= 4 ? Color.GOLD : Color.web("#dddddd"));
        star5.setFill(count >= 5 ? Color.GOLD : Color.web("#dddddd"));
        star6.setFill(count >= 6 ? Color.GOLD : Color.web("#dddddd"));
    }

    private void updateStarAppearance() {
        highlightStars(currentRating);
    }



    @FXML
    public void handleСomments_button() {
        String commentText = CommentTextField.getText();
        if (commentText.isEmpty()) {
            showErrorDialog("Comment cannot be empty");
            return;
        }

        String user_id = UserService.getInstance().getCurrentUser().getId();
        String levelAccess;
        if (UserService.getInstance().getCurrentUser().isAdmin()) {
            levelAccess = "2";
        } else if (UserService.getInstance().getCurrentUser().isPremium()) {
            levelAccess = "1";
        }else {
            levelAccess = "0";
        }

        String jsonComment = new JSONObject()
                .put("user_id", user_id)
                .put("code", lookingParentID)
                .put("text", commentText)
                .put("rating", currentRating)
                .put("levelAccess", levelAccess)
                .toString();

        CompletableFuture<Boolean> result;
        if (page == 1) {
            result = CommentsService.putNewSubjectComment(jsonComment);
        } else if (page == 2) {
            result = CommentsService.putNewTeacherComment(jsonComment);
        } else {
            showErrorDialog("Invalid page type");
            return;
        }

        result.thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    CommentTextField.clear();
                    currentRating = 0;
                    updateStarAppearance();
                    refreshComments();
                });
            } else {
                showErrorDialog("Failed to add comment");
            }
        });
    }
    private void loadComments() {
        CompletableFuture<String> result;
        System.out.println(AppConfig.getAllTeacherURL("1234"));

        try {
            if (page == 1) {
                result = CommentsService.loadAllSubjectComments(lookingParentID);
            } else if (page == 2) {
                result = CommentsService.loadAllTeacherComments(lookingParentID);
            } else {
                showErrorDialog("Invalid page type");
                return;
            }

            result.thenAccept(commentsJson -> {
                if (commentsJson != null) {
                    Platform.runLater(() -> {
                        try {
                            commentAnchorInScrolPane.getChildren().clear();
                            System.out.println(commentsJson);
                            org.json.JSONArray commentsArray = new org.json.JSONArray(commentsJson);
                            displayComments(commentsArray);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showErrorDialog("Error parsing comments: " + e.getMessage());
                        }
                    });
                } else {
                    showErrorDialog("Failed to load comments");
                }
            });
        } catch (NumberFormatException e) {
            showErrorDialog("Invalid parent ID format: " + lookingParentID);
            e.printStackTrace();
        }
    }


    private AnchorPane displayComments(org.json.JSONArray commentsArray) {
        VBox modulesContainer = new VBox(10);

        if (commentsArray.length() == 0) {
            Label noCommentsLabel = new Label("No comments available");
            noCommentsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            VBox.setMargin(noCommentsLabel, new Insets(150, 0, 0, 150));

            modulesContainer.getChildren().add(noCommentsLabel);
        } else {
            for (int i = 0; i < commentsArray.length(); i++) {
                JSONObject comment = commentsArray.getJSONObject(i);
                String name = comment.getString("name");
                Integer comment_id = comment.getInt("comment_id");
                String lookingId = comment.getString("looking_id");
                String description = comment.getString("description");
                double rating = comment.getDouble("rating");
                int levelAccess = comment.getInt("levelAccess");

                AnchorPane commentCard = createCommentCard(name,comment_id, lookingId, description, rating, levelAccess);
                modulesContainer.getChildren().add(commentCard);
            }
        }
        double totalHeight = 0;
        for (Node child : modulesContainer.getChildren()) {
            totalHeight += child.getBoundsInParent().getHeight();
        }
        totalHeight += (modulesContainer.getChildren().size() - 1) * modulesContainer.getSpacing();
        modulesContainer.setPrefHeight(totalHeight+40*commentsArray.length());
        scrollpane.setContent(modulesContainer);
        scrollpane.setFitToWidth(true);
        scrollpane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-control-inner-background: transparent;"
        );
        scrollpane.getStyleClass().add("transparent-scroll-pane");

        if (!commentAnchorInScrolPane.getChildren().contains(scrollpane)) {
            commentAnchorInScrolPane.getChildren().add(scrollpane);
        }

        return commentAnchorInScrolPane;
    }

    private AnchorPane createCommentCard(String name,int comment_id, String lookingId, String description, double rating, int levelAccess) {
        AnchorPane modulePane = new AnchorPane();

        String borderColor;
        String backgroundColor;
        switch (levelAccess) {
            case 1:
                // Gold style for level 1
                borderColor = "#FFD700";
                backgroundColor = "#FFFACD";
                break;
            case 2:
                // Purple style for level 2
                borderColor = "#800080";
                backgroundColor = "#E6E6FA";
                break;
            default:
                // White style for level 0
                borderColor = "#CCCCCC";
                backgroundColor = "#FFFFFF";
                break;
        }

        // Apply styles
        modulePane.setStyle(
                "-fx-background-color: " + backgroundColor + "; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; "
        );

        // User ID
        Label userLabel = new Label(name);
        userLabel.setLayoutX(10);
        userLabel.setLayoutY(10);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        // Rating display as stars
        HBox ratingBox = createRatingStars(rating);
        ratingBox.setLayoutX(10);
        ratingBox.setLayoutY(35);
        ratingBox.setStyle("-fx-alignment: center-left;");

        // Description text
        Label descriptionText = new Label(description);
        descriptionText.setLayoutX(10);
        descriptionText.setLayoutY(65);
        descriptionText.setWrapText(true);
        descriptionText.setPrefWidth(399);
        descriptionText.setStyle("-fx-text-fill: black;");

        modulePane.getChildren().addAll(userLabel, ratingBox, descriptionText);

        // Dynamically adjust module height based on description text
        descriptionText.heightProperty().addListener((obs, oldVal, newVal) -> {
            modulePane.setPrefHeight(newVal.doubleValue() + 70);
        });

        Label comment_idText = new Label(String.valueOf(comment_id));
        comment_idText.setLayoutX(450);
        comment_idText.setLayoutY(10);
        comment_idText.setStyle("-fx-text-fill: black;");

        return modulePane;
    }


    private HBox createRatingStars(double rating) {
        HBox starsBox = new HBox(5);
        starsBox.setStyle("-fx-alignment: center-left;"); // Align stars to the left
        for (int i = 1; i <= 5; i++) {
            FontAwesomeIcon starIcon = new FontAwesomeIcon();
            starIcon.setGlyphName("STAR");
            starIcon.setSize("1.5em");

            if (i <= rating) {
                starIcon.setFill(Color.GOLD);
            } else if (i - 0.5 == rating) {
                starIcon.setFill(Color.GOLD);
                starIcon.setOpacity(0.5);
            } else {
                starIcon.setFill(Color.web("#dddddd"));
            }

            starsBox.getChildren().add(starIcon);
        }

        Label ratingLabel = new Label(String.format(" %.1f", rating).replace(".", ","));
        ratingLabel.setStyle("-fx-font-size: 1.2em; -fx-text-fill: black;");
        starsBox.getChildren().add(ratingLabel);

        return starsBox;
    }

    @FXML
    public void refreshComments() {
        loadComments();
    }

}
