package com.skagit;

/**
 * <pre>
 * From: https://www.geeksforgeeks.org/play-audio-file-using-java/
 * Java class to play an Audio file using Clip Object.
 * </pre>
 * */

/** <pre>
 * Use Audacity to convert iphone's voice memo's m4a files to aiff files as follows:
 * 1. Open Audacity
 * 2. Open the m4a file.
 * 3. File/Export Audio
 * 4. Select "other uncompressed files"
 * 5. Be sure AIFF is in the "Header" box.
 * </pre>
 * */

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SimpleAudioPlayer {

    Long _currentFrame;
    Clip _clip;
    String _status;
    AudioInputStream _audioInputStream;
    String _filePath;

    public SimpleAudioPlayer(final String filePath)
	    throws UnsupportedAudioFileException, IOException, LineUnavailableException {
	_filePath = filePath;
	_audioInputStream = AudioSystem.getAudioInputStream(new File(_filePath).getAbsoluteFile());
	/** Create _clip reference. */
	_clip = AudioSystem.getClip();
	_clip.open(_audioInputStream);
	_clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    private void goToChoice(final int c) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
	switch (c) {
	case 1:
	    pause();
	    break;
	case 2:
	    resumeAudio();
	    break;
	case 3:
	    restart();
	    break;
	case 4:
	    stop();
	    break;
	case 5:
	    System.out.println("Enter time (" + 0 + ", " + _clip.getMicrosecondLength() + ")");
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

    public void pause() {
	if (_status.equals("paused")) {
	    System.out.println("audio is already paused");
	    return;
	}
	_currentFrame = _clip.getMicrosecondPosition();
	_clip.stop();
	_status = "paused";
    }

    public void resumeAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
	if (_status.equals("play")) {
	    System.out.println("Audio is already " + "being played");
	    return;
	}
	_clip.close();
	resetAudioStream();
	_clip.setMicrosecondPosition(_currentFrame);
	play();
    }

    public void restart() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
	_clip.stop();
	_clip.close();
	resetAudioStream();
	_currentFrame = 0L;
	_clip.setMicrosecondPosition(0);
	play();
    }

    public void stop() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
	_currentFrame = 0L;
	_clip.stop();
	_clip.close();
    }

    public void jump(final long c) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
	if (c > 0 && c < _clip.getMicrosecondLength()) {
	    _clip.stop();
	    _clip.close();
	    resetAudioStream();
	    _currentFrame = c;
	    _clip.setMicrosecondPosition(c);
	    play();
	}
    }

    public void resetAudioStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
	_audioInputStream = AudioSystem.getAudioInputStream(new File(_filePath).getAbsoluteFile());
	_clip.open(_audioInputStream);
	_clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public static boolean validate(final File f) {
	if (f == null || !f.isFile()) {
	    return false;
	}
	try (//
		AudioInputStream ais = AudioSystem.getAudioInputStream(f.getAbsoluteFile());
		Clip clip = AudioSystem.getClip() //
	) {
	    return true;
	} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
	}
	return false;
    }

    public static void playSoundFileIfPossible(final File f) {
	if (f == null || !f.isFile()) {
	    return;
	}
	final CountDownLatch syncLatch = new CountDownLatch(1);
	try (//
		AudioInputStream ais = AudioSystem.getAudioInputStream(f.getAbsoluteFile());
		Clip clip = AudioSystem.getClip() //
	) {
	    clip.addLineListener(e -> {
		if (e.getType() == LineEvent.Type.STOP) {
		    syncLatch.countDown();
		}
	    });
	    clip.open(ais);
	    clip.start();
	    try {
		syncLatch.await();
	    } catch (final InterruptedException e1) {
	    }
	} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
	}
    }

    public static void mainx(final String[] args) {
	final String filePath = "Your path for the file";
	try (Scanner sc = new Scanner(System.in)) {
	    try {
		final SimpleAudioPlayer audioPlayer = new SimpleAudioPlayer(filePath);
		audioPlayer.play();
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
	    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
		System.out.println("Error with playing sound.");
		e.printStackTrace();
	    }
	}
    }

}
