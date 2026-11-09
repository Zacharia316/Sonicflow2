package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class StudioDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: StudioDatabase? = null

        fun getInstance(context: Context): StudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudioDatabase::class.java,
                    "studio_daw.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).projectDao().insertAll(getInitialProjects())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInitialProjects(): List<ProjectEntity> {
            val now = System.currentTimeMillis()
            return listOf(
                ProjectEntity(
                    id = "p_continue",
                    title = "Lo-fi Beat",
                    subtitle = "Draft v3",
                    bpm = 98,
                    keySignature = "C Major",
                    durationSeconds = 154,
                    durationFormatted = "2:34",
                    lastEditedDisplay = "Edited 12m ago",
                    lastEditedTimestamp = now - (12 * 60 * 1000),
                    category = "Lo-Fi",
                    lyrics = "driving late, city lights fade\nwaiting for the sunrise on the highway",
                    beatVolume = 0.85f,
                    vocalVolume = 0.80f,
                    adlibVolume = 0.65f,
                    harmonyVolume = 0.72f,
                    beatPan = -0.12f,
                    vocalPan = 0.06f,
                    adlibPan = 0.00f,
                    harmonyPan = 0.03f
                ),
                ProjectEntity(
                    id = "p_summer",
                    title = "Summer Melody",
                    subtitle = "Main Vocal Take",
                    bpm = 128,
                    keySignature = "G Major",
                    durationSeconds = 102,
                    durationFormatted = "1:42",
                    lastEditedDisplay = "Edited 12:34",
                    lastEditedTimestamp = now - (2 * 3600 * 1000),
                    category = "Keys",
                    lyrics = "golden sun in the rearview mirror\nevery beat feels so much clearer"
                ),
                ProjectEntity(
                    id = "p_vocal",
                    title = "Vocal Idea",
                    subtitle = "Acapella Hook",
                    bpm = 95,
                    keySignature = "A Minor",
                    durationSeconds = 48,
                    durationFormatted = "0:48",
                    lastEditedDisplay = "Edited 02:10",
                    lastEditedTimestamp = now - (5 * 3600 * 1000),
                    category = "Vocals",
                    lyrics = "whisper to the microphone\nlet the melody take you home"
                ),
                ProjectEntity(
                    id = "p_chill",
                    title = "Chill Loop",
                    subtitle = "Rhodes + 808",
                    bpm = 80,
                    keySignature = "F Major",
                    durationSeconds = 130,
                    durationFormatted = "2:10",
                    lastEditedDisplay = "Edited 05:08",
                    lastEditedTimestamp = now - (10 * 3600 * 1000),
                    category = "Pads",
                    lyrics = "rain drops tapping on the window sill"
                ),
                ProjectEntity(
                    id = "p_ambient",
                    title = "Ambient Pad",
                    subtitle = "Analog Warmth",
                    bpm = 74,
                    keySignature = "D Minor",
                    durationSeconds = 130,
                    durationFormatted = "2:10",
                    lastEditedDisplay = "2 d ago",
                    lastEditedTimestamp = now - (2 * 86400 * 1000),
                    category = "Pads"
                ),
                ProjectEntity(
                    id = "p_drums",
                    title = "Drums Sketch",
                    subtitle = "Afrobeat 16-bar",
                    bpm = 120,
                    keySignature = "C Major",
                    durationSeconds = 65,
                    durationFormatted = "1:05",
                    lastEditedDisplay = "3 d ago",
                    lastEditedTimestamp = now - (3 * 86400 * 1000),
                    category = "Drums"
                )
            )
        }
    }
}
