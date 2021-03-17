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
package org.teamapps.media.exec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public enum ExternalResource {

	YOUTUBE_DOWNLOADER(true, "youtube/youtube-dl", "youtube/youtube-dl", "youtube/youtube-dl.exe"),
	FFMPEG(true, "ffmpeg/ffmpeg-linux", "ffmpeg/ffmpeg-osx", "ffmpeg/ffmpeg.exe"),
	FFPROBE(true, "ffprobe/ffprobe-linux", "ffprobe/ffprobe-osx", "ffprobe/ffprobe.exe");

	private boolean executable;

	private String linuxResource;
	private String osxResource;
	private String windowsResource;

	ExternalResource(boolean executable, String linuxResource, String osxResource, String windowsResource) {
		this.executable = executable;
		this.linuxResource = linuxResource;
		this.osxResource = osxResource;
		this.windowsResource = windowsResource;
	}

	ExternalResource(String resource) {
		this.linuxResource = resource;
		this.osxResource = resource;
		this.windowsResource = resource;
	}

	public InputStream getInputStream() {
		String osName = System.getProperty("os.name").toLowerCase();
		String resource = linuxResource;
		if (osName.contains("mac")) {
			resource = osxResource;
		} else if (osName.contains("nux")) {
			resource = linuxResource;
		} else if (osName.contains("win")) {
			resource = windowsResource;
		}
		return ExternalResource.class.getResourceAsStream(resource);
	}

	public String createBinary(File workingPath) {
		try {
			File binary = getBinaryFile(workingPath);
			if (!binary.exists()) {
				Files.copy(getInputStream(), binary.toPath());
				ensureExecutable(binary);
			}
			return binary.getPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private File getBinaryFile(File workingPath) {
		return new File(workingPath, getName());
	}

	private void ensureExecutable(File binary) throws IOException {
		if (binary.exists() && !System.getProperty("os.name").toLowerCase().contains("win")) {
			String binaryPath = binary.getPath();
			Runtime.getRuntime().exec("chmod +x " + binaryPath);
		}
	}

	private String getName() {
		return this.name().toLowerCase().replace("_", "");
	}

	public boolean isExecutable() {
		return executable;
	}

	public String getLinuxResource() {
		return linuxResource;
	}

	public String getOsxResource() {
		return osxResource;
	}

	public String getWindowsResource() {
		return windowsResource;
	}
}
