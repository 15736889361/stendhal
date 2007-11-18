/*
 * @(#) src/games/stendhal/client/sprite/AnimatedSprite.java
 *
 * $Id$
 */

package games.stendhal.client.sprite;

//
//

import java.awt.Graphics;
import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * This is a sprite that transparently animates itself when drawn.
 */
public class AnimatedSprite implements Sprite {
	private static Logger logger = Logger.getLogger(AnimatedSprite.class);

	/**
	 * The identifier reference.
	 */
	protected Object reference;

	/**
	 * Whether the sprite is currently animating.
	 */
	protected boolean animating;

	/**
	 * The amount of time passed in the cycle.
	 */
	protected int cycleTime;

	/**
	 * The [minimum] frame durations.
	 */
	protected int[] delays;

	/**
	 * The total duration of a cycle.
	 */
	protected int duration;

	/**
	 * The current frame index.
	 */
	protected int index;

	/**
	 * The frame sprites.
	 */
	protected Sprite[] frames;

	/**
	 * The sprite height.
	 */
	protected int height;

	/**
	 * The time of the last update.
	 */
	protected long lastUpdate;

	/**
	 * Whether to loop after last frame.
	 */
	protected boolean loop;

	/**
	 * The current sprite.
	 */
	protected Sprite sprite;

	/**
	 * The sprite width.
	 */
	protected int width;

	/**
	 * Create an animated sprite from a set of frame sprites.
	 *
	 * <strong>NOTE: The array of frames passed is not copied, and must not be
	 * modified while this instance exists (unless you are sure you know what
	 * you are doing).</strong>
	 *
	 * @param frames
	 *            The frames to animate.
	 * @param delay
	 *            The minimum delay between frames (in ms).
	 *
	 * @throws IllegalArgumentException
	 *             If less than one frame is given, or the delay is < 0.
	 */
	public AnimatedSprite(final Sprite[] frames, final int delay) {
		this(frames, delay, true);
	}

	/**
	 * Create an animated sprite from a set of frame sprites.
	 *
	 * <strong>NOTE: The array of frames passed is not copied, and must not be
	 * modified while this instance exists (unless you are sure you know what
	 * you are doing).</strong>
	 *
	 * @param frames
	 *            The frames to animate.
	 * @param delay
	 *            The minimum delay between frames (in ms).
	 * @param animating
	 *            If animation is enabled.
	 *
	 * @throws IllegalArgumentException
	 *             If less than one frame is given, or the delay is < 0.
	 */
	public AnimatedSprite(final Sprite[] frames, final int delay,
			final boolean animating) {
		this(frames, delay, true, null);
	}

	/**
	 * Create an animated sprite from a set of frame sprites.
	 *
	 * <strong>NOTE: The array of frames passed is not copied, and must not be
	 * modified while this instance exists (unless you are sure you know what
	 * you are doing).</strong>
	 *
	 * @param frames
	 *            The frames to animate.
	 * @param delay
	 *            The minimum delay between frames (in ms).
	 * @param animating
	 *            If animation is enabled.
	 * @param reference
	 *            The sprite identifier reference.
	 *
	 * @throws IllegalArgumentException
	 *             If less than one frame is given, or the delay is < 0.
	 */
	public AnimatedSprite(final Sprite[] frames, final int delay,
			final boolean animating, final Object reference) {
		this(frames, createDelays(delay, frames.length), animating, reference);
	}

	/**
	 * Create an animated sprite from a set of frame sprites.
	 *
	 * <strong>NOTE: The array of frames/delays passed is not copied, and must
	 * not be modified while this instance exists (unless you are sure you know
	 * what you are doing).</strong>
	 *
	 * @param frames
	 *            The frames to animate.
	 * @param delays
	 *            The minimum duration for each frame (in ms).
	 * @param animating
	 *            If animation is enabled.
	 * @param reference
	 *            The sprite identifier reference.
	 *
	 * @throws IllegalArgumentException
	 *             If less than one frame is given, or the delay is < 0.
	 */
	public AnimatedSprite(final Sprite[] frames, final int[] delays, final boolean animating, final Object reference)
			throws IllegalArgumentException {
		if (frames.length == 0) {
			logger.warn("AnimatedSprite needs at least one frame");
		}

		if (delays.length != frames.length) {
			throw new IllegalArgumentException(
					"Mismatch between number of frame sprites and delays");
		}

		/*
		 * Validate delay values. Calculate total cycle duration.
		 */
		duration = 0;

		for (int i = 0; i < delays.length; i++) {
			if (delays[i] < 0) {
				throw new IllegalArgumentException("Delay < 0");
			}

			duration += delays[i];
		}

		this.frames = frames;
		this.delays = delays;
		this.animating = animating;
		this.reference = reference;

		loop = true;

		height = 0;
		width = 0;

		for (Sprite frame : frames) {
			height = Math.max(height, frame.getHeight());
			width = Math.max(width, frame.getWidth());
		}

		index = 0;
		sprite = frames.length>0? frames[0]: null;

		cycleTime = 0;
		lastUpdate = System.currentTimeMillis();
	}

	//
	// AnimatedSprite
	//

	/**
	 * Utility method to convert a single delay to an array of delays having
	 * that value.
	 *
	 * @param delay
	 *            The delay value.
	 * @param count
	 *            The size of the array to create.
	 *
	 * @return An array of delays.
	 */
	protected static int[] createDelays(final int delay, final int count) {
		int[] delays = new int[count];
		Arrays.fill(delays, delay);

		return delays;
	}

	/**
	 * Get the minimum delays for each frame.
	 *
	 * <strong>NOTE: The array of delays returned is not copied, and must not be
	 * modified.</strong>
	 *
	 * @return The minimum delays for each frame (in ms).
	 */
	public int[] getDelays() {
		return delays;
	}

