package com.aaron.voicescrolling.service


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceRecognizer(private val context: Context, private val callback: (String) -> Unit) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val mainHandler = Handler(Looper.getMainLooper())

    // This intent is reused for every start
    private val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        // This helps keep it open slightly longer
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("Voice", "Ready and Listening...")
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val command = matches?.firstOrNull()?.lowercase() ?: ""
            Log.d("Voice", "Heard: $command")

            callback(command)

            // Restart immediately after getting a result
            if (isListening) restartListening()
        }

        override fun onError(error: Int) {
            // Error 6 = Timeout, Error 7 = No match
            Log.d("Voice", "Error/Timeout code: $error. Restarting...")
            if (isListening) {
                // Short delay to let the system "reset" the audio channel
                mainHandler.postDelayed({ restartListening() }, 500)
            }
        }

        override fun onEndOfSpeech() {
            // If the user stops talking, the system might close the mic.
            // We don't restart here because onResults or onError will follow.
        }

        // Empty overrides for required methods
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        if (isListening) return
        isListening = true
        initAndStart()
    }

    private fun initAndStart() {
        // Clean up old instance to avoid "Resource failed to call close"
        speechRecognizer?.destroy()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
            startListening(recognizerIntent)
        }
    }

    private fun restartListening() {
        if (!isListening) return
        speechRecognizer?.cancel()
        speechRecognizer?.startListening(recognizerIntent)
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}