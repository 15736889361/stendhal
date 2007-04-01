/*
 *  SoundSystem in games.stendhal.client
 *  file: SoundSystem.java
 * 
 *  Project stendhal
 *  @author Janet Hunt
 *  Created 25.12.2005
 * 
 *  Copyright (c) 2005 by Jane Hunt
 * 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 Place - Suite 330, Boston, MA 02111-1307, USA, or go to
 http://www.gnu.org/copyleft/gpl.html.
 */

package games.stendhal.client.sound;

import games.stendhal.client.SpriteStore;
import games.stendhal.client.WorldObjects;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.Player;
import games.stendhal.client.entity.User;
import games.stendhal.client.soundreview.AudioClip;
import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;

import marauroa.common.Log4J;
import org.apache.log4j.Logger;

/**
 * This sound system makes available a library of sounds which can be performed
 * under their library sound names. Sounds can be played as one-time occurrences
 * under various operation modi. (Not all combinations of modi are implemented.)
 * Sound volume can be set globally for all played sounds (system level) and per
 * particular sound performance or sound cycle schedule. The sound system also
 * has a global Mute switch.
 * <p>
 * Operation Modi <br>
 * Sounds can be played GLOBAL or MAP-LOCALIZED (attributed with a map location)
 * <br>
 * Sound can be played SINGULAR or in a CYCLIC pattern (which is governed by
 * some random pattern). <br>
 * Sounds can be played CERTAIN or PROBABLE (definable chances). <br>
 * Furthermore, there are the concepts of AUDIBILITY of map sounds and HEARING
 * RANGE of the player, which are both mutable settings.
 * <p>
 * Nature of Library Sounds <br>
 * Library sounds may be multifold and by this consist of a series of singular
 * sound samples. When such a sound is called to perform, one of the alternative
 * samples is randomly selected to play. The definition file determines the
 * inner structure of library sounds, including possible equalizing volume
 * settings.
 * <p>
 * External Files <br>
 * This sound system requires a definition file and a sound database. The
 * definition file is a properties file located in
 * <code>STORE_PROPERTYFILE</code>. The sound database is a ZIP file
 * containing PCM formatted sound samples stored under their sample names.
 * Library sound names, as used in the interface, are related to sample names in
 * the definition file. The location of the database may be also defined in the
 * definition file under property "soundbase".
 * 
 * @author Jane Hunt
 */
public class SoundSystem implements WorldObjects.WorldListener {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(SoundSystem.class);

	/** expected location of the sound definition file (classloader). */
	private static final String STORE_PROPERTYFILE = "data/sounds/stensounds.properties";

	private static SoundSystem singleton;

	/** */
	private Map<byte[], SoundCycle> cycleMap = Collections.synchronizedMap(new HashMap<byte[], SoundCycle>());

	/** */
	private ArrayList<AmbientSound> ambientList = new ArrayList<AmbientSound>();

	/** the used mixer */
	private Mixer mixer;

	/** global volume control */
	private FloatControl volumeCtrl;

	/** current volume setting */
	private int volumeSetting = 100;

	private float volumeDelta;

	/** true when mute is enabled */
	private boolean muteSetting;

	/** true when sound is initialized and operative */
	private boolean operative;

	/**
	 * Plays a sound of the given name from the library of this sound system.
	 * 
	 * @param name
	 *            token of sound
	 * @param volBot
	 *            relative bottom volume in percent, ranges 0..100
	 * @param volTop
	 *            relative top volume in percent, ranges 0..100
	 * @param correctionDB
	 *            loudness correction value (decibel)
	 * @return the sound <code>DataLine</code> that is being played, or
	 *         <b>null</b> on error
	 */
	protected DataLine playSoundIntern(String name, int volBot, int volTop, float correctionDB) {
		// verify start conditions
		if ((name == null) || (volBot == 0) || !operative || muteSetting) {
			return null;
		}

		if ((volBot < 0) || (volBot > 100) || (volTop < 0) || (volTop > 100) || (volTop < volBot)) {
			throw new IllegalArgumentException("bad volume setting");
		}

		// check/fetch sound
		ClipRunner clip = SoundEffectMap.getInstance().getSoundClip(name);
		if (clip == null) {
			return null;
		}

		int volume = volBot + Rand.rand(volTop - volBot + 1);
		return clip.play(volume, correctionDB, SoundSystem.get().getVolumeDelta());
	} // playSound

