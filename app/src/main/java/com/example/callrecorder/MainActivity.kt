package com.example.callrecorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // Constant for microphone permission request code
    private val MIC_PERMISSION_CODE = 100

    // MediaRecorder instance for recording audio
    private var mediaRecorder: MediaRecorder? = null

    // Output file path to save the recorded audio
    private val OUTPUT_FILE = "/sdcard/recorded_audio.3gp"

    // TelephonyManager to listen for phone call state changes
    private lateinit var telephonyManager: TelephonyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the UI layout for this activity
        setContentView(R.layout.activity_main)

        // Get the TelephonyManager system service
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        // Listen for call state changes
        telephonyManager.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                super.onCallStateChanged(state, phoneNumber)
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        // Call is active, start recording if permission granted
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            startRecording()
                        } else {
                            // Request microphone permission if not granted
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                MIC_PERMISSION_CODE
                            )
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        // Call ended, stop recording
                        stopRecording()
                    }
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
    }

    // Function to start audio recording
    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            // Set audio source to voice communication (call audio)
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            // Set output format to 3GP
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            // Set output file path
            setOutputFile(OUTPUT_FILE)
            // Set audio encoder to AMR_NB
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                // Prepare the MediaRecorder
                prepare()
                // Start recording
                start()
                println("[+] Recording started...")
            } catch (e: IOException) {
                // Print stack trace if preparation or start fails
                e.printStackTrace()
            }
        }
    }

    // Function to stop audio recording
    private fun stopRecording() {
        mediaRecorder?.let {
            try {
                // Stop recording
                it.stop()
            } catch (e: RuntimeException) {
                // Handle case where stop is called before start
                e.printStackTrace()
            }
            // Release MediaRecorder resources
            it.release()
            mediaRecorder = null
            println("[+] Recording stopped.")
        }
    }

    // Handle the result of permission requests
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MIC_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can start recording if needed
            } else {
                println("[-] Microphone permission denied.")
            }
        }
    }
}
