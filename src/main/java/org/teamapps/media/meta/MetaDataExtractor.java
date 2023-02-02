/*-
 * ========================LICENSE_START=================================
 * TeamApps-Media
 * ---
 * Copyright (C) 2016 - 2023 TeamApps.org
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
package org.teamapps.media.meta;


import org.teamapps.media.image.GeoTag;
import org.teamapps.media.image.JpegGeoTagReader;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MetaDataExtractor {

    public static final Set<String> AUDIO_VIDEO_FILE_EXTENSIONS = new HashSet<>(Arrays.asList("mp3", "aiff", "flac", "mp4", "wav", "webm", "mpg", "mp2", "mpeg", "mpe", "mpv", "ogg", "m4p", "m4v", "avi", "wmv", "mov", "qt", "flv", "swf", "mkv", "vob", "rmvb", "3gp", "3g2", "f4a"));

    private final FfProbe ffProbe;
    private final JpegGeoTagReader jpegGeoTagReader;

    public MetaDataExtractor() {
        ffProbe = new FfProbe();
        jpegGeoTagReader = new JpegGeoTagReader();
    }

    public MediaInfo readFile(File file) {
        if (file == null || !file.isDirectory() && file.length() == 0) {
            return null;
        }
        String fileNameExtension = getFileNameExtension(file);
        if (AUDIO_VIDEO_FILE_EXTENSIONS.contains(fileNameExtension)) {
            return ffProbe.readMediaInfo(file);
        }
        if (fileNameExtension != null && (fileNameExtension.equals("jpg") || fileNameExtension.equals("jpeg"))) {
            GeoTag geoTag = jpegGeoTagReader.readMetadataSave(file);
            if (geoTag != null && geoTag.getLatitude() != 0 && geoTag.getLongitude() != 0) {
                MediaInfo mediaInfo = new MediaInfo();
                mediaInfo.setFileName(file.getName());
                mediaInfo.setContainsImage(true);
                mediaInfo.setImageLatitude(geoTag.getLatitude());
                mediaInfo.setImageLongitude(geoTag.getLongitude());
                mediaInfo.setImageAltitude(geoTag.getAltitude());
                mediaInfo.setTimestamp(geoTag.getTimestampAsEpochSeconds());
                return mediaInfo;
            }
        }
        return null;
    }


    public static String getFileNameExtension(File file) {
        int pos = file.getName().lastIndexOf('.');
        if (pos <= 0) {
            return null;
        } else {
            return file.getName().substring(pos + 1).toLowerCase();
        }
    }
}