	/**
	 * Plays a sound of the given name from the library of this sound system.
	 * 
	 * @param name
	 *            token of sound
	 * @param volume
	 *            relative sound amplitude request in percent, ranges 0..100
	 * @return the sound <code>DataLine</code> that is being played, or
	 *         <b>null</b> on error
	 */
	public static DataLine playSound(String name, int volume) {
		return get().playSoundIntern(name, volume, volume, (float) 0.0);
	}

	/**
	 * Plays a sound of the given name from the library of this sound system by
	 * setting volume to a random value between volBot and volTop.
	 * 
	 * @param name
	 *            token of sound
	 * @param volBot
	 *            relative bottom volume in percent, ranges 0..100
	 * @param volTop
	 *            relative top volume in percent, ranges 0..100
	 * @return the sound <code>DataLine</code> that is being played, or
	 *         <b>null</b> on error
	 */
	static DataLine playSound(String name, int volBot, int volTop) {
		return get().playSoundIntern(name, volBot, volTop, (float) 0.0);
	}

	/**
	 * Plays a sound subject to a random performance chance.
	 * 
	 * @param chance
	 *            0..100 % chance value
	 * @param name
	 *            token of sound
	 * @param volBot
	 *            relative bottom volume in percent, ranges 0..100
	 * @param volTop
	 *            relative top volume in percent, ranges 0..100
	 * @return the sound <code>DataLine</code> that is being played, or
	 *         <b>null</b> on error or if performance is bailed
	 */
	static DataLine probablePlaySound(int chance, String name, int volBot, int volTop) {
		if (Rand.rand(100) < chance) {
			return get().playSoundIntern(name, volBot, volTop, (float) 0.0);
		}
		return null;
	}

	/**
	 * Plays a sound bound to a given map position, possibly restricted by an
	 * audibility area confinement. The sound volume is automatically adjusted
	 * to reflect the player's map position and hearing range.
	 * 
	 * @param where
	 *            map position expressed in zone's coordinate system
	 * @param audibility
	 *            rectangel area of the coordinate system where this sound is
	 *            audible; if <b>null</b> it is audible everywhere
	 * @param name
	 *            library sound token
	 * @param volBot
	 *            relative bottom volume
	 * @param volTop
	 *            relative top volume
	 * @param chance
	 *            percent chance of performance
	 * @return <code>javax.sound.sampled.DataLine</code> the sound line that
	 *         is being performed or <b>null</b> if no performance takes place
	 */
	public static DataLine playMapSound(Point2D where, Rectangle2D audibility, String name, int volBot, int volTop,
	        int chance) {
		Point2D playerPosition;
		Rectangle2D playerHearing;
		double distance;
		double maxDist;
		int fogVolume;

		// broken cases
		if ((where == null) || (chance < 0)) {
			throw new IllegalArgumentException();
		}

		// lost chance cases (random)
		if ((chance < 100) && (Rand.rand(100) >= chance)) {
			return null;
		}

		
		if (User.isNull()) {
			return null;
		}
		Player user = User.get();

		playerPosition = user.getPosition();
		playerHearing = user.getHearingArea();

		// exclusion cases
		if (!playerHearing.contains(where) || ((audibility != null) && !audibility.contains(playerPosition))) {
			return null;
		}

		logger.debug("SoundSystem: playing map sound (" + name + ") at pos " + (int) where.getX() + ", "
		        + (int) where.getY());

		// determine sound volume cutoff due to distance (fog value)
		distance = where.distance(playerPosition);
		maxDist = playerHearing.getWidth() / 2;
		fogVolume = Math.max(0, (int) (95 * (maxDist - distance) / maxDist + 5));

		return get().playSoundIntern(name, volBot, volTop, DBValues.getDBValue(fogVolume));
	} // playMapSound

