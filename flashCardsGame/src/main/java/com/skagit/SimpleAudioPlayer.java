package com.skagit;

/** From: https://www.geeksforgeeks.org/play-audio-file-using-java/ */
/** <pre>
 * Use Audacity to convert the voice memo's m4a files to aiff files as follows:
 * 1. Open Audacity
 * 2. Open the m4a file.
 * 3. File/Export Audio
 * 4. Select "other uncompressed files"
 * 5. Be sure AIFF is in the "Header" box.
 * </pre>
 * */

/** Java class to play an Audio file using Clip Object. */
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SimpleAudioPlayer {

	Long _currentFrame;
	Clip _clip;

	String _status;

	AudioInputStream _audioInputStream;
	final String _filePath;

	@SuppressWarnings("unused")
	public SimpleAudioPlayer(final String filePath)
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		_filePath = filePath;
		_audioInputStream = AudioSystem
				.getAudioInputStream(new File(_filePath).getAbsoluteFile());
		_clip = AudioSystem.getClip();
		_audioInputStream.mark(Integer.MAX_VALUE);

		_clip.open(_audioInputStream);
		_clip.start();
		final int x0 = 0;
		_audioInputStream.reset();
		_clip = AudioSystem.getClip();
		_clip.open(_audioInputStream);
		_clip.start();
		final int x2 = 0;
		_clip.close();

		_audioInputStream = AudioSystem
				.getAudioInputStream(new File(_filePath).getAbsoluteFile());
		_clip = AudioSystem.getClip();
		_clip.open(_audioInputStream);
		_clip.start();
		final int x1 = 0;
		_clip.close();
		// _clip.loop(Clip.LOOP_CONTINUOUSLY);
		// _clip.loop(0);
	}

	public static void main(final String[] args) {
		final String filePath = "TestAudioFile.aiff";
		try {
			final SimpleAudioPlayer audioPlayer = new SimpleAudioPlayer(filePath);
			System.exit(33);
			try (Scanner sc = new Scanner(System.in)) {
				while (true) {
					System.out.println("1. pause");
					System.out.println("2. resume");
					System.out.println("3. restart");
					System.out.println("4. stop");
					System.out.println("5. Jump to specific time");
					final int c = sc.nextInt();
					audioPlayer.goToChoice(c);
					if (c == 4) {
						break;
					}
				}
			}
		} catch (final Exception e) {
			System.out.println("Error with playing sound.");
			e.printStackTrace();
		}
	}

	/** Work as the user enters his choice. */
	private void goToChoice(final int c)
			throws IOException, LineUnavailableException, UnsupportedAudioFileException {
		switch (c) {
			case 1 :
				pause();
				break;
			case 2 :
				resumeAudio();
				break;
			case 3 :
				restart();
				break;
			case 4 :
				stop();
				break;
			case 5 :
				System.out
						.println("Enter time (" + 0 + ", " + _clip.getMicrosecondLength() + ")");
				final Scanner sc = new Scanner(System.in);
				final long c1 = sc.nextLong();
				jump(c1);
				break;
		}
	}

	public void play() {
		_clip.start();
		_status = "play";
	}

	/** Method to pause the audio. */
	public void pause() {
		if (_status.equals("paused")) {
			System.out.println("audio is already paused");
			return;
		}
		this._currentFrame = this._clip.getMicrosecondPosition();
		_clip.stop();
		_status = "paused";
	}

	/** Method to resume the audio. */
	public void resumeAudio()
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (_status.equals("play")) {
			System.out.println("Audio is already " + "being played");
			return;
		}
		_clip.close();
		resetAudioStream();
		_clip.setMicrosecondPosition(_currentFrame);
		this.play();
	}

	// Method to restart the audio
	public void restart()
			throws IOException, LineUnavailableException, UnsupportedAudioFileException {
		_clip.stop();
		_clip.close();
		resetAudioStream();
		_currentFrame = 0L;
		_clip.setMicrosecondPosition(0);
		this.play();
	}

	// Method to stop the audio
	public void stop()
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		_currentFrame = 0L;
		_clip.stop();
		_clip.close();
	}

	// Method to jump over a specific part
	public void jump(final long c)
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (c > 0 && c < _clip.getMicrosecondLength()) {
			_clip.stop();
			_clip.close();
			resetAudioStream();
			_currentFrame = c;
			_clip.setMicrosecondPosition(c);
			this.play();
		}
	}

	/** Method to reset audio stream. */
	public void resetAudioStream()
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		_audioInputStream = AudioSystem
				.getAudioInputStream(new File(_filePath).getAbsoluteFile());
		_clip.open(_audioInputStream);
		_clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

}
