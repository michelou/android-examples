/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package org.example.wallpaper

import java.util.concurrent.{ExecutorService, Executors}

import javax.microedition.khronos.egl.{EGL10, EGL11, EGLConfig, EGLContext}
import javax.microedition.khronos.egl.{EGLDisplay, EGLSurface}
import javax.microedition.khronos.opengles.GL10

import android.service.wallpaper.WallpaperService

import android.view.SurfaceHolder

class Wallpaper extends WallpaperService {
  private class MyEngine extends Engine {

  // Engine implementation goes here...

  private var glRenderer: GLRenderer = _
  private var gl: GL10 = _
  private var egl: EGL10 = _
  private var glc: EGLContext = _
  private var glDisplay: EGLDisplay = _
  private var glSurface: EGLSurface = _

  private var executor: ExecutorService = _
  private var drawCommand: Runnable = _

  override def onCreate(holder: SurfaceHolder) {
    super.onCreate(holder)
         
    executor = Executors.newSingleThreadExecutor()

    drawCommand = new Runnable() {
      def run() {
        glRenderer.onDrawFrame(gl)
        egl.eglSwapBuffers(glDisplay, glSurface)
        if (isVisible()
            && egl.eglGetError() != EGL11.EGL_CONTEXT_LOST) {
          executor execute drawCommand
        }
      }
    }   
  }

  override def onDestroy() {
    executor.shutdownNow()
    super.onDestroy()
  }

  override def onSurfaceCreated(holder: SurfaceHolder) {
    super.onSurfaceCreated(holder)
         
    val surfaceCreatedCommand = new Runnable() {
      override def run() {
        // Initialize OpenGL
        egl = EGLContext.getEGL.asInstanceOf[EGL10]
        glDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        val version = new Array[Int](2)
        egl.eglInitialize(glDisplay, version)
        val configSpec = Array(
          EGL10.EGL_RED_SIZE, 5,
          EGL10.EGL_GREEN_SIZE, 6, EGL10.EGL_BLUE_SIZE,
          5, EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE)
        val configs = new Array[EGLConfig](1)
        val numConfig = new Array[Int](1)
        egl.eglChooseConfig(glDisplay, configSpec, configs, 1, numConfig)
        val config = configs(0)

        glc = egl.eglCreateContext(glDisplay, config,
                     EGL10.EGL_NO_CONTEXT, null)

        glSurface = egl.eglCreateWindowSurface(glDisplay, config, holder, null)
        egl.eglMakeCurrent(glDisplay, glSurface, glSurface, glc)
        gl = glc.getGL.asInstanceOf[GL10]

        // Initialize Renderer
        glRenderer = new GLRenderer(Wallpaper.this)
        glRenderer.onSurfaceCreated(gl, config)
      }
    }
    executor execute surfaceCreatedCommand
  }

  override def onSurfaceDestroyed(holder: SurfaceHolder) {
         
    val surfaceDestroyedCommand = new Runnable() {
      def run() {
        // Free OpenGL resources
        egl.eglMakeCurrent(glDisplay, EGL10.EGL_NO_SURFACE,
                     EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
        egl.eglDestroySurface(glDisplay, glSurface)
        egl.eglDestroyContext(glDisplay, glc)
        egl.eglTerminate(glDisplay)
      }
    }
    executor execute surfaceDestroyedCommand

    super.onSurfaceDestroyed(holder)
  }

  override def onSurfaceChanged(holder: SurfaceHolder,
                                format: Int, width: Int, height: Int) {
    super.onSurfaceChanged(holder, format, width, height)
         
    val surfaceChangedCommand = new Runnable() {
      def run() {
        glRenderer.onSurfaceChanged(gl, width, height)
      }
    }
    executor execute surfaceChangedCommand
  }

  override def onVisibilityChanged(visible: Boolean) {
    super.onVisibilityChanged(visible)
         
    if (visible) {
      executor execute drawCommand
    }
  }

  override def onOffsetsChanged(xOffset: Float, yOffset: Float,
                                xOffsetStep: Float, yOffsetStep: Float,
                                xPixelOffset: Int, yPixelOffset: Int) {
    super.onOffsetsChanged(xOffset, yOffset, xOffsetStep,
               yOffsetStep, xPixelOffset, yPixelOffset)
         
    val offsetsChangedCommand = new Runnable() {
      def run() {
        glRenderer.setParallax(xOffset - xOffsetStep)
      }
    }
    executor execute offsetsChangedCommand  
  }

  } // MyEngine

  override def onCreateEngine(): Engine = new MyEngine()

}

