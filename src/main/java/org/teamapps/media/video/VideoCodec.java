/*-
 * ========================LICENSE_START=================================
 * TeamApps-Media
 * ---
 * Copyright (C) 2016 - 2024 TeamApps.org
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

public enum VideoCodec {

	H261("h261", "H.261"),
	H263("h263", "H.263 / H.263-1996, H.263+ / H.263-1998 / H.263 version 2"),
	H263P("h263p", "H.263+ / H.263-1998 / H.263 version 2"),
	H264("h264", "H.264 / AVC / MPEG-4 AVC / MPEG-4 part 10 (decoders: h264 h264_vda ) (encoders: libx264 libx264rgb )"),
	HEVC("hevc", "H.265 / HEVC (High Efficiency Video Coding) (encoders: libx265 )"),
	MJPEG("mjpeg", "Motion JPEG"),
	MPEG1VIDEO("mpeg1video", "MPEG-1 video"),
	MPEG2VIDEO("mpeg2video", "MPEG-2 video (decoders: mpeg2video mpegvideo )"),
	MPEG4("mpeg4", "MPEG-4 part 2 (encoders: mpeg4 libxvid )"),
	MSMPEG4V2("msmpeg4v2", "MPEG-4 part 2 Microsoft variant version 2"),
	MSMPEG4V3("msmpeg4v3", "MPEG-4 part 2 Microsoft variant version 3 (decoders: msmpeg4 ) (encoders: msmpeg4 )"),
	MSVIDEO1("msvideo1", "Microsoft Video 1"),
	RAWVIDEO("rawvideo", "raw video"),
	THEORA("theora", "Theora (encoders: libtheora )"),
	VP8("vp8", "On2 VP8 (decoders: vp8 libvpx ) (encoders: libvpx )"),
	VP9("vp9", "Google VP9 (decoders: vp9 libvpx-vp9 ) (encoders: libvpx-vp9 )"),
	WMV1("wmv1", "Windows Media Video 7"),
	WMV2("wmv2", "Windows Media Video 8");

	private String ffmpegName;
	private String description;

	VideoCodec(String ffmpegIdentifier, String description) {
		this.ffmpegName = ffmpegIdentifier;
		this.description = description;
	}

	public String getFfmpegName() {
		return ffmpegName;
	}

	public String getDescription() {
		return description;
	}
}
