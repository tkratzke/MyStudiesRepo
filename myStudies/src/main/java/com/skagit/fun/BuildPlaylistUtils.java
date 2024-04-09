package com.skagit.fun;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.skagit.util.ElementIterator;
import com.skagit.util.LsFormatter;
import com.skagit.util.TimeUtilities;

public class BuildPlaylistUtils {

    final private static int _MinSecondsForSong = 15;
    final private static int _SecondsPerGroup = 12 * 60;
    final private static int _SecondsPerBigGroup = 45 * 60;

    private enum PlaylistType {
	WPL, XML
    }

    private static final PlaylistType[] _PlaylistTypes = PlaylistType.values();

    private static class AudioInfo {
	private final File _f;
	private final String _title;
	private final int _seconds;
	private final int _trackId;

	private String getString() {
	    if (_f == null) {
		return "Bad file.";
	    }
	    String s = String.format("%s::%s (%s)", _f.getAbsolutePath(), _title,
		    TimeUtilities.durationInSecondsToMinsSecsString(_seconds, /* nDigitsForMinutes= */2));
	    if (_trackId >= 0) {
		s += String.format(" TrackId[%d]", _trackId);
	    }
	    return s;
	}

	@Override
	public String toString() {
	    return getString();
	}

	private AudioInfo(final Element element, final PlaylistType playlistType, final File dirOfPlaylistFile) {
	    File f = null;
	    int trackId = -1;
	    switch (playlistType) {
	    case WPL:
		if (element.getTagName().compareToIgnoreCase("media") == 0 && element.hasAttribute("src")) {
		    final String fileName = element.getAttribute("src");
		    try {
			f = new File(dirOfPlaylistFile, fileName).getCanonicalFile();
		    } catch (final IOException e) {
		    }
		}
		break;
	    case XML:
		if (element.getTagName().compareToIgnoreCase("dict") == 0) {
		    final ElementIterator it0 = new ElementIterator(element);
		    while (it0.hasNextElement()) {
			final Element elt = it0.nextElement();
			if (elt.getTagName().compareToIgnoreCase("key") == 0) {
			    final String textContent = elt.getTextContent();
			    if (textContent.compareToIgnoreCase("Location") == 0) {
				final String urlString = it0.nextElement().getTextContent();
				String filePath = null;
				/** Old style had: new URL(urlString).toURI().getPath() */
				try {
				    filePath = new File(urlString).toURI().getPath();
				} catch (final Exception e) {
				}
				f = filePath == null ? null : new File(filePath);
				break;
			    }
			}
		    }
		    if (f != null) {
			final ElementIterator it1 = new ElementIterator(element);
			while (it1.hasNextElement()) {
			    final Element elt = it1.nextElement();
			    if (elt.getTagName().compareToIgnoreCase("key") == 0) {
				final String textContent = elt.getTextContent();
				if (textContent.compareToIgnoreCase("Track ID") == 0) {
				    final String trackIdString = it1.nextElement().getTextContent();
				    try {
					trackId = Integer.parseInt(trackIdString);
				    } catch (final NumberFormatException e1) {
				    }
				    break;
				}
			    }
			}
		    }
		}
		break;
	    }
	    _trackId = trackId;
	    String title = null;
	    int seconds = -1;
	    if (f != null && f.isFile() && f.canRead()) {
		/** We have f. */
		try {
		    final AudioFile audioFile = AudioFileIO.read(f);
		    final Tag tag = audioFile.getTag();
		    title = tag.getFirst(FieldKey.TITLE);
		    seconds = audioFile.getAudioHeader().getTrackLength() + 1;
		} catch (final CannotReadException | IOException | TagException | ReadOnlyFileException
			| InvalidAudioFrameException e) {
		    f = null;
		}
	    }
	    if (f != null && title != null && seconds > _MinSecondsForSong) {
		_f = f;
		_title = title;
		_seconds = seconds;
		return;
	    }
	    _f = null;
	    _title = null;
	    _seconds = -1;
	    return;
	}
    }