	/** plays (?) and registers an ambient sound 
	 * @param ambient the sound to be registered 
	 * */
	void playAmbientSound(AmbientSound ambient) {

		ambient.play();

		synchronized (ambientList) {
			ambientList.add(ambient);
		}
	} // playAmbientSound

	/**
	 * removes the ambient sound from the internal list. 
	 * It should already be stopped.
	 
	 * @param ambient the ambient sound to be removed
	 */
	static void stopAmbientSound(AmbientSound ambient) {
		//		  TODO: assert the sound  is stopped
		SoundSystem sys;

		sys = get();

		synchronized (sys.ambientList) {
			sys.ambientList.remove(ambient);
		}
	}

	/** Stops and removes all ambient sounds. */
	private void clearAmbientSounds() {
		synchronized (ambientList) {
			List<AmbientSound> list = new ArrayList<AmbientSound>(ambientList);
			for (AmbientSound sound : list) {
				sound.terminate();
			}
		}
	}

	/**
	 * Starts cyclic performance of a given library sound, attributed to a
	 * specific entity on the map. There can only be one sound cycle for an
	 * entity at a given time. If an sound cycle is started while a previous
	 * cycle is defined for the entity, the previous cycle is discarded and any
	 * ongoing sound performance stopped.
	 * 
	 * @param entity
	 *            the game object that makes the sound
	 * @param token
	 *            the library sound
	 * @param period
	 *            maximum time period for one sound occurrence
	 * @param volBot
	 *            bottom volume
	 * @param volTop
	 *            top volume
	 * @param chance
	 *            percent chance of performance
	 * @return SoundCycle
	 */
	public static SoundCycle startSoundCycle(Entity entity, String token, int period, int volBot, int volTop, int chance) {
		SoundSystem sys;
		SoundCycle cycle;
		SoundCycle c1;
		byte[] entity_token;

		if (!(sys = get()).isOperative()) {
			return null;
		}

		entity_token = entity.get_IDToken();
		cycle = null;
		synchronized (sys.cycleMap) {
			try {
				cycle = new SoundCycle(entity, token, period, volBot, volTop, chance);
				cycle.play();

				c1 = sys.cycleMap.get(entity_token);
				if (c1 != null) {
					c1.terminate();
				}

				sys.cycleMap.put(entity_token, cycle);
			} catch (IllegalStateException e) {
				logger.error("*** Undefined sound sample: " + token, e);
			}
		}
		return cycle;
	} // startSoundCycle

	/**
	 * Stops execution of the sound cycle for a specific map entity. This will
	 * interrupt any ongoing sound performance immediately.
	 * 
	 * @param entity_ID
	 *            byte[] identity token of the map entity
	 */
	public static void stopSoundCycle(byte[] entity_ID) {
		SoundCycle cycle;
		SoundSystem sys;

		sys = get();
		cycle = sys.cycleMap.get(entity_ID);
		if (cycle != null) {
			synchronized (sys.cycleMap) {
				sys.cycleMap.remove(entity_ID);
				cycle.terminate();
			}
		}
	}

	/**
	 * Loads a junk of data from the jar soundfile and returns it as a byte
	 * array.
	 * 
	 * @param entry
	 * @return the data in the Zipentry
	 * @throws IOException
	 */
	private byte[] getData(String name) throws IOException {
		InputStream in;
		ByteArrayOutputStream bout;

		in = getResourceStream(name);
		bout = new ByteArrayOutputStream();
		transferData(in, bout, 4096);
		in.close();
		return bout.toByteArray();
	}

	/**
	 * Whether the parameter sound is available in this sound system.
	 * 
	 * @param name token of sound
	 * @return true, iif it is available
	 */
	boolean contains(String name) {
		return (name != null) && SoundEffectMap.getInstance().containsKey(name);
	}

	/**
	 * Obtains a resource input stream. Fetches currently from the main
	 * program's classloader.
	 * 
	 * @param name
	 * @return InputStream
	 * @throws IOException
	 */
	public static InputStream getResourceStream(String name) throws IOException {
		URL url = SpriteStore.get().getResourceURL(name);
		if (url == null) {
			throw new FileNotFoundException(name);
		}
		return url.openStream();
	}

