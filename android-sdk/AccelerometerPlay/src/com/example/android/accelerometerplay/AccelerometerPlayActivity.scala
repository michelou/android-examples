/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.accelerometerplay

import android.app.Activity
import android.content.Context, Context._
import android.graphics.{Bitmap, BitmapFactory, Canvas}
import android.graphics.BitmapFactory.Options
import android.hardware.{Sensor, SensorEvent, SensorEventListener, SensorManager}
import android.os.{Bundle, PowerManager}
import android.util.DisplayMetrics
import android.view.{Display, Surface, View, WindowManager}

/**
 * This is an example of using the accelerometer to integrate the device's
 * acceleration to a position using the Verlet method. This is illustrated with
 * a very simple particle system comprised of a few iron balls freely moving on
 * an inclined wooden table. The inclination of the virtual table is controlled
 * by the device's accelerometer.
 * 
 * @see SensorManager
 * @see SensorEvent
 * @see Sensor
 */

class AccelerometerPlayActivity extends Activity {

  private var mSimulationView: SimulationView = _
  private var mSensorManager: SensorManager = _
  private var mPowerManager: PowerManager = _
  private var mWindowManager: WindowManager = _
  private var mDisplay: Display = _
  private var mWakeLock: PowerManager#WakeLock = _

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Get an instance of the SensorManager
    mSensorManager = getSystemService(SENSOR_SERVICE).asInstanceOf[SensorManager]

    // Get an instance of the PowerManager
    mPowerManager = getSystemService(POWER_SERVICE).asInstanceOf[PowerManager]

    // Get an instance of the WindowManager
    mWindowManager = getSystemService(WINDOW_SERVICE).asInstanceOf[WindowManager]
    mDisplay = mWindowManager.getDefaultDisplay