    private static void addToAudioInfoList(final PlaylistType playlistType, final File dirOfPlaylistFile,
	    final Element element, final ArrayList<AudioInfo> audioInfoList) {
	/** Is element an AudioInfoElement? */
	final AudioInfo audioInfo = new AudioInfo(element, playlistType, dirOfPlaylistFile);
	if (audioInfo._f != null) {
	    audioInfoList.add(audioInfo);
	}
	final ElementIterator it = new ElementIterator(element);
	while (it.hasNextElement()) {
	    addToAudioInfoList(playlistType, dirOfPlaylistFile, it.nextElement(), audioInfoList);
	}
    }

    private static AudioInfo[] playlistToAudioInfos(final PlaylistType playlistType, final File playlistFile) {
	final File dirOfPlaylistFile = playlistFile.getParentFile();
	try (FileInputStream fis = new FileInputStream(playlistFile)) {
	    final Document document = LsFormatter._DocumentBuilder.parse(fis);
	    final Element root = document.getDocumentElement();
	    final ArrayList<AudioInfo> audioInfoList = new ArrayList<>();
	    addToAudioInfoList(playlistType, dirOfPlaylistFile, root, audioInfoList);
	    final int nAudioInfos = audioInfoList == null ? 0 : audioInfoList.size();
	    final AudioInfo[] audioInfos = audioInfoList.toArray(new AudioInfo[nAudioInfos]);
	    if (playlistType == PlaylistType.XML) {
		final String playlistName = FilenameUtils.getBaseName(playlistFile.getAbsolutePath());
		final TreeMap<Integer, Integer> trackIdToOrder = getXmlTrackIdToOrder(root, playlistName);
		Arrays.parallelSort(audioInfos, new Comparator<AudioInfo>() {

		    @Override
		    public int compare(final AudioInfo audioInfo0, final AudioInfo audioInfo1) {
			final int order0 = trackIdToOrder.get(audioInfo0._trackId);
			final int order1 = trackIdToOrder.get(audioInfo1._trackId);
			return order0 < order1 ? -1 : (order0 > order1 ? 1 : 0);
		    }
		});
	    }
	    return audioInfos;
	} catch (final Exception e) {
	}
	return null;
    }

    private static AudioInfo[][] getAudioInfoGroups(final AudioInfo[] audioInfos) {
	final int nAudioInfos = audioInfos == null ? 0 : audioInfos.length;
	if (nAudioInfos == 0) {
	    return new AudioInfo[0][];
	}
	final ArrayList<AudioInfo[]> listOfGroups = new ArrayList<>();
	int secondsOfCurrentGroup = 0;
	int secondsOfCurrentBigGroup = 0;
	final ArrayList<AudioInfo> currentList = new ArrayList<>();
	for (int k = 0; k < nAudioInfos; ++k) {
	    final AudioInfo audioInfo = audioInfos[k];
	    final int seconds = audioInfo._seconds;
	    if (seconds > _SecondsPerGroup) {
		/** This one is large. Record the current group. */
		if (!currentList.isEmpty()) {
		    listOfGroups.add(currentList.toArray(new AudioInfo[currentList.size()]));
		}
		/** Record this as a singleton group. */
		listOfGroups.add(new AudioInfo[] { audioInfo });
		currentList.clear();
		secondsOfCurrentGroup = 0;
		/** If this one completes a Big-Group, start a new one. */
		secondsOfCurrentBigGroup += seconds;
		if (secondsOfCurrentBigGroup >= _SecondsPerBigGroup) {
		    secondsOfCurrentBigGroup = 0;
		}
	    } else {
		currentList.add(audioInfo);
		secondsOfCurrentGroup += seconds;
		secondsOfCurrentBigGroup += seconds;
		if (secondsOfCurrentGroup >= _SecondsPerGroup || secondsOfCurrentBigGroup >= _SecondsPerBigGroup) {
		    listOfGroups.add(currentList.toArray(new AudioInfo[currentList.size()]));
		    currentList.clear();
		    secondsOfCurrentGroup = 0;
		    if (secondsOfCurrentBigGroup >= _SecondsPerBigGroup) {
			secondsOfCurrentBigGroup = 0;
		    }
		}
	    }
	}
	if (!currentList.isEmpty()) {
	    listOfGroups.add(currentList.toArray(new AudioInfo[currentList.size()]));
	}
	return listOfGroups.toArray(new AudioInfo[listOfGroups.size()][]);
    }

