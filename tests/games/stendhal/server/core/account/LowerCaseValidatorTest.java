package games.stendhal.server.core.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import marauroa.common.game.Result;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LowerCaseValidatorTest {

	@Before
	public void setUp() throws Exception {
		Locale.setDefault(Locale.ENGLISH);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testLowerCaseValidator() {
		LowerCaseValidator lcv = new LowerCaseValidator("A");
		assertEquals(Result.FAILED_INVALID_CHARACTER_USED, lcv.validate());
		lcv = new LowerCaseValidator("a");
		assertNull(lcv.validate());

		// Note: To successfully pass the following tests, Eclipse's file encoding
		// must be set to "UTF-8". 
		// german Umlaut "Ue"
		lcv = new LowerCaseValidator("Ü");	
		assertEquals(Result.FAILED_INVALID_CHARACTER_USED, lcv.validate());
		// german Umlaut "ue"
		lcv = new LowerCaseValidator("ü");	
		assertNull(lcv.validate());
	}

}
