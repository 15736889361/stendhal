/*
 * @(#) games/stendhal/client/gui/j2d/entity/RPEntity2DView.java
 *
 * $Id$
 */

package games.stendhal.client.gui.j2d.entity;

//
//

import games.stendhal.client.IGameScreen;
import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.sprite.AnimatedSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.Direction;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import marauroa.common.game.RPAction;

/**
 * The 2D view of an RP entity.
 */
public abstract class RPEntity2DView extends ActiveEntity2DView {
	private static final int ICON_OFFSET = 8;

	private static Map<Object, Sprite[]> bladeStrikeSprites;

	private static Sprite eatingSprite;

	private static Sprite poisonedSprite;

	private static Sprite hitSprite;

	private static Sprite blockedSprite;

	private static Sprite missedSprite;

	/**
	 * The RP entity this view is for.
	 */
	protected RPEntity rpentity;

	/**
	 * Blade strike frame.
	 */
	private int frameBladeStrike;

	/**
	 * Model attributes effecting the title changed.
	 */
	private boolean titleChanged;

	/**
	 * The title image sprite.
	 */
	private Sprite titleSprite;

	/*
	 * The drawn height.
	 */
	protected int height;

	/*
	 * The drawn width.
	 */
	protected int width;

	static {
		SpriteStore st = SpriteStore.get();

		Sprite tiles = st.getSprite("data/sprites/combat/blade_strike.png");

		bladeStrikeSprites = new HashMap<Object, Sprite[]>();

		int twidth = 3 * IGameScreen.SIZE_UNIT_PIXELS;
		int theight = 4 * IGameScreen.SIZE_UNIT_PIXELS;

		int y = 0;
		bladeStrikeSprites.put(Direction.UP, st.getTiles(tiles, 0, y, 3, twidth,
				theight));

		y += theight;
		bladeStrikeSprites.put(Direction.RIGHT, st.getTiles(tiles, 0, y, 3, twidth,
				theight));

		y += theight;
		bladeStrikeSprites.put(Direction.DOWN, st.getTiles(tiles, 0, y, 3, twidth,
				theight));

		y += theight;
		bladeStrikeSprites.put(Direction.LEFT, st.getTiles(tiles, 0, y, 3, twidth,
				theight));

		hitSprite = st.getSprite("data/sprites/combat/hitted.png");
		blockedSprite = st.getSprite("data/sprites/combat/blocked.png");
		missedSprite = st.getSprite("data/sprites/combat/missed.png");
		eatingSprite = st.getSprite("data/sprites/ideas/eat.png");
		poisonedSprite = st.getSprite("data/sprites/ideas/poisoned.png");
	}

	/**
	 * Create a 2D view of an entity.
	 *
	 * @param rpentity
	 *            The entity to render.
	 */
	public RPEntity2DView(final RPEntity rpentity) {
		super(rpentity);

		this.rpentity = rpentity;

		titleSprite = createTitleSprite();
		titleChanged = false;
	}

	//
	// RPEntity2DView
	//

	/**
	 * Populate keyed state sprites.
	 *
	 * @param map
	 *            The map to populate.
	 * @param tiles
	 *            The master sprite.
	 * @param width
	 *            The tile width (in pixels).
	 * @param height
	 *            The tile height (in pixels).
	 */
	protected void buildSprites(final Map<Object, Sprite> map,
			final Sprite tiles, final int width, final int height) {
		int y = 0;
		map.put(Direction.UP, createWalkSprite(tiles, y, width, height));

		y += height;
		map.put(Direction.RIGHT, createWalkSprite(tiles, y, width, height));

		y += height;
		map.put(Direction.DOWN, createWalkSprite(tiles, y, width, height));

		y += height;
		map.put(Direction.LEFT, createWalkSprite(tiles, y, width, height));
	}

	/**
	 * Calculate sprite image offset. Sub-classes may override this to change
	 * alignment.
	 *
	 * @param spriteWidth
	 *            The sprite width (in pixels).
	 * @param spriteHeight
	 *            The sprite height (in pixels).
	 * @param entityWidth
	 *            The entity width (in pixels).
	 * @param entityHeight
	 *            The entity height (in pixels).
	 */
	@Override
	protected void calculateOffset(final int spriteWidth, final int spriteHeight,
			final int entityWidth, final int entityHeight) {
		/*
		 * X alignment centered, Y alignment bottom
		 */
		xoffset = (entityWidth - spriteWidth) / 2;
		yoffset = entityHeight - spriteHeight;
	}

