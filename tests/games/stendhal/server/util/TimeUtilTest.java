package games.stendhal.server.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for TimeUtil.
 */
public class TimeUtilTest {

	@Test
	public void testTimeUtil() {
		Assert.assertEquals("5 seconds", TimeUtil.timeUntil(5, true));
		Assert.assertEquals("7 minutes, 1 seconds", TimeUtil.timeUntil(421, true));
		Assert.assertEquals("22 hours, 59 minutes, 49 seconds", TimeUtil.timeUntil(82789, true));
		Assert.assertEquals("11 weeks, 20 hours, 58 minutes, 2 seconds", TimeUtil.timeUntil(6728282, true));
		Assert.assertEquals("138 weeks, 3 days, 4 hours, 6 minutes, 12 seconds", TimeUtil.timeUntil(83736372, true));
	}

	@Test
	public void testApproxTimeUntil() {
		Assert.assertEquals("less than a minute", TimeUtil.approxTimeUntil(5));
		Assert.assertEquals("7 minutes", TimeUtil.approxTimeUntil(421));
		Assert.assertEquals("23 hours", TimeUtil.approxTimeUntil(82789));
		Assert.assertEquals("just over 11 weeks", TimeUtil.approxTimeUntil(6728282));
		Assert.assertEquals("about 138 and a half weeks", TimeUtil.approxTimeUntil(83736372));
	}

}
