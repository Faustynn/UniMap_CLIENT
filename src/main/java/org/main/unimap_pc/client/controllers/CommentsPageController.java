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
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.CommentsService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.utils.WindowDragHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class CommentsPageController implements LanguageSupport {
    // Type of page
    private static final int SUBJECT_PAGE = 1;
    private static final int TEACHER_PAGE = 2;

    // Style constants for different user levels
    private static final String STYLE_REGULAR = "#FFFFFF";
    private static final String STYLE_PREMIUM = "#FFFACD";
    private static final String STYLE_ADMIN = "#E6E6FA";
    private static final String BORDER_REGULAR = "#CCCCCC";
    private static final String BORDER_PREMIUM = "#FFD700";
    private static final String BORDER_ADMIN = "#800080";

    @Setter
    private int page = 0;

    @Setter
    private String lookingParentID = null;

    private double xOffset = 0;
    private double yOffset = 0;

    @Getter
    private int currentRating = 0;

    @FXML public ScrollPane scrollpane;
    @FXML public AnchorPane dragArea, commentAnchorInScrolPane;
    @FXML public FontAwesomeIcon back_btn, star1, star2, star3, star4, star5, star6, refresh_btn;
    @FXML public Label comments_text, add_comment_text, set_stars_text;
    @FXML public Button add_comments_btn;
    @FXML public TextField CommentTextField;
    @FXML public HBox star_box;
    @FXML public ComboBox<String> languageComboBox;

    private final WindowDragHandler windowDragHandler = new WindowDragHandler();

    @FXML
    public void initialize() {
        try {
            initLanguageSupport();
            initCommentTextField();
            setupStarRating();
            windowDragHandler.setupWindowDragging(dragArea);
        } catch (Exception e) {
            Logger.error("Error during comments page initializing: " + e.getMessage());
        }
    }


    private void initLanguageSupport() {
        languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
        loadCurrentLanguage();
        LanguageManager.changeLanguage((String) PreferenceServise.get("LANGUAGE"));
        LanguageManager.getInstance().registerController(this);
        updateUILanguage(LanguageManager.getCurrentBundle());
    }

    private void initCommentTextField() {
        CommentTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 33) {
                CommentTextField.setText(newValue.substring(0, 33) + "\n" + newValue.substring(33));
            }
        });
    }


    /**
     * Set page data
     * @param pageType page type (1 - Subjects, 2 - Teachers)
     * @param id Subject or Teacher ID
     */
    public void setDatas(Integer pageType, String id) {
        if (pageType != null && id != null) {
       //     System.out.println("Page type: " + pageType + " ID: " + id);
            this.page = pageType;
            this.lookingParentID = id;

            Platform.runLater(this::loadComments);
        } else {
            System.out.println("Input is null");
        }
    }

    @FXML
    private void handleCloseApp() throws IOException {
        Stage stage = (Stage) back_btn.getScene().getWindow();

        String resourcePath = getReturnPagePath();
        if (resourcePath != null) {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resourcePath)));
            Scene mainScene = new Scene(root);
            stage.setScene(mainScene);
            stage.show();
        } else {
            stage.close();
        }
    }


    private String getReturnPagePath() {
        return switch (page) {
            case SUBJECT_PAGE -> AppConfig.getSUBJECTS_SUB_PAGE_PATH();
            case TEACHER_PAGE -> AppConfig.getTEACHERS_SUB_PAGE_PATH();
            default -> null;
        };
    }


    private void loadCurrentLanguage() {
        String selectedLanguage = PreferenceServise.get(AppConfig.getLANGUAGE_KEY()).toString();
        languageComboBox.setValue(selectedLanguage);

        // listener for lang. editing
        languageComboBox.setOnAction(event -> {
            try {
                String newLanguage = languageComboBox.getValue();
                String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
                LanguageManager.changeLanguage(languageCode);
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                Logger.error("Error changing language: " + e.getMessage());
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
        Color goldColor = Color.GOLD;
        Color grayColor = Color.web("#dddddd");

        star1.setFill(count >= 1 ? goldColor : grayColor);
        star2.setFill(count >= 2 ? goldColor : grayColor);
        star3.setFill(count >= 3 ? goldColor : grayColor);
        star4.setFill(count >= 4 ? goldColor : grayColor);
        star5.setFill(count >= 5 ? goldColor : grayColor);
        star6.setFill(count >= 6 ? goldColor : grayColor);
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

        JSONObject commentJson = createCommentJson(commentText);
        sendCommentToServer(commentJson.toString());
    }


    private JSONObject createCommentJson(String commentText) {
        String userId = UserService.getInstance().getCurrentUser().getId();
        String levelAccess = getUserAccessLevel();

        return new JSONObject()
                .put("user_id", userId)
                .put("code", lookingParentID)
                .put("text", commentText)
                .put("rating", currentRating)
                .put("levelAccess", levelAccess);
    }


    private String getUserAccessLevel() {
        if (UserService.getInstance().getCurrentUser().isAdmin()) {
            return "2";
        } else if (UserService.getInstance().getCurrentUser().isPremium()) {
            return "1";
        } else {
            return "0";
        }
    }


    private void sendCommentToServer(String jsonComment) {
        CompletableFuture<Boolean> result;

        if (page == SUBJECT_PAGE) {
            result = CommentsService.putNewSubjectComment(jsonComment);
        } else if (page == TEACHER_PAGE) {
            result = CommentsService.putNewTeacherComment(jsonComment);
        } else {
            Logger.error("Invalid page type");
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
                Logger.error("Failed to add comment");
                showErrorDialog("Failed to add comment");
            }
        });
    }


    private void loadComments() {
        CompletableFuture<String> result;

        try {
            if (page == SUBJECT_PAGE) {
                result = CommentsService.loadAllSubjectComments(lookingParentID);
            } else if (page == TEACHER_PAGE) {
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
                            JSONArray commentsArray = new JSONArray(commentsJson);
                            displayComments(commentsArray);
                        } catch (Exception e) {
                            Logger.error("Error parsing comments: " + e.getMessage());
                            showErrorDialog("Error parsing comments: " + e.getMessage());
                        }
                    });
                } else {
                    Logger.error("Failed to load comments");
                    showErrorDialog("Failed to load comments");
                }
            });
        } catch (NumberFormatException e) {
            showErrorDialog("Invalid parent ID format: " + lookingParentID);
            Logger.error("Invalid parent ID format: " + lookingParentID);
        }
    }


    private void displayComments(JSONArray commentsArray) {
        VBox modulesContainer = new VBox(10);

        if (commentsArray.isEmpty()) {
            addNoCommentsLabel(modulesContainer);
        } else {
            addCommentsToContainer(commentsArray, modulesContainer);
        }

        setupScrollPane(modulesContainer, commentsArray.length());

    }


    private void addNoCommentsLabel(VBox container) {
        Label noCommentsLabel = new Label("No comments available");
        noCommentsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        VBox.setMargin(noCommentsLabel, new Insets(150, 0, 0, 150));
        container.getChildren().add(noCommentsLabel);
    }


    private void addCommentsToContainer(JSONArray commentsArray, VBox container) {
        for (int i = 0; i < commentsArray.length(); i++) {
            JSONObject comment = commentsArray.getJSONObject(i);
            String name = comment.has("name") ? comment.getString("name") : "Deleted User";
            Integer commentId = comment.getInt("comment_id");
            String lookingId = comment.getString("looking_id");
            String description = comment.getString("description");
            double rating = comment.getDouble("rating");
            int levelAccess = comment.getInt("levelAccess");

            AnchorPane commentCard = createCommentCard(name, commentId, lookingId, description, rating, levelAccess);
            container.getChildren().add(commentCard);
        }
    }


    private void setupScrollPane(VBox modulesContainer, int commentCount) {
        double totalHeight = 0;
        for (Node child : modulesContainer.getChildren()) {
            totalHeight += child.getBoundsInParent().getHeight();
        }
        totalHeight += (modulesContainer.getChildren().size() - 1) * modulesContainer.getSpacing();
        modulesContainer.setPrefHeight(totalHeight + 40 * commentCount);

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
    }


    private AnchorPane createCommentCard(String name, int commentId, String lookingId, String description, double rating, int levelAccess) {
        AnchorPane modulePane = new AnchorPane();
        applyCardStyle(modulePane, levelAccess);

        // User name
        Label userLabel = createUserLabel(name);

        // Rating display as stars
        HBox ratingBox = createRatingStars(rating);

        // Description text
        Label descriptionText = createDescriptionLabel(description);

        modulePane.getChildren().addAll(userLabel, ratingBox, descriptionText);

        // ADMIN (Delete button)
        if (UserService.getInstance().getCurrentUser().isAdmin()) {
            Label commentIdText = createCommentIdLabel(commentId);
            FontAwesomeIcon deleteIconBtn = createDeleteButton(commentId);
            modulePane.getChildren().addAll(commentIdText, deleteIconBtn);
        }

        // Dinamic height adjustment
        descriptionText.heightProperty().addListener((obs, oldVal, newVal) -> {
            modulePane.setPrefHeight(newVal.doubleValue() + 70);
        });

        return modulePane;
    }


    private void applyCardStyle(AnchorPane pane, int levelAccess) {
        String borderColor;
        String backgroundColor = switch (levelAccess) {
            case 1 -> {
                // Gold style for premium users
                borderColor = BORDER_PREMIUM;
                yield STYLE_PREMIUM;
            }
            case 2 -> {
                // Purple style for admins
                borderColor = BORDER_ADMIN;
                yield STYLE_ADMIN;
            }
            default -> {
                // White style for regular users
                borderColor = BORDER_REGULAR;
                yield STYLE_REGULAR;
            }
        };

        pane.setStyle(
                "-fx-background-color: " + backgroundColor + "; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; "
        );
    }


    private Label createUserLabel(String name) {
        Label userLabel = new Label(name);
        userLabel.setLayoutX(10);
        userLabel.setLayoutY(10);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
        return userLabel;
    }


    private Label createDescriptionLabel(String description) {
        Label descriptionText = new Label(description);
        descriptionText.setLayoutX(10);
        descriptionText.setLayoutY(65);
        descriptionText.setWrapText(true);
        descriptionText.setPrefWidth(399);
        descriptionText.setStyle("-fx-text-fill: black;");
        return descriptionText;
    }


    private Label createCommentIdLabel(int commentId) {
        Label commentIdText = new Label(String.valueOf(commentId));
        commentIdText.setLayoutX(450);
        commentIdText.setLayoutY(10);
        commentIdText.setStyle("-fx-text-fill: black;");
        int paddingValue = commentIdText.getText().length() * 5;
        commentIdText.setPadding(new Insets(0, paddingValue, 0, 0));
        return commentIdText;
    }


    private FontAwesomeIcon createDeleteButton(int commentId) {
        FontAwesomeIcon deleteIconBtn = new FontAwesomeIcon();
        deleteIconBtn.setGlyphName("TRASH");
        deleteIconBtn.setSize("1.5em");
        deleteIconBtn.setLayoutX(475);
        deleteIconBtn.setLayoutY(25);
        deleteIconBtn.setFill(Color.BLACK);

        deleteIconBtn.setOnMouseClicked(event -> deleteComment(commentId));

        return deleteIconBtn;
    }


    private void deleteComment(int commentId) {
        CompletableFuture<Boolean> deleteResult;

        if (page == SUBJECT_PAGE) {
            deleteResult = CommentsService.deleteSubjectComment(String.valueOf(commentId));
        } else if (page == TEACHER_PAGE) {
            deleteResult = CommentsService.deleteTeacherComment(String.valueOf(commentId));
        } else {
            showErrorDialog("Invalid type");
            return;
        }

        deleteResult.thenAccept(success -> {
            if (success) {
                Platform.runLater(this::refreshComments);
            } else {
                showErrorDialog("Failed to delete comment");
            }
        });
    }


    private HBox createRatingStars(double rating) {
        HBox starsBox = new HBox(5);
        starsBox.setStyle("-fx-alignment: center-left;");
        starsBox.setLayoutX(10);
        starsBox.setLayoutY(35);

        // Add start
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

        // Add rating label text
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