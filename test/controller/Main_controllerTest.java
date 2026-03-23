package controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import gui.Gui;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import java.lang.reflect.Field;

public class Main_controllerTest {
    @Test //Tests message_sent_from_user
    void testMessageSentFromUserCorrectly() {

        String expectedMessage = "Welcome to Main Controller!";

        Main_controller.message_sent_from_user(expectedMessage);
        String actualMessage = Main_controller.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test //Tests getMessage() returning Null when it is Null
    void testGetMessageReturnsNullWhenEmpty() {

        Main_controller.getMessage();

        String result = Main_controller.getMessage();

        //If message doesnt exist return Null
        assertNull(result);
    }

    @Test // getMessage() to return a string
    void testGetMessageReturnsStoredMessage() {

        Main_controller.message_sent_from_user("Hello");

        String result = Main_controller.getMessage();

        //See if the message sent is correct
        assertEquals("Hello", result);
    }

    @Test// getMessage() to clear the string after sending the message for the next message
    void testGetMessageClearsAfterReading() {
        Main_controller.message_sent_from_user("Test");

        String first = Main_controller.getMessage();
        String second = Main_controller.getMessage();

        // combine
        assertEquals("Test", first);
        assertNull(second);
    }

    //Helping methods to access private methods in the gui
    private Object getField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void testUpdateUserListParsesCoordinatorAndUsersCorrectly() throws Exception {
        //Initiate
        Gui gui = new Gui();

        DefaultListModel<String> model = new DefaultListModel<>();
        setField(gui, "onlineUsersModel", model);

        String data = "Amin|Amin:127.0.0.1:5000,Hugo:127.0.0.1:5001";

        Main_controller.update_user_list(data);

        // Wait for Swing update if needed
        SwingUtilities.invokeAndWait(() -> {});


        assertEquals("Amin", gui.coordinatorUsername);
        assertEquals(2, model.size());
        assertEquals("Amin", model.getElementAt(0));
        assertEquals("Hugo", model.getElementAt(1));
    }

}
