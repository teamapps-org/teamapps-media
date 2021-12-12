/*-
 * ========================LICENSE_START=================================
 * TeamApps-Media
 * ---
 * Copyright (C) 2016 - 2021 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
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
