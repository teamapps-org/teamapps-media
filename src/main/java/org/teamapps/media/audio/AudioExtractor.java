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

package org.teamapps.media.audio;

import org.teamapps.media.exec.CommandLineExecutor;
import org.teamapps.media.exec.ExternalResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioExtractor extends CommandLineExecutor {

	public AudioExtractor() {
		super(ExternalResource.FFMPEG);
	}

	public AudioExtractor(File workingDirectory) {
		super(workingDirectory, ExternalResource.FFMPEG);
	}

	public File extractPCM(File mediaFile, boolean showLogs) {
		return extractPCM(mediaFile, true, showLogs);
	}

	public File extractPCM(File mediaFile, boolean convertToMono, boolean showLogs) {
		String pcmPath = new File(getWorkingDirectory(), getNextFileName("pcm", ".wav")).getPath();
		StringBuilder argsBuilder = new StringBuilder();
		argsBuilder.append("-i ").append(mediaFile.getPath()).append(" ");
		argsBuilder.append("-ar 8000 ");
		if (convertToMono) {
			argsBuilder.append("-ac 1 ");
		}
		argsBuilder.append("-acodec pcm_s16le ");
		argsBuilder.append(pcmPath).append(" ");
		argsBuilder.append("-hide_banner");
		if (executeCommand(argsBuilder.toString(), 30 * 60, showLogs)) {
			return new File(pcmPath);
		}
		return null;
	}

	public List<File> splitStereoWav(File mediaFile, boolean showLogs) {
		File wav1 = new File(getWorkingDirectory(), getNextFileName("left-channel", ".wav"));
		File wav2 = new File(getWorkingDirectory(), getNextFileName("right-channel", ".wav"));
		StringBuilder argsBuilder = new StringBuilder();
		argsBuilder.append("-i ").append(mediaFile.getPath()).append(" ");
		argsBuilder.append("-ar 8000 ");
		argsBuilder.append("-map_channel 0.0.0  ").append(wav1.getPath()).append(" ");
		argsBuilder.append("-map_channel 0.0.1  ").append(wav2.getPath()).append(" ");
		if (executeCommand(argsBuilder.toString(), 30 * 60, showLogs)) {
			if (wav1.exists() && wav2.exists()) {
				List<File> files = new ArrayList<>();
				files.add(wav1);
				files.add(wav2);
				return files;
			}
		}
		return null;
	}

	public File mergeTwoMonoWavToStereoWav(File wav1, File wav2, boolean showLogs) {
		File stereoWav = new File(getWorkingDirectory(), getNextFileName("stereo", ".wav"));
		if (executeCommand(30 * 60, showLogs, "-i", wav1.getPath(), "-i", wav2.getPath(), "-filter_complex", "[0:a][1:a]amerge=inputs=2[aout]", "-map", "[aout]", stereoWav.getPath())) {
			if (stereoWav.length() > 0) {
				return stereoWav;
			}
		}
		return null;
	}

	public File compressAudio(File mediaFile, int audioBitRateKb, boolean showLogs) {
		File audio = new File(getWorkingDirectory(), getNextFileName("compressed", ".mp3"));
		StringBuilder argsBuilder = new StringBuilder();
		argsBuilder.append("-i ").append(mediaFile.getPath()).append(" ");
		argsBuilder.append("-ab ").append(audioBitRateKb).append(" ");
		argsBuilder.append("-f mp3 ");
		argsBuilder.append(audio.getPath());
		if (executeCommand(argsBuilder.toString(), 30 * 60, showLogs)) {
			return audio;
		}
		return null;
	}

	public String readMediaInfo(File mediaFile) {
		StringBuilder argsBuilder = new StringBuilder();
		argsBuilder.append("-i ").append(mediaFile.getPath()).append(" ");
		argsBuilder.append("-hide_banner");
		StringBuilder info = new StringBuilder();
		StringBuilder error = new StringBuilder();
		if (executeCommand(argsBuilder.toString(), 30, info, error)) {
			return info.toString();
		}
		return null;
	}


}
