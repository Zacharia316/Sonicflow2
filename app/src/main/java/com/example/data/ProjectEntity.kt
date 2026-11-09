package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String = "Draft v1",
    val bpm: Int = 140,
    val keySignature: String = "C Major",
    val durationSeconds: Int = 94,
    val durationFormatted: String = "1:34",
    val lastEditedDisplay: String = "Just now",
    val lastEditedTimestamp: Long = System.currentTimeMillis(),
    val category: String = "All", // "Lo-Fi", "Vocals", "Pads", "Drums", "Keys"
    val beatStemPath: String? = null,
    val vocalStemPath: String? = null,
    val adlibStemPath: String? = null,
    val harmonyStemPath: String? = null,
    val beatVolume: Float = 0.85f,   // -3.2dB
    val vocalVolume: Float = 0.80f,  // -5.1dB
    val adlibVolume: Float = 0.65f,  // -8.0dB
    val harmonyVolume: Float = 0.72f,// -6.4dB
    val beatPan: Float = -0.12f,
    val vocalPan: Float = 0.06f,
    val adlibPan: Float = 0.0f,
    val harmonyPan: Float = 0.03f,
    val lyrics: String = "driving late, city lights fade\nwaiting for the sunrise on the highway",
    val notesData: String = "" // Simple serialized note grid
)
