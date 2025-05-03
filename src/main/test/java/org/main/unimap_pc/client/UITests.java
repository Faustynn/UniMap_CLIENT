package org.main.unimap_pc.client;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class UITests {
    // A little disclaimer is we ALWAYS need to log in to the application
    // Otherwise the app won't really allow the access to the panels
    // I have tried it and after fixing the issue for a couple of hours
    // Understood the best decision is just simulate the user flow, indeed

    private final String login = "nazar";

    @Start
    private void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass()
                .getResource("/org/main/unimap_pc/views/LoginPage.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }



    // Everytime we are performing the test, the user should be logged in
    // The app for security reasons obviously will not give us the instant
    // access to its core functionality

    void login(FxRobot robot) {
        robot.clickOn("#fieldUsername").write(login);

        // The security is especially valuable, so strong password is required
        robot.clickOn("#fieldPassword").write("adminadmin1");

        // This button could be omitted if you used the app before and made it
        // "Remember you", as I did before without thinking further about this test
        // So I am still doing the terms checking to avoid failure
        robot.clickOn("#checkTerms");

        robot.clickOn("#btnSignin");
    }



    // Testing the fact that user indeed can touch the buttons
    // Type to the fields etc. Could be a trivial thing
    // until you realize that button can not do the function
    // it does or the field could clear up just by random mistake
    // Also, the name could be wrongly written (like weird ASCII
    // symbols instead of normal output), so there is a primary test for it

    @Test
    void testCredentialsCorrectness(FxRobot robot) throws Exception {
        login(robot);

        // I have noticed, the app simply is a bit longer to load
        // As an instant robot redirect, so even 500 secs did not work
        // But 800+ ms is great deal here. I am afraid when we will use
        // The real cloud environment, the tester would simply fail
        // So additional 200 ms are a great step towards avoiding this situation
        robot.sleep(1000);

        // Checking if the name is indeed what we need
        verifyThat("#navi_username_text", (Label t) -> t.getText().equals(login));
    }


    // The second test is trivial
    // When the user types complete nonsense or just the
    // subject does not exist yet, we should clearly state this fact

    @Test
    void testSubjectsEmpty(FxRobot robot) throws Exception {
        login(robot);

        robot.sleep(1100);

        robot.clickOn("#btn_teacherspage");

        robot.sleep(860);

        // Sadly, not found in the FIIT database
        robot.clickOn("#searchField").write("doc. Ing. Randomname");

        // Because the program architecture does not contain a direct textbox
        // Only spawns it at some point
        // This test made barely for vision purposes
        // No panic message would be returned there, but the user can see if
        // there is no text "No teachers matching your criteria"
    }



    // This test made for language change test
    // Most website do not translate 100% of text
    // We are doing the same as well, as there is no
    // Real need to translate trivial words, which users
    // Have learned from daily device usage. At the same time
    // There is a strong need to make core elements translated correctly
    // The app is in English by default, so this test changes it
    // to the Slovak language and verifies it works
    // by checking the update of the name of the random field
    // In that case we used the menu variant

    @Test
    void testLanguageAlteration(FxRobot robot) throws Exception {
        login(robot);

        robot.sleep(1100);

        robot.clickOn("#btn_teacherspage");

        robot.sleep(860);

        robot.clickOn("#languageComboBox");
        robot.clickOn("Slovenský");

        // It is a local thing, so should be very quick
        // Doing a strict timeline
        robot.sleep(200);

        verifyThat("#btn_teacherspage", (MFXButton b) -> b.getText().equals("Učitelia"));
    }



    // This test should prove that the logout
    // Indeed will work and not only the app state inside
    // the code will prove it, but also the clear visual
    // representation of the event
    // Sometimes such things indeed happening in production
    // So this test could be launched everytime the UI functionality
    // depending on the logout, would be altered someway

    @Test
    void testLogout(FxRobot robot) throws Exception {
        login(robot);

        robot.sleep(1000);

        robot.clickOn("#logoutbtn");

        robot.sleep(860);

        // Basically there we are verifying there IS the field
        // Otherwise the user still on the same page in an unacceptable
        // response time and we are failing
        verifyThat("#fieldUsername", (TextField tf) -> tf.getText().isEmpty());
    }



    // The user should also be notified if he cannot log in
    // The message should be in any form, but we should see it clearly
    // on the screen
    // There we are checking it

    @Test
    void testNegativeLogin(FxRobot robot) throws Exception {
        robot.clickOn("#fieldUsername").write("randomnamethatanotexists1311");

        // The security is especially valuable, so strong password is required
        robot.clickOn("#fieldPassword").write("");

        // This button could be omitted if you used the app before and made it
        // "Remember you", as I did before without thinking further about this test
        // So I am still doing the terms checking to avoid failure
        robot.clickOn("#checkTerms");

        robot.clickOn("#btnSignin");

        // The feedback should be more instant than when we succeed
        // As the wrong credentials do not generate additional gui
        // except for one label, which should be thrown in less than 200 ms
        // for my opinion for an outstanding usability
        robot.sleep(500);

        // Usually the info message is empty, which means you will not see it
        // In the normal conditions, however there are situations where the
        // error should occur, such as this one, and then the label becomes
        // non-empty. We need to ensure the label indeed reacts to the bad data
        verifyThat("#infoMess", (Label t) -> !t.getText().isEmpty());
    }

}