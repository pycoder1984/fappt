package com.vidking.firetv.febbox

import android.os.Parcel
import android.os.Parcelable

/**
 * Output of the resolver — everything ExoPlayerActivity needs to start
 * playback. [variants] always has at least one element; sources that don't
 * expose multiple qualities (WebView sniffer) return a single variant with
 * `quality = "auto"`. Febbox returns the full ladder (4K / 1080p / 720p / 360p
 * / AUTO) so the UI can prompt for a quality.
 */
data class ResolvedStream(
    val variants: List<StreamVariant>,
    val referer: String,
    val userAgent: String,
    val subtitles: List<SubtitleTrack>,
    val introStartMs: Long = -1L,
    val introEndMs: Long = -1L
) {
    val primaryUrl: String get() = variants.first().url

    /** True if the source offers more than one selectable video variant. */
    val hasQualityChoices: Boolean get() = variants.count { !it.isAudioOnly } > 1
}

/**
 * One streamable variant. Audio-only variants are kept (Febbox returns 2-4
 * `audio_*` tracks) so they can be filtered out of the quality picker but
 * still surfaced to ExoPlayer if needed.
 */
data class StreamVariant(
    val url: String,
    val quality: String,
    val codec: String = "",
    val isAudioOnly: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        url = parcel.readString().orEmpty(),
        quality = parcel.readString().orEmpty(),
        codec = parcel.readString().orEmpty(),
        isAudioOnly = parcel.readByte() != 0.toByte()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeString(quality)
        dest.writeString(codec)
        dest.writeByte(if (isAudioOnly) 1 else 0)
    }

    /** Coarse rank used to order the quality picker. AUTO is demoted. */
    fun qualityRank(): Int {
        val q = quality.lowercase()
        return when {
            q == "auto" -> 1
            "2160" in q || "4k" in q || "uhd" in q -> 4000
            "1440" in q || "2k" in q -> 2000
            "1080" in q || "fhd" in q -> 1080
            "720" in q || "hd" in q -> 720
            "480" in q -> 480
            "360" in q -> 360
            else -> 0
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<StreamVariant> {
            override fun createFromParcel(parcel: Parcel) = StreamVariant(parcel)
            override fun newArray(size: Int) = arrayOfNulls<StreamVariant>(size)
        }
    }
}

data class SubtitleTrack(
    val url: String,
    val label: String,
    val language: String,
    val mimeType: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeString(label)
        dest.writeString(language)
        dest.writeString(mimeType)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SubtitleTrack> {
            override fun createFromParcel(parcel: Parcel) = SubtitleTrack(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SubtitleTrack>(size)
        }

        fun mimeFromUrlOrFormat(url: String, format: String?): String {
            val lower = (format ?: url).lowercase()
            return when {
                "vtt" in lower -> "text/vtt"
                "ass" in lower || "ssa" in lower -> "text/x-ssa"
                else -> "application/x-subrip"
            }
        }
    }
}
