package com.rekoj134.opengllightingdemo

import android.app.ActivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rekoj134.opengllightingdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var rendererSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initOpenGLES()
    }

    private fun initOpenGLES() {
        // Kiểm tra phiên bản của OpenGLES
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        // 0x30002 là phiên bản 3.2 của OpenGLES
        val supportsEs32 = configurationInfo.reqGlEsVersion >= 0x30002

        if (supportsEs32) {
            // Set phiên bản và Renderer cho GL SurfaceView
            // Khi set EGLContextClientVersion = 3, ta đang cho GLSurfaceView biết rằng mình sử dụng phiên bản 3.0 trở lên
            binding.myGLSurfaceView.setEGLContextClientVersion(3)
            binding.myGLSurfaceView.setRenderer(MyRenderer(this@MainActivity))
            rendererSet = true
        } else {
            Toast.makeText(this@MainActivity, "This device doesn't support OpenGL ES 3.2", Toast.LENGTH_SHORT).show()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            binding.myGLSurfaceView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            binding.myGLSurfaceView.onPause()
        }
    }
}