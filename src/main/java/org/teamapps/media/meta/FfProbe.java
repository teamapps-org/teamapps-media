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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import org.teamapps.media.exec.CommandLineExecutor;
import org.teamapps.media.exec.ExternalResource;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

public class FfProbe {

    private static final CommandLineExecutor commandLineExecutor = new CommandLineExecutor(ExternalResource.FFPROBE);
    private final ObjectMapper mapper = new ObjectMapper();
    private final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);

    private static AtomicLong ID_GENERATOR = new AtomicLong(0);
    private Executor executor;


    public FfProbe() {
    }

    public FfProbe(Executor executor) {
        this.executor = executor;
    }

    public Map<String, Object> readMediaInfoMap(File mediaFile) {
        StringBuilder argsBuilder = new StringBuilder();
        argsBuilder.append("-v quiet -print_format json -show_format -show_streams ").append(mediaFile.getPath()).append(" ");
        StringBuilder info = new StringBuilder();
        StringBuilder error = new StringBuilder();
        if (commandLineExecutor.executeCommand(argsBuilder.toString(), 30, info, error)) {
            String json = info.toString();
            System.out.println(json);
            try {
                if (!json.isBlank()) {
                    return mapper.readValue(json, type);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public MediaInfo readMediaInfo(File file) {
        Map<String, Object> map = readMediaInfoMap(file);
        if (map == null) {
            return null;
        }
        MediaInfo mediaInfo = new MediaInfo();
        if (map.containsKey("format")) {
            Map<String, Object> formatMap = (Map<String, Object>) map.get("format");
            mediaInfo.setFileName(getString("filename", formatMap));
            int duration = (int) (parseDouble("duration", formatMap));
            mediaInfo.setDurationInSecs(duration);
            mediaInfo.setFormat(getString("format_name", formatMap));
            mediaInfo.setFormatDescription(getString("format_long_name", formatMap));
        }
        if (map.containsKey("streams")) {
            List<Object> streams = (List<Object>) map.get("streams");
            Map<String, Object> audioStream = getFirstMediaStream(streams, true);
            if (audioStream != null) {
                mediaInfo.setContainsAudio(true);
                mediaInfo.setAudioCodec(getString("codec_name", audioStream));
                mediaInfo.setAudioCodecDescription(getString("codec_long_name", audioStream));
                mediaInfo.setAudioSamplingRate(parseInt("sample_rate", audioStream));
                mediaInfo.setAudioBitRate(parseInt("bit_rate", audioStream));
                mediaInfo.setStereoAudio(parseInt("channels", audioStream) == 2);
            }
            Map<String, Object> videoStream = getFirstMediaStream(streams, false);
            if (videoStream != null) {
                mediaInfo.setContainsVideo(true);
                mediaInfo.setVideoCodec(getString("codec_name", videoStream));
                mediaInfo.setVideoCodecDescription(getString("codec_long_name", videoStream));
                mediaInfo.setVideoBitRate(parseInt("bit_rate", videoStream));
                mediaInfo.setVideoWidth(parseInt("width", videoStream));
                mediaInfo.setVideoHeight(parseInt("height", videoStream));
            }
        }
        return mediaInfo;
    }

    private String getString(String key, Map<String, Object> map) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof String) {
                return (String) value;
            }
        }
        return null;
    }

    private int parseInt(String key, Map<String, Object> map) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof Integer) {
                return (int) value;
            }
            if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        }
        return 0;
    }

    private double parseDouble(String key, Map<String, Object> map) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof Double) {
                return (double) value;
            }
            if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
        }
        return 0;
    }

    private Map<String, Object> getFirstMediaStream(List<Object> streams, boolean audio) {
        for (Object stream : streams) {
            Map<String, Object> streamMap = (Map<String, Object>) stream;
            if (streamMap.containsKey("codec_type")) {
                if (audio && streamMap.get("codec_type").equals("audio")) {
                    return streamMap;
                } else if (!audio && streamMap.get("codec_type").equals("video")) {
                    return streamMap;
                }
            }
        }
        return null;
    }
}