	private void init() {

		
		String hstr;
		int loaded;
		/**
		 * count the amount of file that could not be loaded
		 */
		int failedCounted;
		int count;
		int pos;

		int loudness;

		if (!initJavaSound()) {
			logger.error("*** SOUNDSYSTEM JAVA INIT ERROR");
			return;
		}

		try {
			Properties prop;
			// load sound properties
			prop = new Properties();
			loadSoundProperties(prop);

			// get sound library filepath
			String soundBase = prop.getProperty("soundbase", "data/sounds/");
		
		
			Enumeration<Object> maps = prop.keys();
			// read all load-permitted sounds listed in properties
			// from soundfile into cache map
			failedCounted = 0;
			loaded = 0;
			count = 0;
			
			for (String key=(String) maps.nextElement();maps.hasMoreElements(); key = (String) maps.nextElement()) {
				byte[] soundData;
				String value;
				String name;
				if (isValidEntry(key,prop.getProperty(key))) {
					name = key.substring(4);
					value = prop.getProperty(key);

					logger.debug("- sound definition: " + name + " = " + value);

					String filename = null;
					if ((pos = value.indexOf(',')) > -1) {
						filename = value.substring(0, pos);
					} else {
						filename = value;
					}
					soundData = getData(soundBase + filename);

					// construct sound clip from sample data
					// (we always do that to verify sound sample format)
					AudioClip sound;
					try {
						// determine equalizing loudness setting
						loudness = 100;
						if ((pos = value.lastIndexOf(',')) != -1) {
							loudness = MathHelper.parseInt_default(value.substring(pos + 1), 100);
						}

						// investigate sample status
						int i;
						if ((i = name.indexOf('.')) != -1) {
							name = name.substring(0, i);
						}

						// sound = new ClipRunner(this, name + "@" + path,
						// soundData, loudness);
						sound = new AudioClip(mixer, name + "@" + filename, soundData, loudness);
						count++;
					} catch (Exception e) {
						// could not validate sound file content
						hstr = "*** CORRUPTED SOUND: " + name + "=" + filename;
						logger.error(hstr, e);
						failedCounted++;
						continue;
					}

					// store new sound object into soundsystem library map if opted
					//if (load) {
					logger.debug("- storing mem-library soundclip: " + name);

					// stores the clip sound in memory
					ClipRunner clip = SoundEffectMap.getInstance().getSoundClip(name);
					if (clip == null) {
						clip = new ClipRunner(name);
						SoundEffectMap.getInstance().put(name, clip);
					}
					clip.addSample(sound);

					// memorizes the sound data (only for init purposes)
					//			dataList.put(path, soundData);
					loaded++;
				}
			} // for

			// report to startup console

			hstr = "Stendhal Soundsystem OK: " + count + " samples approved / " + loaded + " loaded / "
			        + SoundEffectMap.getInstance().size() + " library sounds";
			logger.info(hstr);
			System.out.println(hstr);
			if (failedCounted != 0) {
				hstr = "missing or corrupted sounds: " + failedCounted;
				logger.info(hstr);
				System.out.println(hstr);
			}

			// register listeners
			WorldObjects.addWorldListener(this);
			operative = true;
		}

		catch (IOException e) {
			hstr = "*** SOUNDSYSTEM LOAD ERROR";
			logger.error(hstr, e);
			return;
		}
	} // init

	//	 

	// (do not load when ",x" trailing path; 
	// always load when "." in
	// name)

	/**
	 * A key/value pair is assumed valid if 	
	 * <ul>
	 *    <li>key starts with "sfx." <b>and </b></li>
	 *    <li>key does not end with ",x"</li>
	 *    <li>or value contains a "."</li>
	 * </ul>
	 * @param key
	 * @param value
	 * @return true, if it is valid, false otherwise
	 */
	boolean isValidEntry(String key, String value) {
		boolean load;
		int pos1;
		if (key.startsWith("sfx.")) {
			if ((pos1 = value.indexOf(',')) > -1) {
				load = value.substring(pos1 + 1).charAt(0) != 'x';
			} else {
				load = true;
			}
			load |= value.indexOf('.') != -1;
			return load;
		} else {
			return false;
		}
	}