	/**
	 * Create the title sprite.
	 *
	 * @return The title sprite.
	 */
	protected Sprite createTitleSprite() {
		String titleType = rpentity.getTitleType();
		int adminlevel = rpentity.getAdminLevel();
		Color nameColor = null;

		if (titleType != null) {
			if (titleType.equals("npc")) {
				nameColor = new Color(200, 200, 255);
			} else if (titleType.equals("enemy")) {
				nameColor = new Color(255, 200, 200);
			}
		}

		if (nameColor == null) {
			if (adminlevel >= 800) {
				nameColor = new Color(200, 200, 0);
			} else if (adminlevel >= 400) {
				nameColor = new Color(255, 255, 0);
			} else if (adminlevel > 0) {
				nameColor = new Color(255, 255, 172);
			} else {
				nameColor = Color.white;
			}
		}

		return screen.createString(entity.getTitle(), nameColor);
	}

	/**
	 * Extract a walking animation for a specific row. The source sprite
	 * contains 3 animation tiles, but this is converted to 4 frames.
	 *
	 * @param tiles
	 *            The tile image.
	 * @param y
	 *            The base Y coordinate.
	 * @param width
	 *            The frame width.
	 * @param height
	 *            The frame height.
	 *
	 * @return A sprite.
	 */
	protected Sprite createWalkSprite(final Sprite tiles, final int y,
			final int width, final int height) {
		SpriteStore store = SpriteStore.get();

		Sprite[] frames = new Sprite[4];

		int x = 0;
		frames[0] = store.getTile(tiles, x, y, width, height);

		x += width;
		frames[1] = store.getTile(tiles, x, y, width, height);

		x += width;
		frames[2] = store.getTile(tiles, x, y, width, height);

		frames[3] = frames[1];

		return new AnimatedSprite(frames, 100, false);
	}

	/**
	 * Draw the floating text indicators (floaters).
	 *
	 * @param g2d
	 *            The graphics context.
	 * @param x
	 *            The drawn X coordinate.
	 * @param y
	 *            The drawn Y coordinate.
	 * @param width
	 *            The drawn width.
	 */
	protected void drawFloaters(final Graphics2D g2d, final int x, final int y,
			final int width) {
		FontMetrics fm = g2d.getFontMetrics();

		Iterator<RPEntity.TextIndicator> iter = rpentity.getTextIndicators();

		while (iter.hasNext()) {
			RPEntity.TextIndicator indicator = iter.next();

			int age = indicator.getAge();
			String text = indicator.getText();

			int stringwidth = fm.stringWidth(text) + 2;

			int tx = x + ((width - stringwidth) / 2);
			int ty = y - (int) (age * 5L / 300L);

			Color color = indicator.getType().getColor();

			screen.drawOutlineString(g2d, color, text, tx, ty + 10);
		}
	}

	/**
	 * Draw the entity HP bar.
	 *
	 * @param g2d
	 *            The graphics context.
	 * @param x
	 *            The drawn X coordinate.
	 * @param y
	 *            The drawn Y coordinate.
	 * @param width
	 *            The drawn width.
	 */
	protected void drawHPbar(final Graphics2D g2d, final int x, final int y,
			final int width) {
		int barWidth = Math.max(width * 2 / 3, IGameScreen.SIZE_UNIT_PIXELS);

		int bx = x + ((width - barWidth) / 2);
		int by = y - 3;

		float hpRatio = rpentity.getHPRatio();

		float r = Math.min((1.0f - hpRatio) * 2.0f, 1.0f);
		float g = Math.min(hpRatio * 2.0f, 1.0f);

		g2d.setColor(Color.gray);
		g2d.fillRect(bx, by, barWidth, 3);

		g2d.setColor(new Color(r, g, 0.0f));
		g2d.fillRect(bx, by, (int) (hpRatio * barWidth), 3);

		g2d.setColor(Color.black);
		g2d.drawRect(bx, by, barWidth, 3);
	}

	/**
	 * Draw the entity status bar. The status bar show the title and HP bar.
	 *
	 * @param g2d
	 *            The graphics context.
	 * @param x
	 *            The drawn X coordinate.
	 * @param y
	 *            The drawn Y coordinate.
	 * @param width
	 *            The drawn width.
	 */
	protected void drawStatusBar(final Graphics2D g2d, final int x,
			final int y, final int width) {
		drawTitle(g2d, x, y, width);
		drawHPbar(g2d, x, y, width);
	}

