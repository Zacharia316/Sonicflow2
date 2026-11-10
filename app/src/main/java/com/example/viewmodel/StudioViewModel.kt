package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioExporter
import com.example.audio.AudioPlayer
import com.example.audio.AudioRecorder
import com.example.audio.AudioSynthesizer
import com.example.audio.DrumType
import com.example.data.ProjectEntity
import com.example.data.StudioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

enum class StudioScreen {
    HOME,
    PROJECTS,
    MIXER,
    PIANO_ROLL,
    INSTRUMENTS,
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

    // Active project state
    private val _activeProject = MutableStateFlow(
        ProjectEntity(
            id = "p_midnight",
            title = "Midnight Drive",
            subtitle = "Final Mix",
            bpm = 140,
            keySignature = "C Major",
            durationSeconds = 154,
            durationFormatted = "2:34",
            lastEditedDisplay = "Edited 12m ago",
            beatVolume = 0.85f,  // -3.2dB
            vocalVolume = 0.80f, // -5.1dB
            adlibVolume = 0.65f, // -8.0dB
            harmonyVolume = 0.72f,// -6.4dB
            beatPan = -0.12f,
            vocalPan = 0.06f,
            adlibPan = 0.00f,
            harmonyPan = 0.03f,
            lyrics = "driving late, city lights fade\nwaiting for the sunrise on the highway"
        )
    )
    val activeProject: StateFlow<ProjectEntity> = _activeProject.asStateFlow()

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
        // Wire audio engine callbacks to notification shower
        audioPlayer.onError = { err -> postStatus(err, isError = true) }
        audioPlayer.onStatus = { msg -> postStatus(msg, isError = false) }
        audioRecorder.onError = { err -> postStatus(err, isError = true) }
        audioRecorder.onStatus = { msg -> postStatus(msg, isError = false) }

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

    fun selectProject(project: ProjectEntity) {
        _activeProject.value = project
        _bpm.value = project.bpm
        postStatus("Loaded Project: ${project.title}")
        navigateTo(StudioScreen.MIXER)
    }

    fun togglePlay() {
        if (_isPlaying.value) {
            stopPlayback()
            postStatus("Playback Stopped")
        } else {
            startPlayback()
            postStatus("Playing: ${_activeProject.value.title} (${_bpm.value} BPM)")
        }
    }

    fun startPlayback() {
        _isPlaying.value = true
        audioPlayer.startLoop(
            bpm = _bpm.value,
            enableMetronome = _isMetronomeOn.value,
            onBeatTick = { step, progress ->
                _currentStep.value = step
                _playbackProgress.value = progress

                beatMeter.value = (0.5f + Random.nextFloat() * 0.45f) * _activeProject.value.beatVolume
                vocalMeter.value = (0.4f + Random.nextFloat() * 0.40f) * _activeProject.value.vocalVolume
                adlibMeter.value = (0.2f + Random.nextFloat() * 0.40f) * _activeProject.value.adlibVolume
                harmonyMeter.value = (0.3f + Random.nextFloat() * 0.40f) * _activeProject.value.harmonyVolume
            },
            onStop = {
                _isPlaying.value = false
            }
        )
    }

    fun stopPlayback() {
        _isPlaying.value = false
        audioPlayer.stopLoop()
        beatMeter.value = 0f
        vocalMeter.value = 0f
        adlibMeter.value = 0f
        harmonyMeter.value = 0f
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
            audioPlayer.playPianoNote(midiNote, _velocity.value / 127f)
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
        audioPlayer.playDrum(type, 1.0f)
        postStatus("Triggered: ${type.displayName}")
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

    override fun onCleared() {
        super.onCleared()
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
