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

package org.teamapps.media.exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class CommandLineExecutor {

	private static final Logger logger = LoggerFactory.getLogger(CommandLineExecutor.class);

	private AtomicLong longId = new AtomicLong();
	private File workingDirectory;
	private String binaryPath;
	private String name;

	public CommandLineExecutor(ExternalResource resource) {
		this(null, resource);
	}

	public CommandLineExecutor(File workingDirectory, ExternalResource resource) {
		this.workingDirectory = createWorkingDir(workingDirectory);
		this.binaryPath = resource.createBinary(this.workingDirectory);
		name = getClass().getSimpleName();
	}

	public CommandLineExecutor(String binaryPath) {
		this.binaryPath = binaryPath;
		workingDirectory = createWorkingDir(null);
		name = getClass().getSimpleName();
	}

	public boolean executeCommand(String commandArgs, int maxRuntimeInSeconds, boolean showLogs) {
		try {
			Process process = Runtime.getRuntime().exec(binaryPath + " " + commandArgs);
			if (showLogs) {
				showLogs(process.getErrorStream(), true);
				showLogs(process.getInputStream(), false);
			}
			if (!process.waitFor(maxRuntimeInSeconds, TimeUnit.SECONDS)) {
				process.destroy();
				process.destroyForcibly();
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean executeCommand(int maxRuntimeInSeconds, boolean showLogs, String... commandArgs) {
		try {
			String[] cmdArray = new String[commandArgs.length + 1];
			cmdArray[0] = binaryPath;
			for (int i = 1; i <= commandArgs.length; i++) {
				cmdArray[i] = commandArgs[i - 1];
			}
			Process process = Runtime.getRuntime().exec(cmdArray);
			if (showLogs) {
				showLogs(process.getErrorStream(), true);
				showLogs(process.getInputStream(), false);
			}
			if (!process.waitFor(maxRuntimeInSeconds, TimeUnit.SECONDS)) {
				process.destroy();
				process.destroyForcibly();
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean executeCommand(String commandArgs, int maxRuntimeInSeconds, StringBuilder info, StringBuilder error) {
		try {
			Process process = Runtime.getRuntime().exec(binaryPath + " " + commandArgs);
			readLogs(process.getInputStream(), info);
			readLogs(process.getErrorStream(), error);

			if (!process.waitFor(maxRuntimeInSeconds, TimeUnit.SECONDS)) {
				process.destroy();
				process.destroyForcibly();
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public CompletableFuture<Void> executeCommandAsync(String commandArgs, int maxRuntimeInSeconds) {
		return executeCommandAsync(commandArgs, maxRuntimeInSeconds, null);
	}

	public CompletableFuture<Void> executeCommandAsync(String commandArgs, int maxRuntimeInSeconds, Executor executor) {
		Runnable runnable = () -> {
			StringBuilder logsStringBuilder = new StringBuilder();
			try {
				String command = binaryPath + " " + commandArgs;
				Process process = Runtime.getRuntime().exec(command);
				readLogs(process.getErrorStream(), logsStringBuilder);
				if (!process.waitFor(maxRuntimeInSeconds, TimeUnit.SECONDS)) {
					process.destroy();
					process.destroyForcibly();
					throw new TimeoutException(logsStringBuilder.toString());
				}
			} catch (Exception e) {
				throw new RuntimeException(logsStringBuilder.toString(), e);
			}
		};
		if (executor != null) {
			return CompletableFuture.runAsync(runnable, executor);
		} else {
			return CompletableFuture.runAsync(runnable);
		}
	}

	public CompletableFuture<File> executeCommandAsync(String commandArgs, File expectedResultFile, boolean expectFileWithContent, int maxRuntimeInSeconds, Executor executor) {
		StringBuilder logsBuilder = new StringBuilder();
		StringBuilder errorLogsBuilder = new StringBuilder();
		Function<Void, File> resultFileFunction = aVoid -> {
			if (!expectedResultFile.exists() || (expectFileWithContent && expectedResultFile.length() == 0)) {
				throw new RuntimeException("Error: missing result file with executable args:" + commandArgs + "\n" + logsBuilder.toString() + errorLogsBuilder.toString());
			}
			return expectedResultFile;
		};
		Runnable runnable = createExecutionRunnable(commandArgs, maxRuntimeInSeconds, logsBuilder, errorLogsBuilder);
		return executor != null ? CompletableFuture.runAsync(runnable, executor).thenApply(resultFileFunction) : CompletableFuture.runAsync(runnable).thenApply(resultFileFunction);
	}

	public CompletableFuture<File> executeCommandAsync(String commandArgs, File path, String partOfResultFileName, boolean expectFileWithContent, int maxRuntimeInSeconds, Executor executor) {
		StringBuilder logsBuilder = new StringBuilder();
		StringBuilder errorLogsBuilder = new StringBuilder();
		Function<Void, File> resultFileFunction = aVoid -> {
			File expectedResultFile = Arrays.stream(path.listFiles())
					.filter(file -> file.getName().contains(partOfResultFileName))
					.findAny()
					.orElse(null);
			if (expectedResultFile == null || !expectedResultFile.exists() || (expectFileWithContent && expectedResultFile.length() == 0)) {
				throw new RuntimeException("Error: missing result file with executable args:" + commandArgs + "\n" + logsBuilder.toString() + errorLogsBuilder.toString());
			}
			return expectedResultFile;
		};
		Runnable runnable = createExecutionRunnable(commandArgs, maxRuntimeInSeconds, logsBuilder, errorLogsBuilder);
		return executor != null ? CompletableFuture.runAsync(runnable, executor).thenApply(resultFileFunction) : CompletableFuture.runAsync(runnable).thenApply(resultFileFunction);
	}

	private Runnable createExecutionRunnable(String commandArgs, int maxRuntimeInSeconds, StringBuilder logsBuilder, StringBuilder errorLogsBuilder) {
		return () -> {
			try {
				String command = binaryPath + " " + commandArgs;
				Process process = Runtime.getRuntime().exec(command);
				readLogs(process.getInputStream(), logsBuilder);
				readLogs(process.getErrorStream(), errorLogsBuilder);
				if (!process.waitFor(maxRuntimeInSeconds, TimeUnit.SECONDS)) {
					process.destroy();
					process.destroyForcibly();
					throw new TimeoutException(errorLogsBuilder.toString());
				}
			} catch (Exception e) {
				throw new RuntimeException(errorLogsBuilder.toString(), e);
			}
		};
	}

	private void readLogs(final InputStream is, StringBuilder sb) {
		Thread thread = new Thread(() -> {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(true);
		thread.setName("cli-output-reader");
		thread.start();
	}

	public void showLogs(final InputStream is, boolean errorLog) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					String line;
					while ((line = reader.readLine()) != null) {
						if (errorLog) {
							logger.warn(name + ":" + line);
						} else {
							logger.info(name + ":" + line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		thread.setDaemon(true);
		thread.setName("cli-log-reader");
		thread.start();
	}

	public File createWorkingDir(File workingDirectory) {
		if (workingDirectory == null || !workingDirectory.getParentFile().exists()) {
			try {
				return File.createTempFile("temp", "tmp").getParentFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		if (!workingDirectory.exists()) {
			workingDirectory.mkdir();
		}
		return workingDirectory;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public String getWorkingPath() {
		return workingDirectory.getPath();
	}

	public String getBinaryPath() {
		return binaryPath;
	}

	public String getNextFileName(String name, String suffix) {
		return name + System.currentTimeMillis() + "-" + longId.incrementAndGet() + suffix;
	}
}
