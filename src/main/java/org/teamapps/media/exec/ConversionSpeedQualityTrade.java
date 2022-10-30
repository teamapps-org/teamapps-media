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

public enum ConversionSpeedQualityTrade {

    ULTRA_FAST("ultrafast"),
    SUPER_FAST("superfast"),
    VERY_FAST("veryfast"),
    FASTER("faster"),
    FAST("fast"),
    MEDIUM("medium"),
    SLOW("slow"),
    SLOWER("slower"),
    VERY_SLOW("veryslow");

    private String ffmpegOption;

    ConversionSpeedQualityTrade(String option) {
        this.ffmpegOption = option;
    }

    public String getFfmpegOption() {
        return ffmpegOption;
    }
}
