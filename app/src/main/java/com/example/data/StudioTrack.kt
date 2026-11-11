package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Represents an audio clip placed on a DAW track timeline.
 */
data class AudioClip(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val filePath: String,
    val startOffsetSeconds: Float = 0f,
    val durationSeconds: Float = 0f,
    val waveformPoints: List<Float> = emptyList()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("filePath", filePath)
            put("startOffsetSeconds", startOffsetSeconds.toDouble())
            put("durationSeconds", durationSeconds.toDouble())
            val wfArr = JSONArray()
            waveformPoints.forEach { wfArr.put(it.toDouble()) }
            put("waveformPoints", wfArr)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): AudioClip {
            val wf = mutableListOf<Float>()
            val wfArr = json.optJSONArray("waveformPoints")
            if (wfArr != null) {
                for (i in 0 until wfArr.length()) {
                    wf.add(wfArr.optDouble(i, 0.5).toFloat())
                }
            }
            return AudioClip(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", "Audio"),
                filePath = json.optString("filePath", ""),
                startOffsetSeconds = json.optDouble("startOffsetSeconds", 0.0).toFloat(),
                durationSeconds = json.optDouble("durationSeconds", 0.0).toFloat(),
                waveformPoints = wf
            )
        }
    }
}

/**
 * Represents a track channel in the multitrack DAW timeline.
 */
data class StudioTrack(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Audio 1",
    val icon: String = "mic",
    val isMuted: Boolean = false,
    val isSoloed: Boolean = false,
    val volume: Float = 1.0f,
    val clips: List<AudioClip> = emptyList()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("icon", icon)
            put("isMuted", isMuted)
            put("isSoloed", isSoloed)
            put("volume", volume.toDouble())
            val clipsArr = JSONArray()
            clips.forEach { clipsArr.put(it.toJson()) }
            put("clips", clipsArr)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): StudioTrack {
            val clipsList = mutableListOf<AudioClip>()
            val clipsArr = json.optJSONArray("clips")
            if (clipsArr != null) {
                for (i in 0 until clipsArr.length()) {
                    clipsArr.optJSONObject(i)?.let { clipsList.add(AudioClip.fromJson(it)) }
                }
            }
            return StudioTrack(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", "Audio 1"),
                icon = json.optString("icon", "mic"),
                isMuted = json.optBoolean("isMuted", false),
                isSoloed = json.optBoolean("isSoloed", false),
                volume = json.optDouble("volume", 1.0).toFloat(),
                clips = clipsList
            )
        }

        fun listToJson(tracks: List<StudioTrack>): String {
            val arr = JSONArray()
            tracks.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun listFromJson(jsonStr: String): List<StudioTrack> {
            if (jsonStr.isBlank()) return emptyList()
            return try {
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<StudioTrack>()
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { list.add(fromJson(it)) }
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