	/**
	 * @param prop
	 *            the Property Object to load to
	 * @throws IOException
	 */
	private void loadSoundProperties(Properties prop) throws IOException {
		InputStream in1;

		in1 = getResourceStream(STORE_PROPERTYFILE);
		prop.load(in1);
		in1.close();
	}

	/**
	 * @return <b>true</b> if javaSound init is successfull,
	 *         <p>
	 *         <b>false</b> otherwise
	 */
	private boolean initJavaSound() {
		Info info;
		Info[] mixInfos;
		String hstr;

		if (((mixInfos = AudioSystem.getMixerInfo()) == null) || (mixInfos.length == 0)) {
			logger.error("*** SoundSystem: no sound driver available!");
			return false;
		}

		mixer = AudioSystem.getMixer(null); // mixInfos[4] );
		info = mixer.getMixerInfo();
		hstr = "Sound driver: " + info.getName() + "(" + info.getDescription() + ")";
		logger.info(hstr);

		// try a master volume control
		try {
			volumeCtrl = (FloatControl) mixer.getControl(FloatControl.Type.MASTER_GAIN);
			volumeCtrl.setValue(0f);
		} catch (Exception e) {
			logger.debug("SoundSystem: no master volume controls");
		}

		return true;
	} // initJavaSound

	/**
	 * Sets the global Mute switch of this sound system. Does nothing on
	 * duplicate call.
	 */
	public void setMute(boolean v) {
		if (v == muteSetting) {
			return;
		}

		logger.info("- sound system setting mute = " + (v ? "ON" : "OFF"));
		muteSetting = v;

		synchronized (ambientList) {
			for (AmbientSound ambient : ambientList) {
				if (v) {
					ambient.stop();
				} else {
					ambient.play();
				}
			}
		}
	} // setMute

	/**
	 * Returns the actual state of the global Mute switch of this sound system.
	 * 
	 * @return <b>true</b> if and only if Mute is ON (silent)
	 */
	public boolean isMute() {
		return muteSetting;
	}

	/**
	 * Sets a global volume level for all sounds played with this sound system.
	 * The volume value ranges between 0 (silent) and 100 (loudest).
	 * 
	 * @param volume
	 *            0 .. 100
	 */
	public void setVolume(int volume) {
		float dB;

		volume = (volume < 0) ? volume = 0 : volume;
		volume = (volume > 100) ? volume = 100 : volume;

		dB = DBValues.getDBValue(volume);
		logger.info("- sound system setting volume dB = " + dB + "  (gain " + volume + ")");

		volumeSetting = volume;
		if (volumeCtrl != null) {
			volumeCtrl.setValue(dB);
		} else {
			volumeDelta = dB;

			// update ambient sounds
			synchronized (ambientList) {
				for (AmbientSound amb : ambientList) {
					amb.updateVolume();
				}
			}
		}
	} // setVolume

	/**
	 * Returns the current value of this sound system's voume setting.
	 * 
	 * @return volume ranging 0 (silent) .. 100 (loudest)
	 */
	public int getVolume() {
		return volumeSetting;
	}

	/** 
	 * Whether the sound system has been initialized and is ready to operate.
	 *
	 * @return true, iff the sound system was initialized
	 */
	public boolean isOperative() {
		return operative;
	}

	/**
	 * @return the singleton instance of the Stendhal sound system.
	 */
	public static SoundSystem get() {
		if (singleton == null)
			singleton = new SoundSystem();
		return singleton;
	}

	/**
	 * Releases any resources associated with this sound system. The system is
	 * rendered inoperative.
	 */
	public void exit() {
		clearAmbientSounds();
		logger.info("sound system exit performed, inactive");
	}

	private SoundSystem() {
		init();
	}

