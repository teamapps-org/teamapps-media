/*-
 * ========================LICENSE_START=================================
 * TeamApps-Media
 * ---
 * Copyright (C) 2016 - 2022 TeamApps.org
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class AudioExtractor {

	private static final CommandLineExecutor commandLineExecutor = new CommandLineExecutor(ExternalResource.FFMPEG);
	private Executor executor;
	private AtomicLong idGenerator = new AtomicLong();

	public AudioExtractor() {
	}

	public AudioExtractor(Executor executor) {
		this.executor = executor;
	}

	public CompletableFuture<File> extractWavePcm(File inputFile, int timeoutSeconds) {
		try {
			File outputFile = createNonExistingTempFile("wav");
			String args = "-i " + inputFile.getPath() + " "
					+ "-ar 8000 "
					+ "-acodec pcm_s16le "
					+ "-nostdin "
					+ "-hide_banner "
					+ outputFile.getPath();
			return commandLineExecutor.executeCommandAsync(args, outputFile, true, timeoutSeconds, executor);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public CompletableFuture<File> extractMonoWavePcm8kHz(File inputFile, int timeoutSeconds) {
		return extractMonoWavePcm8kHz(inputFile, null, timeoutSeconds);
	}

	public CompletableFuture<File> extractMonoWavePcm8kHz(File inputFile, File outputFile, int timeoutSeconds) {
		try {
			File outFile = outputFile != null ? outputFile : createNonExistingTempFile("wav");
			String args = "-i " + inputFile.getPath() + " "
					+ "-ar 8000 "
					+ "-ac 1 "
					+ "-acodec pcm_s16le "
					+ "-nostdin "
					+ "-hide_banner "
					+ outFile.getPath();
			return commandLineExecutor.executeCommandAsync(args, outFile, true, timeoutSeconds, executor);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public File extractMonoWavePcm8kHz(File inputFile, File outputFile, int timeoutSeconds, boolean showLogs) {
		try {
			File outFile = outputFile != null ? outputFile : createNonExistingTempFile("wav");
			String args = "-i " + inputFile.getPath() + " "
					+ "-ar 8000 "
					+ "-ac 1 "
					+ "-acodec pcm_s16le "
					+ "-hide_banner "
					+ "-nostdin "
					+ outFile.getPath();
			commandLineExecutor.executeCommand(args, timeoutSeconds, showLogs);
			return outFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public CompletableFuture<File> extractPcmRaw8kHz(File inputFile, int timeoutSeconds) {
		return extractPcmRaw8kHz(inputFile, null, timeoutSeconds);
	}

	public CompletableFuture<File> extractPcmRaw8kHz(File inputFile, File outputFile, int timeoutSeconds) {
		try {
			File outFile = outputFile != null ? outputFile : createNonExistingTempFile("raw");
			String args = "-i " + inputFile.getPath() + " "
					+ "-ar 8000 "
					+ "-ac 1 "
					+ "-f s16le "
					+ "-acodec pcm_s16le "
					+ "-nostdin "
					+ "-hide_banner "
					+ outFile.getPath();
			return commandLineExecutor.executeCommandAsync(args, outFile, true, timeoutSeconds, executor);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public CompletableFuture<File> convertToMp3(File inputFile, File outputFile, int kiloBit, int timeoutSeconds) {
		try {
			File outFile = outputFile != null ? outputFile : createNonExistingTempFile("mp3");
			String args = "-i " + inputFile.getPath() + " "
					+ "-vn "
					+ "-b:a " + kiloBit + "k "
					+ "-nostdin "
					+ "-hide_banner "
					+ outFile.getPath();
			return commandLineExecutor.executeCommandAsync(args, outFile, true, timeoutSeconds, executor);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public File extractPcmRaw8kHz(File inputFile, File outputFile, int timeoutSeconds, boolean showLogs) {
		try {
			File outFile = outputFile != null ? outputFile : createNonExistingTempFile("raw");
			String args = "-i " + inputFile.getPath() + " "
					+ "-ar 8000 "
					+ "-ac 1 "
					+ "-f s16le "
					+ "-acodec pcm_s16le "
					+ "-nostdin "
					+ "-hide_banner "
					+ outFile.getPath();
			commandLineExecutor.executeCommand(args, timeoutSeconds, showLogs);
			return outFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public File concatSameFormatMediaFiles(List<File> inputFiles, File outputFile, int timeoutSeconds, boolean showLogs) {
		try {
			File concatFile = Files.createTempFile("temp-concat-list", ".txt").toFile();
			byte[] concatData = inputFiles.stream().map(f -> "file '" + f.getPath() + "'").collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8);
			Files.write(concatFile.toPath(), concatData);
			String args = "-safe 0 -f concat -i " + concatFile.getPath() + " -c copy " + outputFile.getPath();
			commandLineExecutor.executeCommand(args, timeoutSeconds, showLogs);
			return outputFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private File createNonExistingTempFile(String fileExtension) throws IOException {
		File parentFile = File.createTempFile("temp", "tmp").getParentFile();
		File tempFile = new File(parentFile,"converted-" + System.currentTimeMillis() + "-" + idGenerator.incrementAndGet() + "." + fileExtension);
		parentFile.delete();
		return tempFile;
	}

	public String readMediaInfo(File mediaFile) {
		StringBuilder argsBuilder = new StringBuilder();
		argsBuilder.append("-i ").append(mediaFile.getPath()).append(" ");
		argsBuilder.append("-hide_banner");
		StringBuilder info = new StringBuilder();
		StringBuilder error = new StringBuilder();
		if (commandLineExecutor.executeCommand(argsBuilder.toString(), 30, info, error)) {
			return info.toString();
		}
		return null;
	}


}
