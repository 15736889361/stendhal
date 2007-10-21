/*
 * CreateAccountDialogTest.java
 * 
 * Created on Oct 21, 2007, 2:08:15 PM
 */

package games.stendhal.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the CreateAccountDialog
 * @author timothyb89
 */
public class CreateAccountDialogTest {
    
    @Test
    public void testValidatePass() {
        CreateAccountDialog cad = new CreateAccountDialog();
        assertFalse(cad.validatePassword(new String(), new String()));
        
        String user1 = "qwerty";
        String pass1 = "qwerty";
        assertFalse(cad.validatePassword(user1, pass1));
        
        String user2 = "qwertyuiop";
        String pass2 = "qwerty";
        assertFalse(cad.validatePassword(user2, pass2));
        
        String pass3 = "qwertyu";
        assertFalse(cad.validatePassword(user2, pass3));
        
        String pass4 = "tyuiop";
        assertFalse(cad.validatePassword(user2, pass4));
        
        String pass5 = "rtyuiop";
        assertFalse(cad.validatePassword(user2, pass5));
    }

}
