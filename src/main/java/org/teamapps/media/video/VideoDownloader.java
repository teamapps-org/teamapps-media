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

package org.teamapps.media.video;


import org.teamapps.media.exec.CommandLineExecutor;
import org.teamapps.media.exec.ExternalResource;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

public class VideoDownloader {

	private static final CommandLineExecutor commandLineExecutor = new CommandLineExecutor(ExternalResource.YOUTUBE_DOWNLOADER);
	private static AtomicLong ID_GENERATOR = new AtomicLong(0);
	private Executor executor;


	public VideoDownloader() {
	}

	public VideoDownloader(Executor executor) {
		this.executor = executor;
	}

	public CompletableFuture<File> downloadVideo(String url, int timeoutSeconds) {
		return downloadVideo(url, null, 0, timeoutSeconds);
	}

	public CompletableFuture<File> downloadVideo(String url, String proxyHost, int proxyPort, int timeoutSeconds) {
		try {
			String id = getNexId();
			File path = File.createTempFile("temp", "temp").getParentFile();
			String args = createProxyArgs(proxyHost, proxyPort)
					+ "--output " + path.getPath() + "/video-" + id + ".%(ext)s "
					+ "--restrict-filenames "
					+ url;
			return commandLineExecutor.executeCommandAsync(args, timeoutSeconds, executor)
					.thenApply(aVoid -> findFile(path, id));
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	private String createProxyArgs(String proxyHost, int proxyPort) {
		return proxyHost != null ? "--proxy " + proxyHost + ":" + proxyPort + " " : "";
	}

	private String getNexId() {
		return "IDx" + System.currentTimeMillis() + "-" + ID_GENERATOR.incrementAndGet() + "xv";
	}

	private File findFile(File path, String partOfName) {
		return Arrays.stream(path.listFiles())
				.filter(file -> file.getName().contains(partOfName))
				.findAny()
				.orElse(null);
	}

}
