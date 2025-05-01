package org.main.unimap_pc.client.configs;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.main.unimap_pc.client.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


@UtilityClass
public class AppConfig {
    // Files paths
    @Getter private final String PREFS_FILE = "src/main/resources/org/main/unimap_pc/cashe/preferences.ser";
    @Getter private final String CACHE_FILE = "src/main/resources/org/main/unimap_pc/cashe/cache.ser";

    // Main Settings
    @Getter private final String APP_TITLE = "UniMap";
    @Getter private final String GIT_HUB = "https://github.com/Faustynn/VAVA_2024";

    // Image paths
    private static final String IMAGE_BASE_PATH = "/org/main/unimap_pc/images/";
    @Getter private final String ICON_PATH = IMAGE_BASE_PATH + "app/GPS_app.png";
    @Getter private final String STU_LOGO_PATH = IMAGE_BASE_PATH + "app/stu_logo_white.png";
    private final String AVATAR_PATH = IMAGE_BASE_PATH + "avatares/";

    // SSE settings
    @Getter private final String SSE_URL = "http://localhost:8080/api/unimap_pc/sse";
    @Getter private final String SSE_SUBSCRIBE_URL = SSE_URL + "/subscribe";

    // fxml paths
    private static final String VIEWS_PATH = "/org/main/unimap_pc/views/";
    @Getter private final String LOGIN_PAGE_PATH = VIEWS_PATH + "LoginPage.fxml";
    @Getter private final String SIGNUP_PAGE_PATH = VIEWS_PATH + "SignUpPage.fxml";
    @Getter private final String FORGOT_PASS_PAGE_PATH = VIEWS_PATH + "ForgotPass.fxml";
    @Getter private final String FORGOT_PASS_PAGE_PATH2 = VIEWS_PATH + "ForgotPass_second.fxml";
    @Getter private final String MAIN_PAGE_PATH = VIEWS_PATH + "HomePage.fxml";
    @Getter private final String SUBJECTS_PAGE_PATH = VIEWS_PATH + "SubjectsPage.fxml";
    @Getter private final String TEACHERS_PAGE_PATH = VIEWS_PATH + "TeachersPage.fxml";
    @Getter private final String PROFILE_PAGE_PATH = VIEWS_PATH + "ProfilePage.fxml";
    @Getter private final String SETTINGS_PAGE_PATH = VIEWS_PATH + "SettingsPage.fxml";
    @Getter private final String SUBJECTS_SUB_PAGE_PATH = VIEWS_PATH + "SubjectSubPage.fxml";
    @Getter private final String TEACHERS_SUB_PAGE_PATH = VIEWS_PATH + "TeacherSubPage.fxml";
    @Getter private final String COMMENTS_PAGE_PATH = VIEWS_PATH + "CommentsPage.fxml";
    @Getter private final String LOADING_PAGE_PATH = VIEWS_PATH + "LoadingScreen.fxml";

    // API endpoints
    private static final String API_URL = "http://localhost:8080/api/unimap_pc/";
    @Getter private final String CHECK_CONNECTION_URL = API_URL + "check-connection";
    @Getter private final String GET_NEWS_URL = API_URL + "news/all";
    @Getter private final String AUTH_URL = API_URL + "authenticate";
    @Getter private final String REGISTR_URL = API_URL + "register";
    @Getter private final String FIND_USER_BY_EMAIL_URL = API_URL + "user/email/";
    @Getter private final String CONFIRM_CODE_TO_EMAIL = API_URL + "user/email/code";
    @Getter private final String CHANGE_PASSWORD = API_URL + "user/email/change_pass";
    @Getter private final String OAUTH2_GOOGLE = API_URL + "authenticate/google";
    @Getter private final String OAUTH2_FACEBOOK = API_URL + "authenticate/facebook";
    @Getter private final String REFRESH_TOKENS_URL = API_URL + "refresh";
    @Getter private final String SUBJECTS_URL = API_URL + "resources/subjects";
    @Getter private final String TEACHERS_URL = API_URL + "resources/teachers";
    @Getter private final String LOG_URL = API_URL + "log";
    @Getter private final String COMMENTS_URL = API_URL + "comments/";


