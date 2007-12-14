package games.stendhal.client.soundreview;

import games.stendhal.common.Rand;

import java.util.HashMap;

public class Sound {
	public static HashMap<String, String[]> soundArray;

	private int x;

	private int y;

	private String name;

	// private boolean loop=false;
	Sound(String name, int x, int y) {
		super();
		this.x = x;
		this.y = y;
		this.name = name;
	}

	public Sound(String name, int x, int y, boolean shallLoop) {
		this(name, x, y);
		// loop = shallLoop;
	}

	public AudioClip play() {
		if (soundArray.containsKey(name)) {

			return SoundMaster.play(
					soundArray.get(name)[Rand.rand(soundArray.get(name).length)],
					x, y);

		} else {
			return SoundMaster.play(name, x, y);

		}

	}

	static {
		soundArray = new HashMap<String, String[]>();

		soundArray.put("smith-mix", new String[] { "hammer-1.wav",
				"smith-1.wav" });
		soundArray.put("chicken-mix", new String[] { "chicken-1.wav" });
		soundArray.put("tavern-mix", new String[] { "kettle-1.wav",
				"bottle-2.wav", "trash-1.wav", "trash-21.wav", "trash-3.wav",
				"metal-can-1.wav", "drain-water-11.wav", "dishbreak-1.wav",
				"dishbreak-2.wav", "dishes-1.wav", "dishes-21.wav",
				"creaky-door-1.wav", "creaky-door-2.wav", "ice-cubes-1.wav",
				"window-close-1.wav" });
		soundArray.put("treecreak-1", new String[] { "treecreak-1.wav" });
		soundArray.put("blackbird-1", new String[] { "blackbird-11.wav" });
		soundArray.put("blackbird-mix", new String[] { "blackbird-6b.wav",
				"blackbird-7b.wav", "blackbird-8b.wav" });
		soundArray.put("firesparks-1", new String[] { "fire-sparkes-1.wav" });
		soundArray.put("lark-1", new String[] { "lark-1.wav" });
		soundArray.put("lark-2", new String[] { "lark-2.wav" });
		soundArray.put("bushbird-mix-1", new String[] { "bird-1b.wav" });
		soundArray.put("water-splash-1", new String[] { "water-2.wav" });
		soundArray.put("water-splash-2", new String[] { "water-3.wav" });
		soundArray.put("water-wave-1", new String[] { "wave-11.wav" });
	}

}
