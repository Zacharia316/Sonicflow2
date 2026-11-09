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

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = StudioDatabase.getInstance(application)
    val audioPlayer = AudioPlayer()
    val audioRecorder = AudioRecorder(application)

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

    private val _playbackProgress = MutableStateFlow(0.14f) // e.g. 00:12 / 01:24
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

    // Meter animation loop
    private var meterJob: Job? = null
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

    // Vocal FX chain states (Image 5 & 8)
    val vocalFxPreset = MutableStateFlow("Clean")
    val autoTuneEnabled = MutableStateFlow(true)
    val autoTuneKey = MutableStateFlow("C Major")
    val autoTuneAmount = MutableStateFlow(48f) // 48%
    val vocalReverbEnabled = MutableStateFlow(true)
    val vocalReverbAmount = MutableStateFlow(32f) // 32%
    val vocalDelayEnabled = MutableStateFlow(true)
    val vocalDelayAmount = MutableStateFlow(24f) // 24%
    val compressorEnabled = MutableStateFlow(true)
    val deEsserEnabled = MutableStateFlow(true)
    val retuneSpeed = MutableStateFlow(45f) // 45%
    val humanize = MutableStateFlow(22f)   // 22%
    val flexTune = MutableStateFlow(true)
    val voicePreset = MutableStateFlow("R&B")
    val isBypassed = MutableStateFlow(false)
    val livePitchCent = MutableStateFlow(12f) // -50c to +50c live monitor

    // Export State (Image 7)
    val exportFormat = MutableStateFlow(AudioExporter.ExportFormat.MP3)
    val exportQuality = MutableStateFlow(AudioExporter.AudioQuality.PRO_STUDIO)
    val exportProgress = MutableStateFlow(0)
    val isExporting = MutableStateFlow(false)
    val exportedFile = MutableStateFlow<File?>(null)

    // Live pitch simulation loop for UI needle
    init {
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

    fun navigateTo(screen: StudioScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
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
        navigateTo(StudioScreen.MIXER)
    }

    fun togglePlay() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
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

                // Animate meter spikes to the beat
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
    }

    fun toggleLoop() {
        _isLoopOn.value = !_isLoopOn.value
    }

    fun toggleMonitor() {
        _isMonitorOn.value = !_isMonitorOn.value
    }

    fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(40, 240)
    }

    fun setQuantize(q: String) {
        _quantize.value = q
    }

    fun setVelocity(v: Int) {
        _velocity.value = v.coerceIn(1, 127)
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
        } else {
            val newNote = PianoRollNote(
                midiNote = midiNote,
                startStep = step,
                lengthSteps = 2,
                velocity = _velocity.value / 127f
            )
            _pianoNotes.value = _pianoNotes.value + newNote
            audioPlayer.playPianoNote(midiNote, _velocity.value / 127f)
        }
    }

    fun undoLastNote() {
        if (_pianoNotes.value.isNotEmpty()) {
            _pianoNotes.value = _pianoNotes.value.dropLast(1)
        }
    }

    // Drum Pad Trigger
    fun triggerPad(type: DrumType) {
        audioPlayer.playDrum(type, 1.0f)
    }

    // Export Action (Image 7)
    fun startExport(context: Context) {
        if (isExporting.value) return
        isExporting.value = true
        exportProgress.value = 0

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
            } catch (e: Exception) {
                e.printStackTrace()
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
}
