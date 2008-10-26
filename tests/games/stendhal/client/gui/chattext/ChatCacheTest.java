package games.stendhal.client.gui.chattext;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import games.stendhal.client.gui.chattext.ChatCache;

import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChatCacheTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testChatCache() {
		new ChatCache(null);
	}

	@Test
	public void testGetLines() {
		ChatCache cache = new ChatCache(null);
		assertTrue(cache.getLines().isEmpty());
		cache.addlinetoCache("one");
		assertFalse(cache.getLines().isEmpty());
	}

	
	@Test
	public void testGetAndSetCurrent() {
		ChatCache cache = new ChatCache(null);
		cache.setCurrent(0);
		assertThat(cache.getCurrent(), is(0));
		cache.setCurrent(10);
		assertThat(cache.getCurrent(), is(10));

	}

	@Test
	public void testAddlinetoCache() {
		ChatCache cache = new ChatCache(null);

		cache.addlinetoCache("one");
		assertThat(cache.current(), is("one"));
		cache.addlinetoCache("two");
		assertThat(cache.current(), is("two"));
	}

	@Test
	public void testNextAndPrevious() throws Exception {
		ChatCache cache = new ChatCache(null);
		assertFalse(cache.hasNext());
		cache.addlinetoCache("one");
		assertFalse(cache.hasNext());
		assertFalse(cache.hasPrevious());
		cache.addlinetoCache("two");
		assertFalse(cache.hasNext());
		assertTrue(cache.hasPrevious());

		assertThat(cache.previous(), is("one"));
		assertThat(cache.current(), is("one"));

		assertThat(cache.next(), is("two"));
		assertThat(cache.current(), is("two"));

	}

	@Test
	public void testNextOnEmptyCache() throws Exception {

		ChatCache cache = new ChatCache(null);
		cache.addlinetoCache("one");
		assertFalse(cache.hasNext());
		try {
			cache.next();
		} catch (NoSuchElementException e) {
			assertThat(cache.current(), is("one"));

		}
	}

	public void testPreviousOnEmptyCache() throws Exception {

		ChatCache cache = new ChatCache(null);
		assertFalse(cache.hasPrevious());
		cache.addlinetoCache("one");
		try {
			cache.previous();
		} catch (NoSuchElementException e) {
			assertThat(cache.current(), is("one"));

		}
	}
}