    // Comment API endpoints
    private static final String ALL_TEACHERS_URL = API_URL + "comments/teacher/";
    private static final String ALL_SUBJECTS_URL = API_URL + "comments/subject/";
    @Getter private final String ADD_TEACHERS_COMMENT_URL = API_URL + "comments/teacher";
    @Getter private final String ADD_SUBJECTS_COMMENT_URL = API_URL + "comments/subject";
    private static final String DELETE_TEACHERS_COMMENT_URL = API_URL + "comments/teacher/";
    private static final String DELETE_SUBJECTS_COMMENT_URL = API_URL + "comments/subject/";
    private static final String DELETE_USER_URL = API_URL + "user/delete/all/";
    private static final String DELETE_COMMENTS_USER_URL = API_URL + "user/delete/comments/";

    // Language settings
    @Getter private final String DEFAULT_LANGUAGE = "English";
    @Getter private final String LANGUAGE_KEY = "preferred_language";
    @Getter private final Map<String, String> LANGUAGE_CODES = Map.of(
            "English", "en",
            "Українська", "ua",
            "Slovenský", "sk"
    );
    @Getter private final Map<String, String> RESOURCE_PATHS = Map.of(
            "en", "org/main/unimap_pc/langs/en",
            "ua", "org/main/unimap_pc/langs/ua",
            "sk", "org/main/unimap_pc/langs/sk"
    );



    // Properties loading
    private static final Properties properties = loadProperties();

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = AppConfig.class.getResourceAsStream("/org/main/unimap_pc/config.properties")) {
            if (input == null) {
                Logger.error("config.properties file is null");
                throw new RuntimeException("config.properties file is null");
            }
            props.load(input);
        } catch (IOException ex) {
            Logger.error("CONFIG ERROR: Failed to load config.properties - " + ex.getMessage());
        }
        return props;
    }



    public String getApiUrl() {
        return API_URL;
    }
    public String getLogLevel() {
        return properties.getProperty("LOG_LEVEL", "INFO");
    }


    private static final Set<String> DEFAULT_AVATARS = Set.of("0.png", "1.png", "2.png", "3.png", "4.png",
                                                              "5.png", "6.png", "7.png", "8.png", "9.png");
    public static Image getAvatar(String avatarname) {
        String imagePath = DEFAULT_AVATARS.contains(avatarname)
                ? "/org/main/unimap_pc/images/avatares/" + avatarname
                : "/org/main/unimap_pc/images/avatares/custom/" + avatarname;

        System.out.println(imagePath);

        try (InputStream resourceStream = AppConfig.class.getResourceAsStream(imagePath)) {
            if (resourceStream == null) {
                System.out.println("!!Avatar image not found: " + imagePath);
                Logger.error("Avatar image not found: " + imagePath);
                return null; // def. val.
            }

            System.out.println("!!Avatar image loaded successfully: " + imagePath);
            return new Image(resourceStream);
        } catch (IOException e) {
            Logger.error("Error loading avatar image: " + e.getMessage());
            return null;
        }
    }

    public String getAllTeacherURL(String id) {
        return ALL_TEACHERS_URL + id;
    }
    public String getAllSubjectsURL(String id) {
        return ALL_SUBJECTS_URL + id;
    }
    public String getDeleteTeacherCommentURL(String id) {
        return DELETE_TEACHERS_COMMENT_URL + id;
    }
    public String getDeleteSubjectsCommentURL(String id) {
        return DELETE_SUBJECTS_COMMENT_URL + id;
    }
    public String getDeleteUserUrl(String id) {
        return DELETE_USER_URL + id;
    }
    public String getDeleteCommentsUserUrl(String id) {
        return DELETE_COMMENTS_USER_URL + id;
    }
}
