package org.sariska.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.oney.WebRTCModule.WebRTCView
import io.sariska.sdk.Conference
import io.sariska.sdk.SariskaMediaTransport
import io.sariska.sdk.Connection
import io.sariska.sdk.JitsiLocalTrack
import io.sariska.sdk.JitsiRemoteTrack

class MainActivity : AppCompatActivity() {
    lateinit var conference: Conference
    lateinit var connection: Connection
    private var mRemoteContainer: RelativeLayout? = null
    private var mLocalContainer: RelativeLayout? = null
    private var localTracks: List<JitsiLocalTrack>? = null
    private var remoteView: WebRTCView? = null
    var PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    )
    var PERMISSION_ALL = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        }
        SariskaMediaTransport.initializeSdk(application) // initialize sdk
        mLocalContainer = findViewById(R.id.local_video_view_container)
        mRemoteContainer = findViewById(R.id.remote_video_view_container)
        setupLocalStream()
        val token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjI0ZmQ2ZjkyZDZkMDE3NDkyZTNlOThlMzM0ZWJhZmM3NmRkMzUwYmI5M2EwNzI5ZDM4IiwidHlwIjoiSldUIn0.eyJjb250ZXh0Ijp7InVzZXIiOnsiaWQiOiJ6dG5reWw3biIsIm5hbWUiOiJqaiJ9LCJncm91cCI6Imc3cWtua205YWJ0cDFuYWd2eXk1ZnUifSwic3ViIjoiMiIsInJvb20iOiJwNHN1anR5YWsiLCJpYXQiOjE2MTg0NDMxNTksIm5iZiI6MTYxODQ0MzE1OSwiaXNzIjoic2FyaXNrYSIsImF1ZCI6Im1lZGlhX21lc3NhZ2luZ19zYXJpc2thIiwiZXhwIjoxNjE4NTI5NTU5fQ.dhGrKmmRE7E1Mr_Hp3Nu9VSoR7mMEmYz5nI-Fp8ZR97wOXMdvvgjUA_xr2ghYTMP6DGm81MztqsJFW5BSZ18D5ejtx11MyjdTDcsVBiVXNmUO7C6KCHHPDEjRirC1mNc5d9V7Unta-Fo6K6oLyWnsPyfctXdrURk8ChrnXPyHvX_TiZalotdkmChooTbCQlL8SNRk-j8-HVWToYxukv7aB7AvTSvh_e9xshAlsZMzO9dfoenkMH4XILfxCfcWcj-gdfZnflwSu5kpBW6CmY-I9Fe6M4DUGamNplgyua3xe1olwXMe-Ofo48-Yu0vkHbE4ssuoMEsgm0vCt7GXTRd2w"
        connection = SariskaMediaTransport.JitsiConnection(token, "dipak", false)
        connection.addEventListener("CONNECTION_ESTABLISHED", { createConference() })
        connection.addEventListener("CONNECTION_FAILED", {})
        connection.addEventListener("CONNECTION_DISCONNECTED", {})
        connection.connect()
    }

    fun setupLocalStream() {
        val options = Bundle()
        options.putBoolean("audio", true)
        options.putBoolean("video", true)
        options.putInt("resolution", 240) // 180, 240, 360, 720, 1080
        //      options.putString("facingMode", "user");   user or environment
//      options.putBoolean("desktop", true);  for screen sharing
//      options.putString("micDeviceId", "mic_device_id");
//      options.putString("cameraDeviceId", "camera_device_id");
        SariskaMediaTransport.createLocalTracks(options) { tracks ->
            runOnUiThread {
                localTracks = tracks
                for (track in tracks) {
                    if (track.getType().equals("video")) {
                        val view: WebRTCView = track.render()
                        view.setObjectFit("cover")
                        mLocalContainer!!.addView(view)
                    }
                }
            }
        }
    }

    fun createConference() {
        conference = connection.initJitsiConference()
        conference.addEventListener("CONFERENCE_JOINED") { ->
            for (track in localTracks!!) {
                conference.addTrack(track)
            }
        }
        conference.addEventListener("DOMINANT_SPEAKER_CHANGED") { p -> val participantId = p as String }
        conference.addEventListener("CONFERENCE_LEFT") {->

        }
        conference.addEventListener("TRACK_ADDED") { p ->
            val track: JitsiRemoteTrack = p as JitsiRemoteTrack
            runOnUiThread {
                if (track.getType().equals("video")) {
                    val view: WebRTCView = track.render()
                    view.setObjectFit("cover")
                    remoteView = view
                    mRemoteContainer!!.addView(view)
                }
            }
        }
        conference.addEventListener("TRACK_REMOVED") { p ->
            val track: JitsiRemoteTrack = p as JitsiRemoteTrack
            runOnUiThread { mRemoteContainer!!.removeView(remoteView) }
        }
        conference.join()
    }

    fun hasPermissions(context: MainActivity?, vararg permissions: String?): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission!!) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onBackPressed() {
        conference.leave()
        connection.disconnect()
        finish()
        System.gc()
        System.exit(0)
    }

    public override fun onDestroy() {
        super.onDestroy()
        conference.leave()
        connection.disconnect()
        finish()
    }
}