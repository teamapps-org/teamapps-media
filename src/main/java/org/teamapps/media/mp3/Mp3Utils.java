package org.teamapps.media.mp3;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import org.mp4parser.IsoFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class Mp3Utils {

	public static int getMp4AudioLengthInSecs(File file) {
		try {
			IsoFile isoFile = new IsoFile(file);
			double lengthInSeconds = (double) isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
			return (int) lengthInSeconds;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}


	public static int getMp3DurationInSecs(File mp3) {
		try {
			Mp3File mp3file = new Mp3File(mp3);
			return (int) (mp3file.getLengthInMilliseconds() / 1_000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String getMp3Title(File mp3) {
		try {
			Mp3File mp3file = new Mp3File(mp3);
			if (mp3file.hasId3v2Tag()) {
				ID3v2 id3v2Tag = mp3file.getId3v2Tag();
				return id3v2Tag.getTitle();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, String> getMp3MetaData(File mp3) {
		try {
			Mp3File mp3file = new Mp3File(mp3);
			if (mp3file.hasId3v2Tag()) {
				ID3v2 id3v2Tag = mp3file.getId3v2Tag();
				Map<String, String> map = new HashMap<>();
				map.put("duration", mp3file.getLengthInMilliseconds() + "");
				map.put("bitrate", mp3file.getBitrate() + "");
				map.put("track", id3v2Tag.getTrack());
				map.put("artist", id3v2Tag.getArtist());
				map.put("title", id3v2Tag.getTitle());
				map.put("album", id3v2Tag.getAlbum());
				map.put("year", id3v2Tag.getYear());
				map.put("genreId", "" + id3v2Tag.getGenre());
				map.put("genre", id3v2Tag.getGenreDescription());
				map.put("comment", id3v2Tag.getComment());
				map.put("lyrics", id3v2Tag.getLyrics());
				map.put("composer", id3v2Tag.getComposer());
				map.put("publisher", id3v2Tag.getPublisher());
				map.put("originalArtist", id3v2Tag.getOriginalArtist());
				map.put("albumArtist", id3v2Tag.getAlbumArtist());
				map.put("copyright", id3v2Tag.getCopyright());
				map.put("url", id3v2Tag.getUrl());
				map.put("encoder", id3v2Tag.getEncoder());
				map.put("imageMimeType", id3v2Tag.getAlbumImageMimeType());
				return map;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] getMp3ThumbnailImage(File mp3) {
		try {
			Mp3File mp3file = new Mp3File(mp3);
			if (mp3file.hasId3v2Tag()) {
				ID3v2 id3v2Tag = mp3file.getId3v2Tag();
				return id3v2Tag.getAlbumImage();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File getMp3ThumbnailImageAsFile(File mp3) {
		try {
			byte[] bytes = getMp3ThumbnailImage(mp3);
			if (bytes != null) {
				File file = File.createTempFile("preview", ".jpg");
				Files.copy(new ByteArrayInputStream(bytes), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return file;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