	/**
	 * Draw the entity title.
	 *
	 * @param g2d
	 *            The graphics context.
	 * @param x
	 *            The drawn X coordinate.
	 * @param y
	 *            The drawn Y coordinate.
	 * @param width
	 *            The drawn width.
	 */
	protected void drawTitle(final Graphics2D g2d, int x, int y, final int width) {
		if (titleSprite != null) {
			int tx = x + ((width - titleSprite.getWidth()) / 2);
			int ty = y - 3 - titleSprite.getHeight();

			titleSprite.draw(g2d, tx, ty);
		}
	}

	/**
	 * Get the full directional animation tile set for this entity.
	 *
	 * @return A tile sprite containing all animation images.
	 */
	protected abstract Sprite getAnimationSprite();

	/**
	 * Get the number of tiles in the X axis of the base sprite.
	 *
	 * @return The number of tiles.
	 */
	protected int getTilesX() {
		return 3;
	}

	/**
	 * Get the number of tiles in the Y axis of the base sprite.
	 *
	 * @return The number of tiles.
	 */
	protected int getTilesY() {
		return 4;
	}

	/**
	 * Determine is the user can see this entity while in ghostmode.
	 *
	 * @return <code>true</code> if the client user can see this entity while
	 *         in ghostmode.
	 */
	protected boolean isVisibleGhost() {
		return false;
	}

	//
	// StateEntity2DView
	//

	/**
	 * Populate keyed state sprites.
	 *
	 * @param map
	 *            The map to populate.
	 */
	@Override
	protected void buildSprites(final Map<Object, Sprite> map) {
		Sprite tiles = getAnimationSprite();

		width = tiles.getWidth() / getTilesX();
		height = tiles.getHeight() / getTilesY();

		buildSprites(map, tiles, width, height);
		calculateOffset(width, height);
	}

	//
	// Entity2DView
	//

	/**
	 * Build a list of entity specific actions. <strong>NOTE: The first entry
	 * should be the default.</strong>
	 *
	 * @param list
	 *            The list to populate.
	 */
	@Override
	protected void buildActions(final List<String> list) {
		super.buildActions(list);

		if (rpentity.isAttackedBy(User.get())) {
			list.add(ActionType.STOP_ATTACK.getRepresentation());
		} else {
			list.add(ActionType.ATTACK.getRepresentation());
		}

		list.add(ActionType.PUSH.getRepresentation());
	}

	/**
	 * Draw the entity.
	 *
	 * @param g2d
	 *            The graphics context.
	 * @param x
	 *            The drawn X coordinate.
	 * @param y
	 *            The drawn Y coordinate.
	 * @param width
	 *            The drawn entity width.
	 * @param height
	 *            The drawn entity height.
	 */
	@Override
	protected void draw(final Graphics2D g2d, final int x, final int y,
			final int width, final int height) {
		Rectangle srect = screen.convertWorldToScreenView(entity.getArea());

		if (rpentity.isBeingAttacked()) {
			// Draw red box around

			g2d.setColor(Color.red);
			g2d.drawRect(srect.x, srect.y, srect.width, srect.height);

			g2d.setColor(Color.black);
			g2d.drawRect(srect.x - 1, srect.y - 1, srect.width + 2,
					srect.height + 2);
		}

		if (rpentity.isAttacking(User.get())) {
			// Draw orange box around
			g2d.setColor(Color.orange);
			g2d.drawRect(srect.x + 1, srect.y + 1, srect.width - 2,
					srect.height - 2);
		}

		if (rpentity.isAttacking() && rpentity.isBeingStruck()) {
			if (frameBladeStrike < 3) {
				Sprite sprite = bladeStrikeSprites.get(getState())[frameBladeStrike];

				int spriteWidth = sprite.getWidth();
				int spriteHeight = sprite.getHeight();

				int sx;
				int sy;

				/*
				 * Align swipe image to be 16 px past the facing edge, centering
				 * in other axis.
				 *
				 * Swipe image is 3x4 tiles, but really only uses partial areas.
				 * Adjust positions to match (or fix images to be
				 * uniform/centered).
				 */
				switch (rpentity.getDirection()) {
				case UP:
					sx = x + ((width - spriteWidth) / 2) + 16;
					sy = y - 16 - 32;
					break;

				case DOWN:
					sx = x + ((width - spriteWidth) / 2);
					sy = y + height - spriteHeight + 16;
					break;

				case LEFT:
					sx = x - 16;
					sy = y + ((height - spriteHeight) / 2) - 16;
					break;

				case RIGHT:
					sx = x + width - spriteWidth + 16;
					sy = y + ((height - spriteHeight) / 2) - ICON_OFFSET;
					break;

				default:
					sx = x + ((width - spriteWidth) / 2);
					sy = y + ((height - spriteHeight) / 2);
				}

				sprite.draw(g2d, sx, sy);
			} else {
				rpentity.doneStriking();
				frameBladeStrike = 0;
			}

			frameBladeStrike++;
		}

		super.draw(g2d, x, y, width, height);

		if (rpentity.isEating()) {
			eatingSprite.draw(g2d, x + ICON_OFFSET, y + height - ICON_OFFSET);
		}

		if (rpentity.isPoisoned()) {
			poisonedSprite.draw(g2d, x - ICON_OFFSET, y + height - ICON_OFFSET);
		}

		if (rpentity.isDefending()) {
			// Draw bottom right combat icon
			int sx = x + width - ICON_OFFSET;
			int sy = y + height - ICON_OFFSET;

			switch (rpentity.getResolution()) {
			case BLOCKED:
				blockedSprite.draw(g2d, sx, sy);
				break;

			case MISSED:
				missedSprite.draw(g2d, sx, sy);
				break;

			case HIT:
				hitSprite.draw(g2d, sx, sy);
				break;
			default: 
				//cannot happen we are switching on enum
			}
		}

		// Enable this to debug entity view area
		if (false) {
			g2d.setColor(Color.cyan);
			g2d.drawRect(x, y, width, height);
		}

		drawFloaters(g2d, x, y, width);
	}

