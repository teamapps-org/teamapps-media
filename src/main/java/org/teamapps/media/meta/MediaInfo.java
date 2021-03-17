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
package org.teamapps.media.meta;

public class MediaInfo {

    private String fileName;
    private String format;
    private String formatDescription;
    private int durationInSecs;
    private int timestamp;

    private boolean containsAudio;
    private boolean stereoAudio;
    private int audioSamplingRate;
    private String audioCodec;
    private String audioCodecDescription;
    private int audioBitRate;

    private boolean containsVideo;
    private String videoCodec;
    private String videoCodecDescription;
    private int videoWidth;
    private int videoHeight;
    private int VideoBitRate;

    private boolean containsImage;
    private int imageWidth;
    private int imageHeight;
    private double imageAltitude;
    private double imageLatitude;
    private double imageLongitude;

    public MediaInfo() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormatDescription() {
        return formatDescription;
    }

    public void setFormatDescription(String formatDescription) {
        this.formatDescription = formatDescription;
    }

    public int getDurationInSecs() {
        return durationInSecs;
    }

    public void setDurationInSecs(int durationInSecs) {
        this.durationInSecs = durationInSecs;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isContainsAudio() {
        return containsAudio;
    }

    public void setContainsAudio(boolean containsAudio) {
        this.containsAudio = containsAudio;
    }

    public boolean isStereoAudio() {
        return stereoAudio;
    }

    public void setStereoAudio(boolean stereoAudio) {
        this.stereoAudio = stereoAudio;
    }

    public int getAudioSamplingRate() {
        return audioSamplingRate;
    }

    public void setAudioSamplingRate(int audioSamplingRate) {
        this.audioSamplingRate = audioSamplingRate;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public String getAudioCodecDescription() {
        return audioCodecDescription;
    }

    public void setAudioCodecDescription(String audioCodecDescription) {
        this.audioCodecDescription = audioCodecDescription;
    }

    public int getAudioBitRate() {
        return audioBitRate;
    }

    public void setAudioBitRate(int audioBitRate) {
        this.audioBitRate = audioBitRate;
    }

    public boolean isContainsVideo() {
        return containsVideo;
    }

    public void setContainsVideo(boolean containsVideo) {
        this.containsVideo = containsVideo;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public String getVideoCodecDescription() {
        return videoCodecDescription;
    }

    public void setVideoCodecDescription(String videoCodecDescription) {
        this.videoCodecDescription = videoCodecDescription;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getVideoBitRate() {
        return VideoBitRate;
    }

    public void setVideoBitRate(int videoBitRate) {
        VideoBitRate = videoBitRate;
    }

    public boolean isContainsImage() {
        return containsImage;
    }

    public void setContainsImage(boolean containsImage) {
        this.containsImage = containsImage;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public double getImageAltitude() {
        return imageAltitude;
    }

    public void setImageAltitude(double imageAltitude) {
        this.imageAltitude = imageAltitude;
    }

    public double getImageLatitude() {
        return imageLatitude;
    }

    public void setImageLatitude(double imageLatitude) {
        this.imageLatitude = imageLatitude;
    }

    public double getImageLongitude() {
        return imageLongitude;
    }

    public void setImageLongitude(double imageLongitude) {
        this.imageLongitude = imageLongitude;
    }

    @Override
    public String toString() {
        return "MediaInfo{" +
                "fileName='" + fileName + '\'' +
                ", format='" + format + '\'' +
                ", formatDescription='" + formatDescription + '\'' +
                ", durationInSecs=" + durationInSecs +
                ", containsAudio=" + containsAudio +
                ", stereoAudio=" + stereoAudio +
                ", audioSamplingRate=" + audioSamplingRate +
                ", audioCodec='" + audioCodec + '\'' +
                ", audioCodecDescription='" + audioCodecDescription + '\'' +
                ", audioBitRate=" + audioBitRate +
                ", containsVideo=" + containsVideo +
                ", videoCodec='" + videoCodec + '\'' +
                ", videoCodecDescription='" + videoCodecDescription + '\'' +
                ", videoWidth=" + videoWidth +
                ", videoHeight=" + videoHeight +
                ", VideoBitRate=" + VideoBitRate +
                '}';
    }
}