    private static void renameFilesInVoiceRecordingsDir(final File dir) {
	final File[] files = dir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		/**
		 * We're assuming these should all be m4a files. For safety's sake, if we run
		 * into something that is not, we bomb immediately.
		 */
		final String extension = FilenameUtils.getExtension(f.getAbsolutePath());
		if (extension.toLowerCase().contentEquals("m4a") && f.isFile() && f.canRead() && f.canWrite()) {
		    return true;
		}
		System.exit(33);
		return false;
	    }
	});
	final int nFiles = files == null ? 0 : files.length;
	if (nFiles == 0) {
	    System.exit(0);
	}
	Arrays.parallelSort(files, new Comparator<File>() {

	    @Override
	    public int compare(final File f0, final File f1) {
		final long lastModified0 = f0.lastModified();
		final long lastModified1 = f1.lastModified();
		return lastModified0 < lastModified1 ? -1 : (lastModified0 > lastModified1 ? 1 : 0);
	    }
	});
	String commonInitialBaseName = null;
	for (int k = 0; k < nFiles; ++k) {
	    final String baseName = FilenameUtils.getBaseName(files[k].getAbsolutePath());
	    if (commonInitialBaseName == null) {
		commonInitialBaseName = baseName;
		continue;
	    }
	    final int len0 = commonInitialBaseName.length();
	    final int len1 = baseName.length();
	    int len = Math.min(len0, len1);
	    for (int k1 = 0; k1 < len; ++k1) {
		if (commonInitialBaseName.charAt(k1) != baseName.charAt(k1)) {
		    len = k1;
		    break;
		}
	    }
	    commonInitialBaseName = commonInitialBaseName.substring(0, len);
	}
	for (int k = 0; k < nFiles; ++k) {
	    final File f = files[k];
	    try {
		final String filePath = f.getAbsolutePath();
		final String extension = FilenameUtils.getExtension(filePath);
		final String newBase = String.format("%s-%02d", commonInitialBaseName, k);
		final AudioFile audioFile = AudioFileIO.read(f);
		final Tag newTag = audioFile.getTag();
		newTag.setField(FieldKey.TITLE, newBase);
		audioFile.commit();
		final String newName = String.format("%s.%s", newBase, extension);
		f.renameTo(new File(dir, newName));
	    } catch (KeyNotFoundException | CannotReadException | IOException | TagException | ReadOnlyFileException
		    | InvalidAudioFrameException | CannotWriteException e) {
	    }
	}
    }

    private static TreeMap<Integer, Integer> getXmlTrackIdToOrder(final Element root, final String playlistName) {
	TreeMap<Integer, Integer> trackIdToOrder = new TreeMap<>();
	final ElementIterator it0 = new ElementIterator(root);
	while (it0.hasNextElement()) {
	    final Element elt0A = it0.nextElement();
	    if (elt0A.getTagName().equalsIgnoreCase("key")) {
		if (elt0A.getTextContent().equalsIgnoreCase("Name")) {
		    final Element elt0B = it0.nextElement();
		    if (elt0B.getTagName().equalsIgnoreCase("string")) {
			if (elt0B.getTextContent().equalsIgnoreCase(playlistName)) {
			    while (it0.hasNextElement()) {
				final Element elt0C = it0.nextElement();
				if (elt0C.getTagName().equalsIgnoreCase("key")) {
				    if (elt0C.getTextContent().equalsIgnoreCase("Playlist Items")) {
					while (it0.hasNextElement()) {
					    final Element elt0D = it0.nextElement();
					    if (elt0D.getTagName().equalsIgnoreCase("array")) {
						final ElementIterator it1 = new ElementIterator(elt0D);
						while (it1.hasNextElement()) {
						    final Element elt1A = it1.nextElement();
						    if (elt1A.getTagName().equalsIgnoreCase("dict")) {
							final ElementIterator it2 = new ElementIterator(elt1A);
							while (it2.hasNextElement()) {
							    final Element elt2A = it2.nextElement();
							    if (elt2A.getTagName().equalsIgnoreCase("key")) {
								if (elt2A.getTextContent()
									.equalsIgnoreCase("Track ID")) {
								    final Element elt2B = it2.nextElement();
								    if (elt2B.getTagName()
									    .equalsIgnoreCase("integer")) {
									final String trackIdString = elt2B
										.getTextContent();
									try {
									    final int trackId = Integer
										    .parseInt(trackIdString);
									    trackIdToOrder.put(trackId,
										    trackIdToOrder.size());
									} catch (final NumberFormatException e) {
									}
								    }
								}
							    }
							}
						    }
						}
					    }
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	if (!trackIdToOrder.isEmpty()) {
	    return trackIdToOrder;
	}
	final ElementIterator it = new ElementIterator(root);
	while (it.hasNextElement()) {
	    final Element child = it.nextElement();
	    trackIdToOrder = getXmlTrackIdToOrder(child, playlistName);
	    if (trackIdToOrder != null) {
		return trackIdToOrder;
	    }
	}
	return null;
    }

    /**
     * If the 1st argument is a File, and not a directory, the 2nd argument is a
     * decimal threshold of minutes. If the 1st argument is a directory, it is the
     * voice recorders directory, and we are renaming and retitling the m4a files in
     * that directory.
     */
    public static void main(final String[] args) {
	int iArg = 0;
	/** Get the Playlist File and the minimum time for each section. */
	final String filePath = args[iArg++];
	final File file = new File(filePath);
	if (file.isDirectory()) {
	    /** We're renaming files. */
	    final File voiceRecordingsDir = file;
	    renameFilesInVoiceRecordingsDir(voiceRecordingsDir);
	} else {
	    final File playlistFile = file;
	    /** Compute the PlaylistType. */
	    PlaylistType playlistType = null;
	    final String playlistFileExtension = FilenameUtils.getExtension(playlistFile.getName());
	    for (final PlaylistType playlistType0 : _PlaylistTypes) {
		if (playlistFileExtension.toUpperCase().equals(playlistType0.name())) {
		    playlistType = playlistType0;
		    break;
		}
	    }
	    /** Gather the AudioInfos. */
	    final AudioInfo[] audioInfos = playlistToAudioInfos(playlistType, playlistFile);
	    /** Group them. */
	    final AudioInfo[][] groups = getAudioInfoGroups(audioInfos);
	    /** Print out the groups. */
	    final int nGroups = groups.length;
	    int totalSeconds = 0;
	    for (int kBigGroup = 0, kGroup = 0, nSecondsInBigGroup = 0; kGroup < nGroups; ++kGroup) {
		final AudioInfo[] group = groups[kGroup];
		System.out.printf("\n\nGroup %02d/%02d", kGroup, kBigGroup);
		final int nInGroup = group.length;
		for (int k1 = 0, cumSeconds = 0; k1 < nInGroup; ++k1) {
		    final AudioInfo audioInfo = group[k1];
		    final int seconds = audioInfo._seconds;
		    cumSeconds += seconds;
		    totalSeconds += seconds;
		    final String minsString = TimeUtilities.durationInSecondsToMinsSecsString(seconds,
			    /* nDigitsForMinutes= */2);
		    final String cumString = TimeUtilities.durationInSecondsToMinsSecsString(cumSeconds,
			    /* nDigitsForMinutes= */2);
		    final String ttlString = TimeUtilities.durationInSecondsToMinsSecsString(totalSeconds,
			    /* nDigitsForMinutes= */3);
		    System.out.printf("\n%s(%s/%s): %s", minsString, cumString, ttlString, audioInfo._title);
		    if (audioInfo._trackId >= 0) {
			System.out.printf(", TrackId[%d]", audioInfo._trackId);
		    }
		    nSecondsInBigGroup += seconds;
		    if (nSecondsInBigGroup >= _SecondsPerBigGroup) {
			++kBigGroup;
			nSecondsInBigGroup = 0;
		    }
		}
	    }
	}
    }
}
