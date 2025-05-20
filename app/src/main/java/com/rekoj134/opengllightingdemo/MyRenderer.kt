package com.rekoj134.opengllightingdemo

import android.content.Context
import android.opengl.GLES32.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.rekoj134.opengllightingdemo.util.ShaderHelper
import com.rekoj134.opengllightingdemo.util.ShaderReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class MyRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val vertices = floatArrayOf(
        -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,

        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
        0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
        -0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,

        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,

        0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
        0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
        0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
        0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
        0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
        0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,

        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
        0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
        0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
        0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
        -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,

        -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
        -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
        -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f
    )

    private var programCube = 0
    private var programLight = 0
    private var VAO = 0
    private var lightVAO = 0
    private var VBO = 0

    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)

    private var angle = 0.0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glEnable(GL_DEPTH_TEST)

        // Load & compile shaders
        programCube = ShaderHelper.buildProgram(
            ShaderReader.readTextFileFromResource(context, R.raw.light_vertex_shader),
            ShaderReader.readTextFileFromResource(context, R.raw.light_fragment_shader)
        )

        programLight = ShaderHelper.buildProgram(
            ShaderReader.readTextFileFromResource(context, R.raw.light_source_vertex_shader),
            ShaderReader.readTextFileFromResource(context, R.raw.light_source_fragment_shader)
        )

        // Setup buffers
        val vaoBuf = IntBuffer.allocate(1)
        val vboBuf = IntBuffer.allocate(1)
        glGenVertexArrays(1, vaoBuf)
        glGenBuffers(1, vboBuf)
        VAO = vaoBuf[0]
        VBO = vboBuf[0]

        glBindVertexArray(VAO)
        glBindBuffer(GL_ARRAY_BUFFER, VBO)
        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)
        glBufferData(GL_ARRAY_BUFFER, vertices.size * Float.SIZE_BYTES, vertexBuffer, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, 3 * Float.SIZE_BYTES)
        glEnableVertexAttribArray(1)

        // Light cube VAO (share same VBO)
        val lightVaoBuf = IntBuffer.allocate(1)
        glGenVertexArrays(1, lightVaoBuf)
        lightVAO = lightVaoBuf[0]
        glBindVertexArray(lightVAO)
        glBindBuffer(GL_ARRAY_BUFFER, VBO)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspect, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        angle += 0.5f
        if (angle >= 360f) angle -= 360f

        val radians = Math.toRadians(angle.toDouble()).toFloat()

        val camX = (sin(radians.toDouble()) * 8).toFloat()
        val camZ = (cos(radians.toDouble()) * 8).toFloat()

        Matrix.setLookAtM(viewMatrix, 0,
            camX, 2f, camZ,
            0f, 0f, 0f,
            0f, 1f, 0f
        )

        // ========== Draw Cube ==========
        glUseProgram(programCube)
        Matrix.setIdentityM(modelMatrix, 0)
        glUniformMatrix4fv(glGetUniformLocation(programCube, "model"), 1, false, modelMatrix, 0)
        glUniformMatrix4fv(glGetUniformLocation(programCube, "view"), 1, false, viewMatrix, 0)
        glUniformMatrix4fv(glGetUniformLocation(programCube, "projection"), 1, false, projectionMatrix, 0)
        glUniform3f(glGetUniformLocation(programCube, "objectColor"), 1f, 0.5f, 0.31f)
        glUniform3f(glGetUniformLocation(programCube, "lightColor"), 1f, 1f, 1f)
        glUniform3f(glGetUniformLocation(programCube, "lightPos"), 1.2f, 1.0f, 2.0f)
        glBindVertexArray(VAO)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // ========== Draw Light Cube ==========
        glUseProgram(programLight)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 1.2f, 1.0f, 2.0f)
        Matrix.scaleM(modelMatrix, 0, 0.2f, 0.2f, 0.2f)
        glUniformMatrix4fv(glGetUniformLocation(programLight, "model"), 1, false, modelMatrix, 0)
        glUniformMatrix4fv(glGetUniformLocation(programLight, "view"), 1, false, viewMatrix, 0)
        glUniformMatrix4fv(glGetUniformLocation(programLight, "projection"), 1, false, projectionMatrix, 0)
        glBindVertexArray(lightVAO)
        glDrawArrays(GL_TRIANGLES, 0, 36)
    }
}