	/**
	 * Transfers the contents of the input stream to the output stream until the
	 * end of input stream is reached.
	 * 
	 * @param input
	 * @param output
	 * @param bufferSize
	 * @throws java.io.IOException
	 */
	static void transferData(InputStream input, OutputStream output, int bufferSize) throws java.io.IOException {
		byte[] buffer = new byte[bufferSize];
		int len;

		while ((len = input.read(buffer)) > 0) {
			output.write(buffer, 0, len);
		}
	} // transferData

	// ************* INNER CLASSES ***********************

	private String actualZone = "";

	public void zoneEntered(String zone) {
		AmbientSound baseAmb;
		AmbientSound ambient;
		Point2D soundPos;
		String hstr;

		hstr = "-- SoundSys: ZONE ENTERED: " + zone;
		logger.debug(hstr);
		// System.out.println( hstr );
		actualZone = zone;

		// 0_SEMOS_VILLAGE
		if (zone.equals("0_semos_village_w")) {
			// global ambient
			ambient = new AmbientSound("semos-village-overall-1", 10);
			ambient.addLoop("wind-loop-1", 25, 0);
			playAmbientSound(ambient);

			// creaking tree and wind
			ambient = AmbientStore.getAmbient("wind-tree-1");

			soundPos = new Point2D.Double(13, 42);
			ambient = new AmbientSound(ambient, "semos-village-tree", soundPos, 30, 25);
			playAmbientSound(ambient);

			// larks
			baseAmb = AmbientStore.getAmbient("meadow-larks-1");

			soundPos = new Point2D.Double(50, 16);
			ambient = new AmbientSound(baseAmb, "semos-village-larks-1", soundPos, 30, 50);
			playAmbientSound(ambient);

			// blackbirds
			baseAmb = AmbientStore.getAmbient("blackbirds-1");

			soundPos = new Point2D.Double(16, 20);
			ambient = new AmbientSound(baseAmb, "semos-village-blackbirds-1", soundPos, 30, 50);
			playAmbientSound(ambient);

			// frog
			baseAmb = AmbientStore.getAmbient("single-frog-1");

			soundPos = new Point2D.Double(28, 15);
			ambient = new AmbientSound(baseAmb, "semos-village-frog-1", soundPos, 6, 30);
			playAmbientSound(ambient);

		}
		// 0_SEMOS_CITY
		else if (zone.equals("0_semos_city")) {
			// blackbirds
			baseAmb = AmbientStore.getAmbient("blackbirds-1");

			soundPos = new Point2D.Double(29, 8);
			ambient = new AmbientSound(baseAmb, "semos-city-blackbirds-1", soundPos, 30, 80);
			playAmbientSound(ambient);

			// chicken
			baseAmb = AmbientStore.getAmbient("chicken-1");

			soundPos = new Point2D.Double(8, 30);
			ambient = new AmbientSound(baseAmb, "semos-city-fowl-1", soundPos, 12, 50);
			playAmbientSound(ambient);

			soundPos = new Point2D.Double(47, 25);
			ambient = new AmbientSound(baseAmb, "semos-city-fowl-2", soundPos, 15, 50);
			playAmbientSound(ambient);

			// worksounds
			baseAmb = AmbientStore.getAmbient("build-works-1");

			soundPos = new Point2D.Double(12, 38);
			ambient = new AmbientSound(baseAmb, "semos-city-works-1", soundPos, 8, 25);
			playAmbientSound(ambient);

			// tavern noise
			baseAmb = AmbientStore.getAmbient("tavern-noise-1");

			soundPos = new Point2D.Double(45, 37);
			ambient = new AmbientSound(baseAmb, "semos-city-tavern-1", soundPos, 10, 40);
			playAmbientSound(ambient);

		} else if (zone.equals("int_semos_blacksmith")) {
			// global ambient
			ambient = new AmbientSound("blacksmith-overall-1", 20);
			ambient.addCycle("hammer-2", 45000, 20, 40, 65);
			playAmbientSound(ambient);

			soundPos = new Point2D.Double(11, 3);
			ambient = new AmbientSound("blacksmith-forgefire-main", soundPos, 30, 50);
			ambient.addLoop("forgefire-1", 50, 0);
			ambient.addCycle("firesparks-1", 60000, 10, 50, 80);
			playAmbientSound(ambient);

			soundPos = new Point2D.Double(3, 3);
			ambient = new AmbientSound("blacksmith-forgefire-side", soundPos, 6, 50);
			ambient.addLoop("forgefire-2", 50, 0);
			ambient.addLoop("forgefire-3", 50, 0);
			playAmbientSound(ambient);

		}
		// 0_SEMOS_ROAD_ADOS
		else if (zone.equals("0_semos_road_e")) {
			// creaking tree and wind
			baseAmb = AmbientStore.getAmbient("wind-tree-1");

			soundPos = new Point2D.Double(10, 45);
			ambient = new AmbientSound(baseAmb, "road-ados-tree-1", soundPos, 30, 30);
			playAmbientSound(ambient);

			soundPos = new Point2D.Double(54, 59);
			ambient = new AmbientSound(baseAmb, "road-ados-tree-2", soundPos, 100, 50);
			playAmbientSound(ambient);

			soundPos = new Point2D.Double(65, 31);
			ambient = new AmbientSound(baseAmb, "road-ados-tree-3", soundPos, 100, 30);
			playAmbientSound(ambient);

			// beach water
			baseAmb = AmbientStore.getAmbient("water-beach-1");

			soundPos = new Point2D.Double(32, 46);
			ambient = new AmbientSound(baseAmb, "road-ados-beachwater-1", soundPos, 7, 25);
			playAmbientSound(ambient);

			soundPos = new Point2D.Double(43, 47);
			ambient = new AmbientSound(baseAmb, "road-ados-beachwater-2", soundPos, 7, 25);
			playAmbientSound(ambient);

			soundPos = new Point2D.Double(32, 55);
			ambient = new AmbientSound(baseAmb, "road-ados-beachwater-3", soundPos, 12, 35);
			playAmbientSound(ambient);

			// water at bridge
			baseAmb = AmbientStore.getAmbient("water-flow-1");

			soundPos = new Point2D.Double(47, 47);
			ambient = new AmbientSound(baseAmb, "road-ados-bridge-1", soundPos, 3, 50);
			playAmbientSound(ambient);

			// larks
			baseAmb = AmbientStore.getAmbient("meadow-larks-1");

			soundPos = new Point2D.Double(15, 15);
			ambient = new AmbientSound(baseAmb, "road-ados-larks-1", soundPos, 30, 50);
			playAmbientSound(ambient);

			soundPos = new Point2D.Double(32, 33);
			ambient = new AmbientSound(baseAmb, "road-ados-larks-2", soundPos, 30, 50);
			playAmbientSound(ambient);

			// bushbirds
			baseAmb = AmbientStore.getAmbient("bushbirds-1");

			soundPos = new Point2D.Double(83, 56);
			ambient = new AmbientSound(baseAmb, "road-ados-bushbirds-1", soundPos, 20, 80);
			playAmbientSound(ambient);

			soundPos = new Point2D.Double(118, 57);
			ambient = new AmbientSound(baseAmb, "road-ados-bushbirds-2", soundPos, 20, 90);
			playAmbientSound(ambient);

		}
	}

	/**
	 * @return the volume delta
	 */
	public float getVolumeDelta() {
		return volumeDelta;
	}

	/*
	 * Overridden:
	 * 
	 * @see games.stendhal.client.WorldObjects.WorldListener#zoneLeft(java.lang.String)
	 */
	public void zoneLeft(String zone) {
		String hstr = "-- SoundSys: ZONE LEFT: " + zone;
		logger.debug(hstr);
		// System.out.println( hstr );

		if (zone.equals(actualZone)) {
			clearAmbientSounds();
		}
	}

	/*
	 * Overridden:
	 * 
	 * @see games.stendhal.client.WorldObjects.WorldListener#playerMoved(games.stendhal.client.entity.Player)
	 */
	public void playerMoved(Player player) {
		// update ambient sounds about player position
		synchronized (ambientList) {
			for (AmbientSound a : ambientList) {
				a.performPlayerMoved(player, isOperative(), isMute());
			}
		}
	}

}
