package org.teamapps.media.media;

import net.coobird.thumbnailator.Thumbnailator;
import org.apache.commons.io.FileUtils;
import org.teamapps.media.mp3.Mp3Utils;
import org.teamapps.media.pdf.PdfUtils;

import java.io.File;
import java.io.IOException;

public class MediaFileUtils {

	public static File createFilePreviewThumbnail(File file, String fileName) {
		if (file == null || file.length() == 0 || fileName == null) {
			return null;
		}
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
			try {
				File tempFile = File.createTempFile("temp", ".jpg");
				Thumbnailator.createThumbnail(file, tempFile, 200, 200);
				return tempFile;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (fileName.endsWith(".pdf")) {
			return PdfUtils.createPdfThumbnailAsFile(file, 45);
		} else if (fileName.endsWith(".mp3")) {
			return Mp3Utils.getMp3ThumbnailImageAsFile(file);
		}
		//todo: word, excel, pp, openDocument, ...
		return null;
	}

	public static String getFileSize(long length) {
		return FileUtils.byteCountToDisplaySize(length);
	}
}