	/**
	 * Draw the top layer parts of an entity. This will be on down after all
	 * other game layers are rendered.
	 *
	 * @param g2d
	 *            The graphics context.
	 * @param x
	 *            The drawn X coordinate.
	 * @param y
	 *            The drawn Y coordinate.
	 * @param width
	 *            The drawn entity width.
	 * @param height
	 *            The drawn entity height.
	 */
	@Override
	protected void drawTop(final Graphics2D g2d, final int x, final int y,
			final int width, final int height) {
		drawStatusBar(g2d, x, y, width);
	}

	/**
	 * Get the height.
	 *
	 * @return The height (in pixels).
	 */
	@Override
	public int getHeight() {
		return height;
	}

	/**
	 * Get the entity's visibility.
	 *
	 * @return The visibility value (0-100).
	 */
	@Override
	protected int getVisibility() {
		/*
		 * Hide while in ghostmode.
		 */
		if (rpentity.isGhostMode()) {
			if (isVisibleGhost()) {
				return super.getVisibility() / 2;
			} else {
				return 0;
			}
		} else {
			return super.getVisibility();
		}
	}

	/**
	 * Get the width.
	 *
	 * @return The width (in pixels).
	 */
	@Override
	public int getWidth() {
		return width;
	}

	/**
	 * Determines on top of which other entities this entity should be drawn.
	 * Entities with a high Z index will be drawn on top of ones with a lower Z
	 * index.
	 *
	 * Also, players can only interact with the topmost entity.
	 *
	 * @return The drawing index.
	 */
	@Override
	public int getZIndex() {
		return 8000;
	}

	/**
	 * Handle updates.
	 */
	@Override
	protected void update() {
		super.update();

		if (titleChanged) {
			titleSprite = createTitleSprite();
			titleChanged = false;
		}
	}

	//
	// EntityChangeListener
	//

	/**
	 * An entity was changed.
	 *
	 * @param entity
	 *            The entity that was changed.
	 * @param property
	 *            The property identifier.
	 */
	@Override
	public void entityChanged(final Entity entity, final Object property) {
		super.entityChanged(entity, property);

		if (property == RPEntity.PROP_ADMIN_LEVEL) {
			titleChanged = true;
			visibilityChanged = true;
		} else if (property == RPEntity.PROP_GHOSTMODE) {
			visibilityChanged = true;
		} else if (property == RPEntity.PROP_OUTFIT) {
			representationChanged = true;
		} else if (property == Entity.PROP_TITLE) {
			titleChanged = true;
		} else if (property == RPEntity.PROP_TITLE_TYPE) {
			titleChanged = true;
		}
	}

	/**
	 * Perform an action.
	 *
	 * @param at
	 *            The action.
	 */
	@Override
	public void onAction(final ActionType at) {
		RPAction rpaction;

		switch (at) {
		case ATTACK:
			rpaction = new RPAction();

			rpaction.put("type", at.toString());
			rpentity.fillTargetInfo(rpaction);

			at.send(rpaction);
			break;

		case STOP_ATTACK:
			rpaction = new RPAction();

			rpaction.put("type", at.toString());
			rpaction.put("attack", "");

			at.send(rpaction);
			break;

		case PUSH:
			rpaction = new RPAction();

			rpaction.put("type", at.toString());
			rpentity.fillTargetInfo(rpaction);

			at.send(rpaction);
			break;

		default:
			super.onAction(at);
			break;
		}
	}
}
