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

public enum AudioCodec {

	AAC("aac", "AAC (Advanced Audio Coding) (decoders: aac aac_fixed )"),
	ADPCM_G722("adpcm_g722", "G.722 ADPCM (decoders: g722 ) (encoders: g722 )"),
	ADPCM_G726("adpcm_g726", "G.726 ADPCM (decoders: g726 ) (encoders: g726 )"),
	FLAC("flac", "FLAC (Free Lossless Audio Codec)"),
	G723_1("g723_1", "G.723.1"),
	GSM("gsm", "GSM (decoders: gsm libgsm ) (encoders: libgsm )"),
	GSM_MS("gsm_ms", "GSM Microsoft variant (decoders: gsm_ms libgsm_ms ) (encoders: libgsm_ms )"),
	MP2("mp2", "MP2 (MPEG audio layer 2) (decoders: mp2 mp2float ) (encoders: mp2 mp2fixed )"),
	MP3("mp3", "MP3 (MPEG audio layer 3) (decoders: mp3 mp3float ) (encoders: libmp3lame )"),
	NELLYMOSER("nellymoser", "Nellymoser Asao"),
	OPUS("opus", "Opus (Opus Interactive Audio Codec) (decoders: opus libopus ) (encoders: libopus )"),
	PCM_ALAW("pcm_alaw", "PCM A-law / G.711 A-law"),
	PCM_MULAW("pcm_mulaw", "PCM mu-law / G.711 mu-law"),
	SPEEX("speex", "Speex (decoders: libspeex ) (encoders: libspeex )"),
	TTA("tta", "TTA (True Audio)"),
	VORBIS("vorbis", "Vorbis (decoders: vorbis libvorbis ) (encoders: vorbis libvorbis )"),
	WAVPACK("wavpack", "WavPack (encoders: wavpack libwavpack )"),
	WMAV1("wmav1", "Windows Media Audio 1"),
	WMAV2("wmav2", "Windows Media Audio 2");

	private final String ffmpegName;
	private final String description;

	AudioCodec(String ffmpegName, String description) {
		this.ffmpegName = ffmpegName;
		this.description = description;
	}

	public String getFfmpegName() {
		return ffmpegName;
	}

	public String getDescription() {
		return description;
	}
}
