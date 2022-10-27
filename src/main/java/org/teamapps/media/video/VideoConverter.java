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
package org.teamapps.media.video;

import org.teamapps.media.ContainerFormat;
import org.teamapps.media.audio.AudioCodec;
import org.teamapps.media.exec.ConversionSpeedQualityTrade;
import org.teamapps.media.exec.CommandLineExecutor;
import org.teamapps.media.exec.ExternalResource;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class VideoConverter {

	private static final CommandLineExecutor commandLineExecutor = new CommandLineExecutor(ExternalResource.FFMPEG);
	private Executor executor;

	public VideoConverter() {
	}

	public VideoConverter(Executor executor) {
		this.executor = executor;
	}

	public CompletableFuture<File> convertVideo(File inputFile, ContainerFormat outputFormat, AudioCodec audioCodec, int audioBitrate, VideoCodec videoCodec, int videoBitrate,
												ConversionSpeedQualityTrade speedQualityTrade, int timeoutSeconds) {
		try {
			File outputFile = File.createTempFile("converted-video-", "." + outputFormat.getFileSuffix());
			String args = "-i " + inputFile.getPath() + " "
					+ "-c:a " + audioCodec.getFfmpegName() + " "
					+ "-b:a " + audioBitrate + " "
					+ "-c:v " + videoCodec.getFfmpegName() + " "
					+ "-b:v " + videoBitrate + " "
					+ "-preset " + speedQualityTrade.getFfmpegOption() + " "
					+ "-y "
					+ "-hide_banner "
					+ outputFile;
			return commandLineExecutor.executeCommandAsync(args, timeoutSeconds, executor)
					.thenApply(aVoid -> outputFile);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public CompletableFuture<File> createVideoThumbnail(File inputFile, int minutes, int seconds, int timeoutSeconds) {
		try {
			File temp = Files.createTempDirectory("temp").toFile();
			File newInput = new File(temp, "video-copy" + System.currentTimeMillis() + ".mp4");
			Files.copy(inputFile.toPath(), newInput.toPath());
			File outputFile = new File(temp, "video" + System.currentTimeMillis() + ".jpg");
			String args = "-ss 00:" + formatTimeValue(minutes) + ":" + formatTimeValue(seconds) + " "
					+ "-i " + newInput.getPath() + " "
					+ "-vframes 1 " + outputFile.getPath() + " "
					+ "-hide_banner ";
			return commandLineExecutor.executeCommandAsync(args, timeoutSeconds, executor)
					.thenApply(aVoid -> outputFile);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	private String formatTimeValue(int value) {
		return value < 9 ? "0" + value : "" + value;
	}
	     

}