	/**
	 * Get the total duration of an animation cycle.
	 *
	 * @return The total duration (in ms).
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Get the frames that make up this animation.
	 *
	 * <strong>NOTE: The array of frames returned is not copied, and must not be
	 * modified.</strong>
	 *
	 * @return The frames.
	 */
	public Sprite[] getFrames() {
		return frames;
	}

	/**
	 * Get the current frame index.
	 *
	 * @return The current frame index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Determine if the sprite is currently animated, or paused.
	 *
	 * @return <code>true</code> if animating.
	 *
	 * @see-also #start()
	 * @see-also #stop()
	 * @see-also #setAnimating(boolean)
	 */
	public boolean isAnimating() {
		return animating;
	}

	/**
	 * Determine if the animation loops.
	 *
	 * @return <code>true</code> if animation loops.
	 *
	 * @see-also #setLoop(boolean)
	 */
	public boolean isLoop() {
		return loop;
	}

	/**
	 * Reset the animation back to it's initial frame, and reset the next frame
	 * time.
	 */
	public void reset() {
		setIndex(0);
	}

	/**
	 * Set the sprite animating state.
	 *
	 * @param animating
	 *            <code>true</code> if animating.
	 */
	public void setAnimating(final boolean animating) {
		this.animating = animating;
	}

	/**
	 * Set the frame index to a specific value.
	 *
	 * @param index
	 *            The index to use.
	 *
	 * @throws ArrayIndexOutOfBoundsException
	 *             If the index is less than 0 or greater than or equal to the
	 *             number of frames.
	 */
	public void setIndex(final int index) {
		if ((index < 0) || (index >= frames.length)) {
			throw new ArrayIndexOutOfBoundsException("Invalid index: " + index);
		}

		this.index = index;
		sprite = frames[index];

		/*
		 * Calculate the time into this frame
		 */
		cycleTime = 0;

		for (int i = 0; i < index; i++) {
			cycleTime += delays[i];
		}

		lastUpdate = System.currentTimeMillis();
	}

	/**
	 * Set the animation loop state.
	 *
	 * @param loop
	 *            <code>true</code> if animation loops.
	 */
	public void setLoop(final boolean loop) {
		this.loop = loop;
	}

	/**
	 * Start the sprite animating.
	 *
	 * @see-also #stop()
	 */
	public void start() {
		animating = true;
	}

	/**
	 * Stop the sprite animating. This does not change the current frame.
	 *
	 * @see-also #start()
	 */
	public void stop() {
		animating = false;
	}

	/**
	 * Update the current frame sprite.
	 */
	protected void update() {
		long now = System.currentTimeMillis();
		update((int) (now - lastUpdate));
		lastUpdate = now;
	}

	//
	// Sprite
	//

	/**
	 * Copy the sprite. This does not do a deep copy, so the frames it is made
	 * of are shared.
	 *
	 * @return A new copy of the sprite.
	 */
	public Sprite copy() {
		AnimatedSprite spriteCopy = new AnimatedSprite(getFrames(), getDelays(),
				isAnimating(), getReference());

		spriteCopy.setLoop(isLoop());
		spriteCopy.setIndex(getIndex());

		return spriteCopy;
	}

	/**
	 * Create a sub-region of this sprite. <strong>NOTE: This does not use
	 * caching.</strong>
	 *
	 * @param x
	 *            The starting X coordinate.
	 * @param y
	 *            The starting Y coordinate.
	 * @param width
	 *            The region width.
	 * @param height
	 *            The region height.
	 * @param ref
	 *            The sprite reference.
	 *
	 * @return A new sprite.
	 */
	public Sprite createRegion(final int x, final int y, final int width,
			final int height, final Object ref) {
		return new TileSprite(this, x, y, width, height, ref);
	}

	/**
	 * Draw the sprite onto the graphics context provided.
	 *
	 * @param g
	 *            The graphics context on which to draw the sprite
	 * @param x
	 *            The x location at which to draw the sprite
	 * @param y
	 *            The y location at which to draw the sprite
	 */
	public void draw(final Graphics g, final int x, final int y) {
		update();

		if (sprite != null) {
			sprite.draw(g, x, y);
		}
	}

	/**
	 * Draws the image
	 *
	 * @param g
	 *            the graphics context where to draw to
	 * @param destx
	 *            destination x
	 * @param desty
	 *            destination y
	 * @param x
	 *            the source x
	 * @param y
	 *            the source y
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 */
	public void draw(final Graphics g, final int destx, final int desty,
			final int x, final int y, final int w, final int h) {
		update();

		if (sprite != null) {
			sprite.draw(g, destx, desty, x, y, w, h);
		}
	}

	/**
	 * Get the height of the drawn sprite.
	 *
	 * @return The height in pixels of this sprite.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Get the sprite reference. This identifier is an externally opaque object
	 * that implements equals() and hashCode() to uniquely/repeatably reference
	 * a keyed sprite.
	 *
	 * @return The reference identifier, or <code>null</code> if not
	 *         referencable.
	 */
	public Object getReference() {
		return reference;
	}

	/**
	 * Get the width of the drawn sprite.
	 *
	 * @return The width in pixels of this sprite.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Update the current frame sprite.
	 * <em>Idealy this would be called from a central time manager,
	 * instead of draw() like now.</em>
	 *
	 * @param delta
	 *            The time since last update (in ms).
	 */
	public void update(final int delta) {
		cycleTime += delta;
		cycleTime %= duration;

		if (animating) {
			while (cycleTime >= delays[index]) {
				cycleTime -= delays[index];

				if (++index == frames.length) {
					index = 0;

					if (!loop) {
						sprite = null;
						animating = false;
						return;
					}
				}
			}

			sprite = frames[index];
		}
	}
}
