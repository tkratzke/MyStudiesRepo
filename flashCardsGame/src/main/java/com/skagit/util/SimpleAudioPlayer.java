package com.skagit.util;

/**
 * <pre>
 * From: https://www.geeksforgeeks.org/play-audio-file-using-java/
 * Java class to play an Audio file using Clip Object.
 * </pre>
 * */

/** <pre> To make sure the clip gets played, look at:
 * https://stackoverflow.com/questions/557903/how-can-i-wait-for-a-java-sound-clip-to-finish-playing-back
 * We really don't use this website though.
 * </pre>
 */

/** <pre>
 * Use Audacity to convert iphone's voice memo's m4a files to aiff files as follows:
 * 1. Open Audacity
 * 2. Open the m4a file.
 * 3. File/Export Audio
 * 4. Select "other uncompressed files"
 * 5. Be sure AIFF is in the "Header" box.
 * </pre>
 *
 * Introduction to lambda expressions:
 * https://www.w3schools.com/java/java_lambda.asp
 * */

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SimpleAudioPlayer {

    public static boolean checkAudioFile(final File f, final boolean playFile, final long lagLengthInMs) {
	final CountDownLatch countDownLatch = new CountDownLatch(1);
	try (//
		final AudioInputStream stream = AudioSystem.getAudioInputStream(f.getAbsoluteFile()); //
		Clip clip = AudioSystem.getClip() //
	) {
	    clip.addLineListener(e -> {
		if (e.getType() == LineEvent.Type.STOP) {
		    countDownLatch.countDown();
		}
	    });
	    clip.open(stream);
	    if (!playFile) {
		return true;
	    }
	    clip.start();
	    try {
		countDownLatch.await();
		/** Wait a specified amount of time for any latency after the STOP event. */
		if (lagLengthInMs > 0L) {
		    Thread.sleep(lagLengthInMs);
		}
	    } catch (final InterruptedException e) {
	    }
	} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
	    return false;
	}
	return true;
    }

    public static void main(final String[] args) {
	try {
	    final String filePath = "RunDir/Data/SoundFilesDirs/VN.00-SoundFiles/Tran-00/000-winter.aiff";
	    System.out.println(checkAudioFile(new File(filePath), /* playFile= */true, /* lagLengthInMs= */250));
	} catch (final Exception ex) {
	    System.out.println("Error with playing sound.");
	    ex.printStackTrace();
	}
    }

}