    // Create a bright wake lock
    mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                                          getClass.getName)

    // instantiate our simulation view and set it as the activity's content
    mSimulationView = new SimulationView(this)
    setContentView(mSimulationView)
  }

  override protected def onResume() {
    super.onResume()
    /*
     * when the activity is resumed, we acquire a wake-lock so that the
     * screen stays on, since the user will likely not be fiddling with the
     * screen or buttons.
     */
    mWakeLock.acquire()

    // Start the simulation
    mSimulationView.startSimulation()
  }

  override protected def onPause() {
    super.onPause()
    /*
     * When the activity is paused, we make sure to stop the simulation,
     * release our sensor resources and wake locks
     */

    // Stop the simulation
    mSimulationView.stopSimulation()

    // and release our wake-lock
    mWakeLock.release()
  }

  object SimulationView {
    // diameter of the balls in meters
    private final val sBallDiameter = 0.004f
    private final val sBallDiameter2 = sBallDiameter * sBallDiameter

    // friction of the virtual table and air
    private final val sFriction = 0.1f
  }

  class SimulationView(context: Context) extends View(context) with SensorEventListener {
    import SimulationView._  //companion object

    private val mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var mLastT: Long = _
    private var mLastDeltaT: Float = _

    private val (mXDpi, mYDpi) = {
      val metrics = new DisplayMetrics
      getWindowManager.getDefaultDisplay getMetrics metrics
      (metrics.xdpi, metrics.ydpi)
    }
    private val mMetersToPixelsX: Float = mXDpi / 0.0254f
    private val mMetersToPixelsY: Float = mYDpi / 0.0254f
    private var mBitmap: Bitmap = {
      // rescale the ball so it's about 0.5 cm on screen
      val ball = BitmapFactory.decodeResource(getResources, R.drawable.ball)
      val dstWidth = (sBallDiameter * mMetersToPixelsX + 0.5f).toInt
      val dstHeight = (sBallDiameter * mMetersToPixelsY + 0.5f).toInt
      Bitmap.createScaledBitmap(ball, dstWidth, dstHeight, true)
    }
    private val mWood: Bitmap = {
      val opts = new Options
      opts.inDither = true
      opts.inPreferredConfig = Bitmap.Config.RGB_565
      BitmapFactory.decodeResource(getResources, R.drawable.wood, opts)
    }
    private var mXOrigin: Float = _
    private var mYOrigin: Float = _
    private var mSensorX: Float = _
    private var mSensorY: Float = _
    private var mSensorTimeStamp: Long = _
    private var mCpuTimeStamp: Long = _
    private var mHorizontalBound: Float = _
    private var mVerticalBound: Float = _
    private val mParticleSystem = new ParticleSystem

    /*
     * Each of our particle holds its previous and current position, its
     * acceleration. for added realism each particle has its own friction
     * coefficient.
     */
    class Particle {
      /*private*/ var mPosX: Float = _
      /*private*/ var mPosY: Float = _
      private var mAccelX: Float = _
      private var mAccelY: Float = _
      private var mLastPosX: Float = _
      private var mLastPosY: Float = _

      // make each particle a bit different by randomizing its
      // coefficient of friction
      private var mOneMinusFriction: Float = {
        val r = (math.random.toFloat - 0.5f) * 0.2f
        1.0f - sFriction + r
      }

      def computePhysics(sx: Float, sy: Float, dT: Float, dTC: Float) {
        // Force of gravity applied to our virtual object
        val m = 1000.0f // mass of our virtual object
        val gx = -sx * m
        val gy = -sy * m

        /*
         * ·F = mA <=> A = ·F / m We could simplify the code by
         * completely eliminating "m" (the mass) from all the equations,
         * but it would hide the concepts from this sample code.
         */
        val invm = 1.0f / m
        val ax = gx * invm
        val ay = gy * invm

        /*
         * Time-corrected Verlet integration The position Verlet
         * integrator is defined as x(t+Æt) = x(t) + x(t) - x(t-Æt) +
         * a(t)Ætö2 However, the above equation doesn't handle variable
         * Æt very well, a time-corrected version is needed: x(t+Æt) =
         * x(t) + (x(t) - x(t-Æt)) * (Æt/Æt_prev) + a(t)Ætö2 We also add
         * a simple friction term (f) to the equation: x(t+Æt) = x(t) +
         * (1-f) * (x(t) - x(t-Æt)) * (Æt/Æt_prev) + a(t)Ætö2
         */
        val dTdT = dT * dT;
        val x = mPosX + mOneMinusFriction * dTC * (mPosX - mLastPosX) +
                mAccelX * dTdT
        val y = mPosY + mOneMinusFriction * dTC * (mPosY - mLastPosY) +
                mAccelY * dTdT
        mLastPosX = mPosX
        mLastPosY = mPosY
        mPosX = x
        mPosY = y
        mAccelX = ax
        mAccelY = ay
      }

      /*
       * Resolving constraints and collisions with the Verlet integrator
       * can be very simple, we simply need to move a colliding or
       * constrained particle in such way that the constraint is satisfied.
       */
      def resolveCollisionWithBounds() {
        val xmax = mHorizontalBound
        val ymax = mVerticalBound
        val x = mPosX
        val y = mPosY
        if (x > xmax) mPosX = xmax
        else if (x < -xmax) mPosX = -xmax
        if (y > ymax) mPosY = ymax
        else if (y < -ymax) mPosY = -ymax
      }
    }

    object ParticleSystem {
      final val NUM_PARTICLES = 15
    }
    /*
     * A particle system is just a collection of particles
     */
    class ParticleSystem {
      import ParticleSystem._  // companion object

      /*
       * Initially our particles have no speed or acceleration
       */
      private val mBalls = Array.fill(NUM_PARTICLES)(new Particle)

      /*
       * Update the position of each particle in the system using the
       * Verlet integrator.
       */
      private def updatePositions(sx: Float, sy: Float, timestamp: Long) {
        val t = timestamp
        if (mLastT != 0) {
          val dT = (t - mLastT).toFloat * (1.0f / 1000000000.0f)
          if (mLastDeltaT != 0) {
            val dTC = dT / mLastDeltaT
            val count = mBalls.length
            for (ball <- mBalls)
              ball.computePhysics(sx, sy, dT, dTC)
          }
          mLastDeltaT = dT
        }
        mLastT = t
      }

      /*
       * Performs one iteration of the simulation. First updating the
       * position of all the particles and resolving the constraints and
       * collisions.
       */
      def update(sx: Float, sy: Float, now: Long) {
        // update the system's positions
        updatePositions(sx, sy, now)

        // We do no more than a limited number of iterations
        val NUM_MAX_ITERATIONS = 10

        /*
         * Resolve collisions, each particle is tested against every
         * other particle for collision. If a collision is detected the
         * particle is moved away using a virtual spring of infinite
         * stiffness.
         */
        var more = true
        val count = mBalls.length
        var k = 0
        while (k < NUM_MAX_ITERATIONS && more) {
          more = false
          for (i <- 0 until count) {
            val curr = mBalls(i)
            for (j <- i + 1 until count) {
              val ball = mBalls(j)
              var dx = ball.mPosX - curr.mPosX
              var dy = ball.mPosY - curr.mPosY
              var dd = dx * dx + dy * dy
              // Check for collisions
              if (dd <= sBallDiameter2) {
                /*
                 * add a little bit of entropy, after nothing is
                 * perfect in the universe.
                 */
                dx += (math.random.toFloat - 0.5f) * 0.0001f
                dy += (math.random.toFloat - 0.5f) * 0.0001f
                dd = dx * dx + dy * dy
                // simulate the spring
                val d = math.sqrt(dd).toFloat
                val c = (0.5f * (sBallDiameter - d)) / d
                curr.mPosX -= dx * c
                curr.mPosY -= dy * c
                ball.mPosX += dx * c
                ball.mPosY += dy * c
                more = true
              }
            }
            /*
             * Finally make sure the particle doesn't intersects
             * with the walls.
             */
            curr.resolveCollisionWithBounds()
          }
          k += 1
        } //while
      }

      def getParticleCount: Int = mBalls.length

      def getPosX(i: Int): Float = mBalls(i).mPosX

      def getPosY(i: Int): Float = mBalls(i).mPosY
    }

    def startSimulation() {
      /*
       * It is not necessary to get accelerometer events at a very high
       * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
       * automatic low-pass filter, which "extracts" the gravity component
       * of the acceleration. As an added benefit, we use less power and
       * CPU resources.
       */
      mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    def stopSimulation() {
      mSensorManager unregisterListener this
    }

    override protected def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
      // compute the origin of the screen relative to the origin of the bitmap
      mXOrigin = (w - mBitmap.getWidth) * 0.5f
      mYOrigin = (h - mBitmap.getHeight) * 0.5f
      mHorizontalBound = ((w / mMetersToPixelsX - sBallDiameter) * 0.5f)
      mVerticalBound = ((h / mMetersToPixelsY - sBallDiameter) * 0.5f)
    }

    override def onSensorChanged(event: SensorEvent) {
      if (event.sensor.getType != Sensor.TYPE_ACCELEROMETER)
        return
      /*
       * record the accelerometer data, the event's timestamp as well as
       * the current time. The latter is needed so we can calculate the
       * "present" time during rendering. In this application, we need to
       * take into account how the screen is rotated with respect to the
       * sensors (which always return data in a coordinate space aligned
       * to with the screen in its native orientation).
       */

      mDisplay.getRotation match {
        case Surface.ROTATION_0 =>
          mSensorX = event.values(0)
          mSensorY = event.values(1)
        case Surface.ROTATION_90 =>
          mSensorX = -event.values(1)
          mSensorY = event.values(0)
        case Surface.ROTATION_180 =>
          mSensorX = -event.values(0)
          mSensorY = -event.values(1)
        case Surface.ROTATION_270 =>
          mSensorX = event.values(1)
          mSensorY = -event.values(0)
        case _ =>
      }

      mSensorTimeStamp = event.timestamp
      mCpuTimeStamp = System.nanoTime
    }

    override protected def onDraw(canvas: Canvas) {
      /*
       * draw the background
       */
      canvas.drawBitmap(mWood, 0, 0, null)

      /*
       * compute the new position of our object, based on accelerometer
       * data and present time.
       */
      val particleSystem = mParticleSystem
      val now = mSensorTimeStamp + (System.nanoTime - mCpuTimeStamp)
      val sx = mSensorX
      val sy = mSensorY

      particleSystem.update(sx, sy, now)

      val xc = mXOrigin
      val yc = mYOrigin
      val xs = mMetersToPixelsX
      val ys = mMetersToPixelsY
      val bitmap = mBitmap
      val count = particleSystem.getParticleCount
      for (i <- 0 until count) {
        /*
         * We transform the canvas so that the coordinate system matches
         * the sensors coordinate system with the origin in the center
         * of the screen and the unit is the meter.
         */
        val x = xc + particleSystem.getPosX(i) * xs
        val y = yc - particleSystem.getPosY(i) * ys
        canvas.drawBitmap(bitmap, x, y, null)
      }

      // and make sure to redraw asap
      invalidate()
    }

    override def onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
  }
}
