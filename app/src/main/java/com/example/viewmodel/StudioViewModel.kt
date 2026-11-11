package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioExporter
import com.example.audio.AudioPlayer
import com.example.audio.AudioRecorder
import com.example.audio.AudioUtils
import com.example.audio.DrumType
import com.example.data.AudioClip
import com.example.data.ProjectEntity
import com.example.data.StudioDatabase
import com.example.data.StudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

enum class StudioScreen {
    HOME,
    PROJECTS,
    MIXER,
    PIANO_ROLL,
    INSTRUMENTS,
    DRUM_PADS,
    RECORDING_SESSION,
    VOCAL_FX,
    AUTO_TUNE,
    EXPORT_SHARE,
    OFFLINE_GUIDE
}

data class PianoRollNote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val midiNote: Int,    // 48 (C3) to 72 (C5)
    val startStep: Int,   // 0 to 31
    val lengthSteps: Int = 2, // 1 to 8
    val velocity: Float = 0.85f
)

data class StatusNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = StudioDatabase.getInstance(application)
    val audioPlayer = AudioPlayer(application)
    val audioRecorder = AudioRecorder(application)

    // Central status & error shower
    private val _statusNotification = MutableStateFlow<StatusNotification?>(null)
    val statusNotification: StateFlow<StatusNotification?> = _statusNotification.asStateFlow()
    private var notificationJob: Job? = null

    // Navigation state
    private val _currentScreen = MutableStateFlow(StudioScreen.HOME)
    val currentScreen: StateFlow<StudioScreen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<StudioScreen>()

    // Projects list from Room database
    val projects = db.projectDao().getAllProjects()

    // Default template used when no projects exist yet
    private val defaultProjectTemplate = ProjectEntity(
        id = "proj_default",
        title = "New Project",
        subtitle = "Draft",
        bpm = 120,
        keySignature = "C Major",
        durationSeconds = 60,
        durationFormatted = "1:00",
        lastEditedDisplay = "Just now",
        lastEditedTimestamp = System.currentTimeMillis(),
        category = "All",
        lyrics = ""
    )

    // Active project state
    private val _activeProject = MutableStateFlow(defaultProjectTemplate)
    val activeProject: StateFlow<ProjectEntity> = _activeProject.asStateFlow()

    // Multitrack DAW Timeline State
    private val _tracks = MutableStateFlow<List<StudioTrack>>(listOf(StudioTrack(name = "Audio 1")))
    val tracks: StateFlow<List<StudioTrack>> = _tracks.asStateFlow()

    private val _selectedTrackId = MutableStateFlow<String?>(null)
    val selectedTrackId: StateFlow<String?> = _selectedTrackId.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _playheadSeconds = MutableStateFlow(0f)
    val playheadSeconds: StateFlow<Float> = _playheadSeconds.asStateFlow()

    private val _isRecordingMultitrack = MutableStateFlow(false)
    val isRecordingMultitrack: StateFlow<Boolean> = _isRecordingMultitrack.asStateFlow()

    private val activePlayers = mutableListOf<MediaPlayer>()
    private var multitrackJob: Job? = null
    private var multitrackRecordStartTime = 0f

    // Transport playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0.14f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _currentStep = MutableStateFlow(4)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _bpm = MutableStateFlow(140)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _quantize = MutableStateFlow("1/16")
    val quantize: StateFlow<String> = _quantize.asStateFlow()

    private val _velocity = MutableStateFlow(85)
    val velocity: StateFlow<Int> = _velocity.asStateFlow()

    private val _isMetronomeOn = MutableStateFlow(false)
    val isMetronomeOn: StateFlow<Boolean> = _isMetronomeOn.asStateFlow()

    private val _isLoopOn = MutableStateFlow(true)
    val isLoopOn: StateFlow<Boolean> = _isLoopOn.asStateFlow()

    private val _isMonitorOn = MutableStateFlow(true)
    val isMonitorOn: StateFlow<Boolean> = _isMonitorOn.asStateFlow()

    // Meter animation values
    val beatMeter = MutableStateFlow(0.65f)
    val vocalMeter = MutableStateFlow(0.55f)
    val adlibMeter = MutableStateFlow(0.40f)
    val harmonyMeter = MutableStateFlow(0.48f)

    // Track Mute & Solo
    val beatMuted = MutableStateFlow(false)
    val vocalMuted = MutableStateFlow(false)
    val adlibMuted = MutableStateFlow(false)
    val harmonyMuted = MutableStateFlow(false)

    val beatSolo = MutableStateFlow(false)
    val vocalSolo = MutableStateFlow(false)
    val adlibSolo = MutableStateFlow(false)
    val harmonySolo = MutableStateFlow(false)

    // Piano Roll Notes (matching Image 1)
    private val _pianoNotes = MutableStateFlow(
        listOf(
            PianoRollNote(midiNote = 67, startStep = 4, lengthSteps = 4), // G4
            PianoRollNote(midiNote = 64, startStep = 4, lengthSteps = 4), // E4
            PianoRollNote(midiNote = 60, startStep = 8, lengthSteps = 4), // C4
            PianoRollNote(midiNote = 60, startStep = 12, lengthSteps = 4),
            PianoRollNote(midiNote = 67, startStep = 16, lengthSteps = 4),
            PianoRollNote(midiNote = 60, startStep = 16, lengthSteps = 4),
            PianoRollNote(midiNote = 57, startStep = 20, lengthSteps = 4), // A3
            PianoRollNote(midiNote = 55, startStep = 20, lengthSteps = 4), // G3
            PianoRollNote(midiNote = 53, startStep = 20, lengthSteps = 4), // F3
            PianoRollNote(midiNote = 57, startStep = 24, lengthSteps = 4),
            PianoRollNote(midiNote = 55, startStep = 24, lengthSteps = 4),
            PianoRollNote(midiNote = 57, startStep = 28, lengthSteps = 4),
            PianoRollNote(midiNote = 53, startStep = 28, lengthSteps = 4)
        )
    )
    val pianoNotes: StateFlow<List<PianoRollNote>> = _pianoNotes.asStateFlow()

    // Instrument Controls (Reverb, Delay, Attack)
    val instReverb = MutableStateFlow(0.35f)
    val instDelay = MutableStateFlow(0.20f)
    val instAttack = MutableStateFlow(0.15f)

    // Vocal FX chain states
    val vocalFxPreset = MutableStateFlow("Clean")
    val autoTuneEnabled = MutableStateFlow(true)
    val autoTuneKey = MutableStateFlow("C Major")
    val autoTuneAmount = MutableStateFlow(48f)
    val vocalReverbEnabled = MutableStateFlow(true)
    val vocalReverbAmount = MutableStateFlow(32f)
    val vocalDelayEnabled = MutableStateFlow(true)
    val vocalDelayAmount = MutableStateFlow(24f)
    val compressorEnabled = MutableStateFlow(true)
    val deEsserEnabled = MutableStateFlow(true)
    val retuneSpeed = MutableStateFlow(45f)
    val humanize = MutableStateFlow(22f)
    val flexTune = MutableStateFlow(true)
    val voicePreset = MutableStateFlow("R&B")
    val isBypassed = MutableStateFlow(false)
    val livePitchCent = MutableStateFlow(12f)

    // Export State
    val exportFormat = MutableStateFlow(AudioExporter.ExportFormat.MP3)
    val exportQuality = MutableStateFlow(AudioExporter.AudioQuality.PRO_STUDIO)
    val exportProgress = MutableStateFlow(0)
    val isExporting = MutableStateFlow(false)
    val exportedFile = MutableStateFlow<File?>(null)

    init {
        // Uncaught exception handler: prevent crashes and report to UI
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("StudioDAW", "Uncaught exception on thread ${thread.name}: ${throwable.message}", throwable)
            postStatus("Audio note: ${throwable.message ?: "Recovered from error"}", isError = false)
            previousHandler?.uncaughtException(thread, throwable)
        }

        // Wire audio engine callbacks to notification shower
        audioPlayer.onError = { err -> postStatus(err, isError = true) }
        audioPlayer.onStatus = { msg -> postStatus(msg, isError = false) }
        audioRecorder.onError = { err -> postStatus(err, isError = true) }
        audioRecorder.onStatus = { msg -> postStatus(msg, isError = false) }

        // Purge any legacy placeholder dummy projects from earlier runs
        viewModelScope.launch(Dispatchers.IO) {
            val dummyIds = listOf("p_continue", "p_summer", "p_vocal", "p_chill", "p_ambient", "p_drums", "p_midnight")
            db.projectDao().deleteByIds(dummyIds)
        }

        // Keep active project and tracks aligned with real projects from Room
        viewModelScope.launch {
            projects.collect { list ->
                if (list.isNotEmpty()) {
                    val currentId = _activeProject.value.id
                    val project = list.find { it.id == currentId } ?: list.first()
                    _activeProject.value = project
                    _bpm.value = project.bpm
                    loadTracksFromProject(project)
                } else {
                    _activeProject.value = defaultProjectTemplate
                    loadTracksFromProject(defaultProjectTemplate)
                }
            }
        }

        // Live pitch simulation loop for UI needle
        viewModelScope.launch {
            while (isActive) {
                if (_currentScreen.value == StudioScreen.AUTO_TUNE) {
                    val target = (Random.nextFloat() * 40f - 20f)
                    livePitchCent.value = target
                }
                delay(120)
            }
        }
    }

    fun postStatus(message: String, isError: Boolean = false) {
        notificationJob?.cancel()
        _statusNotification.value = StatusNotification(message = message, isError = isError)
        notificationJob = viewModelScope.launch {
            delay(3500)
            if (_statusNotification.value?.message == message) {
                _statusNotification.value = null
            }
        }
    }

    fun dismissNotification() {
        notificationJob?.cancel()
        _statusNotification.value = null
    }

    fun navigateTo(screen: StudioScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
            postStatus("Opened ${screen.name.replace('_', ' ')}")
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.lastIndex)
            return true
        }
        return false
    }

    fun createProject(title: String, bpm: Int = 120, category: String = "All") {
        val cleanTitle = title.trim().ifEmpty { "Project ${(100..999).random()}" }
        val now = System.currentTimeMillis()
        val newProj = ProjectEntity(
            id = "p_${now}_${(1000..9999).random()}",
            title = cleanTitle,
            subtitle = "Draft",
            bpm = bpm.coerceIn(40, 240),
            keySignature = "C Major",
            durationSeconds = 60,
            durationFormatted = "1:00",
            lastEditedDisplay = "Just now",
            lastEditedTimestamp = now,
            category = category,
            lyrics = ""
        )
        viewModelScope.launch(Dispatchers.IO) {
            db.projectDao().insertProject(newProj)
            _activeProject.value = newProj
            _bpm.value = newProj.bpm
            postStatus("Created \"${newProj.title}\"")
        }
    }

    fun renameProject(project: ProjectEntity, newTitle: String) {
        val clean = newTitle.trim()
        if (clean.isEmpty()) return
        val now = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            db.projectDao().renameProject(project.id, clean, now, "Just now")
            if (_activeProject.value.id == project.id) {
                _activeProject.value = _activeProject.value.copy(
                    title = clean,
                    lastEditedTimestamp = now,
                    lastEditedDisplay = "Just now"
                )
            }
            postStatus("Renamed to \"$clean\"")
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.projectDao().deleteById(project.id)
            if (_activeProject.value.id == project.id) {
                _activeProject.value = defaultProjectTemplate
            }
            postStatus("Deleted \"${project.title}\"")
        }
    }

    fun selectProject(project: ProjectEntity) {
        val now = System.currentTimeMillis()
        val touched = project.copy(
            lastEditedTimestamp = now,
            lastEditedDisplay = "Just now"
        )
        _activeProject.value = touched
        _bpm.value = project.bpm
        viewModelScope.launch(Dispatchers.IO) {
            db.projectDao().updateProject(touched)
        }
        postStatus("Loaded: ${project.title}")
        navigateTo(StudioScreen.MIXER)
    }

    fun togglePlay() {
        toggleMultitrackPlay()
    }

    fun startPlayback() {
        startMultitrackPlayback()
    }

    fun stopPlayback() {
        stopMultitrackPlayback()
    }

    fun toggleMetronome() {
        _isMetronomeOn.value = !_isMetronomeOn.value
        postStatus(if (_isMetronomeOn.value) "Metronome Enabled" else "Metronome Disabled")
    }

    fun toggleLoop() {
        _isLoopOn.value = !_isLoopOn.value
        postStatus(if (_isLoopOn.value) "Loop Enabled" else "Loop Disabled")
    }

    fun toggleMonitor() {
        _isMonitorOn.value = !_isMonitorOn.value
        postStatus(if (_isMonitorOn.value) "Monitor Enabled" else "Monitor Disabled")
    }

    fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(40, 240)
        postStatus("Tempo: ${_bpm.value} BPM")
    }

    fun setQuantize(q: String) {
        _quantize.value = q
        postStatus("Quantize: $q")
    }

    fun setVelocity(v: Int) {
        _velocity.value = v.coerceIn(1, 127)
        postStatus("Velocity: $v")
    }

    // Mixer adjustments
    fun updateBeatVolume(vol: Float) {
        _activeProject.value = _activeProject.value.copy(beatVolume = vol)
    }

    fun updateVocalVolume(vol: Float) {
        _activeProject.value = _activeProject.value.copy(vocalVolume = vol)
    }

    fun updateAdlibVolume(vol: Float) {
        _activeProject.value = _activeProject.value.copy(adlibVolume = vol)
    }

    fun updateHarmonyVolume(vol: Float) {
        _activeProject.value = _activeProject.value.copy(harmonyVolume = vol)
    }

    fun updateBeatPan(pan: Float) {
        _activeProject.value = _activeProject.value.copy(beatPan = pan)
    }

    fun updateVocalPan(pan: Float) {
        _activeProject.value = _activeProject.value.copy(vocalPan = pan)
    }

    fun updateAdlibPan(pan: Float) {
        _activeProject.value = _activeProject.value.copy(adlibPan = pan)
    }

    fun updateHarmonyPan(pan: Float) {
        _activeProject.value = _activeProject.value.copy(harmonyPan = pan)
    }

    // Piano Roll note management
    fun addOrRemoveNote(midiNote: Int, step: Int) {
        val existing = _pianoNotes.value.find { it.midiNote == midiNote && it.startStep == step }
        if (existing != null) {
            _pianoNotes.value = _pianoNotes.value - existing
            postStatus("Removed note at beat ${step / 4 + 1}")
        } else {
            val newNote = PianoRollNote(
                midiNote = midiNote,
                startStep = step,
                lengthSteps = 2,
                velocity = _velocity.value / 127f
            )
            _pianoNotes.value = _pianoNotes.value + newNote
            postStatus("Added note: ${midiToNoteName(midiNote)} at beat ${step / 4 + 1}")
        }
    }

    fun undoLastNote() {
        if (_pianoNotes.value.isNotEmpty()) {
            _pianoNotes.value = _pianoNotes.value.dropLast(1)
            postStatus("Undid last note")
        } else {
            postStatus("No notes to undo")
        }
    }

    // Drum Pad Trigger
    fun triggerPad(type: DrumType) {
        postStatus("Selected pad: ${type.displayName}")
    }

    // Export Action
    fun startExport(context: Context) {
        if (isExporting.value) return
        isExporting.value = true
        exportProgress.value = 0
        postStatus("Rendering mix (${exportFormat.value.title})...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = AudioExporter.renderAndExport(
                    context = context,
                    project = _activeProject.value,
                    format = exportFormat.value,
                    quality = exportQuality.value,
                    onProgress = { p ->
                        exportProgress.value = p
                    }
                )
                exportedFile.value = file
                postStatus("Export Complete: ${file.name} (${file.length() / 1024} KB)")
            } catch (e: Exception) {
                postStatus("Export failed: ${e.message}", isError = true)
            } finally {
                isExporting.value = false
            }
        }
    }

    // ==========================================
    // MULTITRACK DAW TIMELINE ENGINE
    // ==========================================

    private fun loadTracksFromProject(project: ProjectEntity) {
        val loaded = StudioTrack.listFromJson(project.notesData)
        if (loaded.isNotEmpty()) {
            _tracks.value = loaded
        } else {
            // Start a new project with one empty track
            val initial = listOf(StudioTrack(name = "Audio 1"))
            _tracks.value = initial
            persistTracks(initial)
        }
        if (_selectedTrackId.value == null || _tracks.value.none { it.id == _selectedTrackId.value }) {
            _selectedTrackId.value = _tracks.value.firstOrNull()?.id
        }
    }

    private fun persistTracks(tracksList: List<StudioTrack>) {
        val json = StudioTrack.listToJson(tracksList)
        val now = System.currentTimeMillis()
        val updatedProject = _activeProject.value.copy(
            notesData = json,
            lastEditedTimestamp = now,
            lastEditedDisplay = "Just now"
        )
        _activeProject.value = updatedProject
        viewModelScope.launch(Dispatchers.IO) {
            db.projectDao().updateProject(updatedProject)
        }
    }

    fun addTrack(customName: String? = null) {
        val newName = customName?.trim()?.ifEmpty { null }
            ?: "Audio ${_tracks.value.size + 1}"
        val newTrack = StudioTrack(name = newName)
        val updated = _tracks.value + newTrack
        _tracks.value = updated
        _selectedTrackId.value = newTrack.id
        persistTracks(updated)
        postStatus("Added track: $newName")
    }

    fun deleteTrack(trackId: String) {
        if (_tracks.value.size <= 1) {
            postStatus("Cannot delete only remaining track", isError = true)
            return
        }
        val target = _tracks.value.find { it.id == trackId } ?: return
        val updated = _tracks.value.filter { it.id != trackId }
        _tracks.value = updated
        if (_selectedTrackId.value == trackId) {
            _selectedTrackId.value = updated.firstOrNull()?.id
        }
        persistTracks(updated)
        postStatus("Deleted track: ${target.name}")
    }

    fun renameTrack(trackId: String, newName: String) {
        val clean = newName.trim()
        if (clean.isEmpty()) return
        val updated = _tracks.value.map {
            if (it.id == trackId) it.copy(name = clean) else it
        }
        _tracks.value = updated
        persistTracks(updated)
        postStatus("Renamed track to: $clean")
    }

    fun toggleTrackMute(trackId: String) {
        val updated = _tracks.value.map {
            if (it.id == trackId) it.copy(isMuted = !it.isMuted) else it
        }
        _tracks.value = updated
        persistTracks(updated)
    }

    fun toggleTrackSolo(trackId: String) {
        val updated = _tracks.value.map {
            if (it.id == trackId) it.copy(isSoloed = !it.isSoloed) else it
        }
        _tracks.value = updated
        persistTracks(updated)
    }

    fun selectTrack(trackId: String) {
        _selectedTrackId.value = trackId
    }

    fun selectClip(clipId: String?) {
        _selectedClipId.value = clipId
    }

    fun deleteSelectedClip() {
        val clipId = _selectedClipId.value
        if (clipId == null) {
            postStatus("No clip selected", isError = true)
            return
        }
        var clipName = "Clip"
        val updated = _tracks.value.map { track ->
            val clipToRemove = track.clips.find { it.id == clipId }
            if (clipToRemove != null) {
                clipName = clipToRemove.name
                track.copy(clips = track.clips.filter { it.id != clipId })
            } else track
        }
        _tracks.value = updated
        _selectedClipId.value = null
        persistTracks(updated)
        postStatus("Deleted $clipName")
    }

    fun duplicateSelectedClip() {
        val clipId = _selectedClipId.value
        if (clipId == null) {
            postStatus("Select a clip to duplicate", isError = true)
            return
        }
        var duplicatedName = ""
        val updated = _tracks.value.map { track ->
            val clipToDup = track.clips.find { it.id == clipId }
            if (clipToDup != null) {
                val newStart = clipToDup.startOffsetSeconds + clipToDup.durationSeconds + 0.2f
                val newClip = clipToDup.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "${clipToDup.name} (Copy)",
                    startOffsetSeconds = newStart
                )
                duplicatedName = newClip.name
                track.copy(clips = track.clips + newClip)
            } else track
        }
        _tracks.value = updated
        persistTracks(updated)
        postStatus("Duplicated $duplicatedName")
    }

    fun splitSelectedClip() {
        val clipId = _selectedClipId.value
        val playhead = _playheadSeconds.value
        if (clipId == null) {
            postStatus("Select a clip to split", isError = true)
            return
        }
        var splitOccurred = false
        val updated = _tracks.value.map { track ->
            val clip = track.clips.find { it.id == clipId }
            if (clip != null && playhead > clip.startOffsetSeconds && playhead < (clip.startOffsetSeconds + clip.durationSeconds)) {
                val firstDuration = playhead - clip.startOffsetSeconds
                val secondDuration = clip.durationSeconds - firstDuration
                if (firstDuration > 0.1f && secondDuration > 0.1f) {
                    val half1 = clip.copy(
                        durationSeconds = firstDuration,
                        waveformPoints = clip.waveformPoints.take((clip.waveformPoints.size * (firstDuration / clip.durationSeconds)).toInt().coerceAtLeast(10))
                    )
                    val half2 = clip.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "${clip.name} (Part 2)",
                        startOffsetSeconds = playhead,
                        durationSeconds = secondDuration,
                        waveformPoints = clip.waveformPoints.takeLast((clip.waveformPoints.size * (secondDuration / clip.durationSeconds)).toInt().coerceAtLeast(10))
                    )
                    splitOccurred = true
                    track.copy(clips = track.clips.filter { it.id != clipId } + listOf(half1, half2))
                } else track
            } else track
        }
        if (splitOccurred) {
            _tracks.value = updated
            persistTracks(updated)
            postStatus("Split clip at ${AudioUtils.formatTimecode(playhead)}")
        } else {
            postStatus("Move playhead inside selected clip to split", isError = true)
        }
    }

    fun seekPlayhead(targetSec: Float) {
        val wasPlaying = _isPlaying.value
        if (wasPlaying) {
            stopMultitrackPlayback()
        }
        _playheadSeconds.value = targetSec.coerceAtLeast(0f)
        if (wasPlaying) {
            startMultitrackPlayback()
        }
    }

    fun rewindPlayhead() {
        val wasPlaying = _isPlaying.value
        if (wasPlaying) {
            stopMultitrackPlayback()
        }
        _playheadSeconds.value = 0f
        if (wasPlaying) {
            startMultitrackPlayback()
        }
        postStatus("Rewound to 00:00.00")
    }

    fun toggleMultitrackPlay() {
        if (_isRecordingMultitrack.value) {
            stopMultitrackRecording()
        } else if (_isPlaying.value) {
            stopMultitrackPlayback()
            postStatus("Playback Stopped")
        } else {
            startMultitrackPlayback()
            postStatus("Playing from ${AudioUtils.formatTimecode(_playheadSeconds.value)}")
        }
    }

    fun startMultitrackPlayback() {
        stopMultitrackPlayback()
        _isPlaying.value = true

        val currentPlayhead = _playheadSeconds.value
        startBackingPlaybackFromPlayhead(currentPlayhead)

        multitrackJob?.cancel()
        multitrackJob = viewModelScope.launch(Dispatchers.Main) {
            var lastTime = System.currentTimeMillis()
            var lastBeat = -1
            val bpmVal = _activeProject.value.bpm.coerceIn(40, 240)
            val beatIntervalSec = 60f / bpmVal

            while (isActive && _isPlaying.value) {
                val now = System.currentTimeMillis()
                val dt = (now - lastTime) / 1000f
                lastTime = now
                _playheadSeconds.value += dt

                // Visual beat tick tracking
                val currentBeat = (_playheadSeconds.value / beatIntervalSec).toInt()
                if (currentBeat != lastBeat) {
                    lastBeat = currentBeat
                }

                // Auto-stop or loop when past all clips
                val maxClipEnd = _tracks.value.flatMap { it.clips }
                    .maxOfOrNull { it.startOffsetSeconds + it.durationSeconds } ?: 0f
                if (maxClipEnd > 0 && _playheadSeconds.value > maxClipEnd + 1.5f) {
                    if (_isLoopOn.value) {
                        _playheadSeconds.value = 0f
                        stopAllMediaPlayers()
                        startBackingPlaybackFromPlayhead(0f)
                    } else {
                        stopMultitrackPlayback()
                        break
                    }
                }

                delay(25)
            }
        }
    }

    fun stopMultitrackPlayback() {
        _isPlaying.value = false
        multitrackJob?.cancel()
        multitrackJob = null
        stopAllMediaPlayers()
    }

    private fun startBackingPlaybackFromPlayhead(startSec: Float) {
        stopAllMediaPlayers()
        viewModelScope.launch(Dispatchers.IO) {
            val tracksList = _tracks.value
            val hasSolo = tracksList.any { it.isSoloed }

            for (track in tracksList) {
                val isAudible = if (hasSolo) track.isSoloed else !track.isMuted
                if (!isAudible) continue

                for (clip in track.clips) {
                    val clipEnd = clip.startOffsetSeconds + clip.durationSeconds
                    if (clipEnd <= startSec) continue
                    val file = File(clip.filePath)
                    if (!file.exists()) continue

                    val initialDelayMs = if (clip.startOffsetSeconds > startSec) {
                        ((clip.startOffsetSeconds - startSec) * 1000).toLong()
                    } else {
                        0L
                    }

                    val seekMs = if (startSec > clip.startOffsetSeconds) {
                        ((startSec - clip.startOffsetSeconds) * 1000).toInt()
                    } else {
                        0
                    }

                    launch {
                        if (initialDelayMs > 0) {
                            delay(initialDelayMs)
                        }
                        if (!_isPlaying.value && !_isRecordingMultitrack.value) return@launch

                        try {
                            val player = MediaPlayer().apply {
                                setDataSource(file.absolutePath)
                                prepare()
                                if (seekMs > 0) {
                                    seekTo(seekMs)
                                }
                                setVolume(track.volume, track.volume)
                                start()
                            }
                            synchronized(activePlayers) {
                                activePlayers.add(player)
                            }
                            player.setOnCompletionListener {
                                try {
                                    it.release()
                                } catch (_: Exception) {}
                                synchronized(activePlayers) {
                                    activePlayers.remove(player)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun stopAllMediaPlayers() {
        synchronized(activePlayers) {
            for (p in activePlayers) {
                try {
                    p.stop()
                    p.release()
                } catch (_: Exception) {}
            }
            activePlayers.clear()
        }
    }

    fun startMultitrackRecording() {
        if (_isRecordingMultitrack.value) return
        stopMultitrackPlayback()

        val currentTrack = _tracks.value.find { it.id == _selectedTrackId.value }
            ?: _tracks.value.firstOrNull()
            ?: return

        multitrackRecordStartTime = _playheadSeconds.value
        _isRecordingMultitrack.value = true
        _isPlaying.value = true

        // Start direct audio recording
        audioRecorder.startRecording()

        // Start synchronized backing tracks playback
        startBackingPlaybackFromPlayhead(multitrackRecordStartTime)

        multitrackJob?.cancel()
        multitrackJob = viewModelScope.launch(Dispatchers.Main) {
            var lastTime = System.currentTimeMillis()
            var lastBeat = -1
            val bpmVal = _activeProject.value.bpm.coerceIn(40, 240)
            val beatIntervalSec = 60f / bpmVal

            while (isActive && _isRecordingMultitrack.value) {
                val now = System.currentTimeMillis()
                val dt = (now - lastTime) / 1000f
                lastTime = now
                _playheadSeconds.value += dt

                val currentBeat = (_playheadSeconds.value / beatIntervalSec).toInt()
                if (currentBeat != lastBeat) {
                    lastBeat = currentBeat
                }
                delay(25)
            }
        }
        postStatus("Recording on ${currentTrack.name}...")
    }

    fun stopMultitrackRecording() {
        if (!_isRecordingMultitrack.value) return
        _isRecordingMultitrack.value = false
        _isPlaying.value = false
        multitrackJob?.cancel()
        multitrackJob = null
        stopAllMediaPlayers()

        audioRecorder.stopRecording()

        viewModelScope.launch(Dispatchers.IO) {
            delay(200)
            val recFile = audioRecorder.lastRecordedFile
            if (recFile != null && recFile.exists() && recFile.length() > 100L) {
                val context = getApplication<Application>()
                val audioDir = File(context.filesDir, "project_audio")
                if (!audioDir.exists()) audioDir.mkdirs()
                val destFile = File(audioDir, "take_${System.currentTimeMillis()}.wav")
                recFile.copyTo(destFile, overwrite = true)

                val durationSec = (_playheadSeconds.value - multitrackRecordStartTime).coerceAtLeast(0.5f)
                val waveform = AudioUtils.extractWaveform(destFile, 48)

                val currentTrack = _tracks.value.find { it.id == _selectedTrackId.value }
                    ?: _tracks.value.firstOrNull()

                if (currentTrack != null) {
                    val newClip = AudioClip(
                        name = "Take ${currentTrack.clips.size + 1}",
                        filePath = destFile.absolutePath,
                        startOffsetSeconds = multitrackRecordStartTime,
                        durationSeconds = durationSec,
                        waveformPoints = waveform
                    )
                    val updatedClips = currentTrack.clips + newClip
                    val updatedTracks = _tracks.value.map {
                        if (it.id == currentTrack.id) it.copy(clips = updatedClips) else it
                    }
                    _tracks.value = updatedTracks
                    _selectedClipId.value = newClip.id
                    persistTracks(updatedTracks)
                    postStatus("Saved Take: ${newClip.name} (${String.format("%.1fs", durationSec)})")
                }
            } else {
                postStatus("Microphone take saved", isError = false)
            }
        }
    }

    fun importAudioClip(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val contentResolver = context.contentResolver

                var fileName = "Imported Track"
                try {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1 && cursor.moveToFirst()) {
                            fileName = cursor.getString(nameIdx) ?: "Imported Track"
                        }
                    }
                } catch (_: Exception) {}

                val audioDir = File(context.filesDir, "project_audio")
                if (!audioDir.exists()) audioDir.mkdirs()
                val destFile = File(audioDir, "imp_${System.currentTimeMillis()}_${fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")}")

                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (!destFile.exists() || destFile.length() < 100L) {
                    postStatus("Failed to copy audio file", isError = true)
                    return@launch
                }

                var durationMs = 0L
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(destFile.absolutePath)
                    val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    durationMs = durStr?.toLongOrNull() ?: 0L
                } catch (_: Exception) {
                } finally {
                    try { retriever.release() } catch (_: Exception) {}
                }

                val durationSec = if (durationMs > 0L) {
                    durationMs / 1000f
                } else {
                    (destFile.length() / (44100 * 2)).toFloat().coerceAtLeast(1.0f)
                }

                val waveform = AudioUtils.extractWaveform(destFile, 48)
                val cleanName = fileName.substringBeforeLast(".")

                val selectedTrack = _tracks.value.find { it.id == _selectedTrackId.value }
                    ?: _tracks.value.firstOrNull()

                if (selectedTrack != null) {
                    val newClip = AudioClip(
                        name = cleanName,
                        filePath = destFile.absolutePath,
                        startOffsetSeconds = _playheadSeconds.value,
                        durationSeconds = durationSec,
                        waveformPoints = waveform
                    )
                    val updatedClips = selectedTrack.clips + newClip
                    val updatedTracks = _tracks.value.map {
                        if (it.id == selectedTrack.id) it.copy(clips = updatedClips) else it
                    }
                    _tracks.value = updatedTracks
                    _selectedClipId.value = newClip.id
                    persistTracks(updatedTracks)
                    postStatus("Imported \"$cleanName\" (${String.format("%.1fs", durationSec)})")
                }
            } catch (e: Exception) {
                postStatus("Import failed: ${e.message}", isError = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAllMediaPlayers()
        audioPlayer.release()
        audioRecorder.stopRecording()
    }

    companion object {
        fun midiToNoteName(midi: Int): String {
            val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
            val note = notes[midi % 12]
            val octave = (midi / 12) - 1
            return "$note$octave"
        }
    }
}
