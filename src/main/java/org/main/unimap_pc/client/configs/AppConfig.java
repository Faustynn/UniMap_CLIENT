package org.main.unimap_pc.client.configs;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

// TODO: Rewrite prop file with lombok

@Getter
@Setter
public class AppConfig {
    // General app settings
    private static final String APP_TITLE = "UniMap";
    private static final String ICON_PATH = "/org/main/unimap_pc/images/GPS_app.png";
    private static final String GIT_HUB = "https://github.com/Faustynn/VAVA_2024";
    @Getter
    private static final String PREFS_FILE = "org/main/unimap_pc/cashe/preferences.ser";
    @Getter
    private static final String CACHE_FILE = "org/main/unimap_pc/cashe/cache.ser";
    // FXMLs
    private static final String LOGIN_PAGE_PATH = "/org/main/unimap_pc/views/LoginPage.fxml";
    private static final String SIGNUP_PAGE_PATH = "/org/main/unimap_pc/views/SignUpPage.fxml";
    private static final String FORGOT_PASS_PAGE_PATH = "/org/main/unimap_pc/views/ForgotPass.fxml";
    private static final String FORGOT_PASS_PAGE_PATH2 = "/org/main/unimap_pc/views/ForgotPass_second.fxml";

    private static final String MAIN_PAGE_PATH = "/org/main/unimap_pc/views/HomePage.fxml";
    private static final String SUBJECTS_PAGE_PATH = "/org/main/unimap_pc/views/SubjectsPage.fxml";
    private static final String TEACHERS_PAGE_PATH = "/org/main/unimap_pc/views/TeachersPage.fxml";
    private static final String PROFILE_PAGE_PATH = "/org/main/unimap_pc/views/ProfilePage.fxml";
    private static final String SETTINGS_PAGE_PATH = "/org/main/unimap_pc/views/SettingsPage.fxml";

    private static final String SUBJECTS_SUB_PAGE_PATH = "/org/main/unimap_pc/views/SubjectSubPage.fxml";
    private static final String TEACHERS_SUB_PAGE_PATH = "/org/main/unimap_pc/views/TeacherSubPage.fxml";

    // APIs
    private static final String API_URL = "http://localhost:8080/api/unimap_pc/";
    private static final String CHECK_CONNECTION_URL = API_URL + "check-connection";
    private static final String GET_NEWS_URL = API_URL + "news/all";

    private static final String AUTH_URL = API_URL + "authenticate";
    private static final String REGISTR_URL = API_URL + "register";

    private static final String FIND_USER_BY_EMAIL_URL = API_URL + "user/email/";

    private static final String CONFIRM_CODE_TO_EMAIL = API_URL + "user/email/code";
    private static final String CHANGE_PASSWORD = API_URL + "user/email/password";

    private static final String OAUTH2_GOOGLE = API_URL + "authenticate/google";
    private static final String OAUTH2_FACEBOOK = API_URL + "authenticate/facebook";

    private static final String REFRESH_TOKENS_URL = API_URL + "refresh";

    private static final String SUBJECTS_URL = API_URL + "resources/subjects";
    private static final String TEACHERS_URL = API_URL + "resources/teachers";
    private static final String COMMENTS_URL = API_URL + "comments/";
    // Getters
    public static String getLoginPagePath() {
        return LOGIN_PAGE_PATH;
    }
    public static String getMainPagePath() {
        return MAIN_PAGE_PATH;
    }
    public static String getSignupPagePath() {
        return SIGNUP_PAGE_PATH;
    }
    public static String getForgotPassPagePath() {
        return FORGOT_PASS_PAGE_PATH;
    }
    public static String getCheckConnectionUrl() {
        return CHECK_CONNECTION_URL;
    }
    public static String getAuthUrl() {
        return AUTH_URL;
    }
    public static String getFindUserByEmailUrl() {
        return FIND_USER_BY_EMAIL_URL;
    }
    public static String getAppTitle() {
        return APP_TITLE;
    }
    public static String getIconPath() {
        return ICON_PATH;
    }
    public static String getGithubPage() {
        return GIT_HUB;
    }
    public static String getRegistrUrl() {
        return REGISTR_URL;
    }
    public static String getForgotPassPagePath2() {
        return FORGOT_PASS_PAGE_PATH2;
    }
    public static String getConfirmCodeToEmail() {
        return CONFIRM_CODE_TO_EMAIL;
    }
    public static String getChangePassword() {
        return CHANGE_PASSWORD;
    }
    public static String getSubjectsPagePath() {
        return SUBJECTS_PAGE_PATH;
    }
    public static String getTeachersPagePath() {
        return TEACHERS_PAGE_PATH;
    }
    public static String getProfilePagePath() {
        return PROFILE_PAGE_PATH;
    }
    public static String getSettingsPagePath() {
        return SETTINGS_PAGE_PATH;
    }
    public static String getCommentsUrl() {
        return COMMENTS_URL;
    }
    public static String getSubjectsSubPagePath() {
        return SUBJECTS_SUB_PAGE_PATH;
    }
    public static String getTeachersSubPagePath() {
        return TEACHERS_SUB_PAGE_PATH;
    }
    @Getter
    private static final String DEFAULT_LANGUAGE = "English";
    @Getter
    private static final String LANGUAGE_KEY = "preferred_language";

    @Getter
    private static final Map<String, String> LANGUAGE_CODES = Map.of(
            "English", "en",
            "Українська", "ua",
            "Slovenský", "sk"
    );
    @Getter
    private static final Map<String, String> RESOURCE_PATHS = Map.of(
            "en", "org/main/unimap_pc/langs/en",
            "ua", "org/main/unimap_pc/langs/ua",
            "sk", "org/main/unimap_pc/langs/sk"
    );


    public static String getSubjectsUrl() {
        return SUBJECTS_URL;
    }
    public static String getTeachersUrl() {
        return TEACHERS_URL;
    }





    private static final Properties properties = new Properties();
    static {
        try (InputStream input = AppConfig.class.getResourceAsStream("/org/main/unimap_pc/config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties file is null");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getOauth2Google() {
        return OAUTH2_GOOGLE;
    }

    public static String getOauth2Facebook() {
        return OAUTH2_FACEBOOK;
    }

    public static String getRefreshTokenUrl() {
        return REFRESH_TOKENS_URL;
    }


    public static Image getAvatar(String avatarID) {
        String imagePath = "/org/main/unimap_pc/images/avatares/" + avatarID + ".png";
        System.out.println(imagePath);

        URL resource = AppConfig.class.getResource(imagePath);
        if (resource == null) {
            System.err.println("Avatar image not found: " + imagePath);
            return null; // or return a default image
        }

        return new Image(resource.toString());
    }

    public static String getNewsUrl() {
        return GET_NEWS_URL;
    }
}
