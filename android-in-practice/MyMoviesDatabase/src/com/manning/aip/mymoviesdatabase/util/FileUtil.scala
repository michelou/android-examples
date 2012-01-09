package com.manning.aip.mymoviesdatabase
package util

import android.util.Log

import java.io.{BufferedWriter, File, FileInputStream, FileOutputStream}
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel

/**
 * FileUtil methods. 
 * 
 * @author ccollins
 *
 */
object FileUtil {

  // Object for intrinsic lock (per docs 0 length array "lighter" than a normal Object)
  final val DATA_LOCK = Array[AnyRef]()

  // nio is there too
  /**
   * Copy file.
   * 
   * @param src
   * @param dst
   * @return
   */
  def copyFile(src: File, dst: File): Boolean = {
    var result = false
    var inChannel: FileChannel = null
    var outChannel: FileChannel = null
    DATA_LOCK synchronized {
      try {
        inChannel = new FileInputStream(src).getChannel
        outChannel = new FileOutputStream(dst).getChannel
        inChannel.transferTo(0, inChannel.size, outChannel)
        result = true
      } catch {
        case e: IOException =>
      } finally {
        if (inChannel != null && inChannel.isOpen)
          try inChannel.close()
          catch { case e: IOException => /*ignore*/ }
        if (outChannel != null && outChannel.isOpen)
          try outChannel.close()
          catch { case e: IOException => /*ignore*/ }
      }
    }
    result
  }

  /**
   * Replace entire File with contents of String.
   * 
   * @param fileContents
   * @param file
   * @return
   */
  def writeStringAsFile(fileContents: String, file: File): Boolean = {
    var result = false
    try {
      DATA_LOCK synchronized {
        if (file != null) {
          file.createNewFile() // ok if returns false, overwrite
          // FileWriter will use default encoding
          // (could use FileOutputStream to control encoding and guarantee flush, but don't need that here)
          val out = new BufferedWriter(new FileWriter(file), 1024)
          out write fileContents
          out.close() // close will flush and close (no guarantee of sync though)
          result = true
        }
      }
    } catch {
      case e: IOException =>
        Log.e(Constants.LOG_TAG, "Error writing string data to file " + e.getMessage, e)
    }
    result
  }

  /**
   * Append String to end of File.
   * 
   * @param appendContents
   * @param file
   * @return
   */
  def appendStringToFile(appendContents: String, file: File): Boolean = {
    var result = false;
    try {
      DATA_LOCK synchronized {
        if ((file != null) && file.canWrite) {
          file.createNewFile() // ok if returns false, overwrite
          // FileWriter will use default encoding, and easy to append
          val out = new BufferedWriter(new FileWriter(file, true), 1024)
          out write appendContents
          out.close()
          result = true
        }
      }
    } catch {
      case e: IOException =>
        Log.e(Constants.LOG_TAG, "Error appending string data to file " + e.getMessage, e)
    }
    result
  }

  /**
   * Call sync on a FileOutputStream to ensure it is written to disk immediately
   * (write, flush, close, etc, don't guarantee physical disk write on buffered file systems).
   * 
   * @param stream
   * @return
   */
  def syncStream(fos: FileOutputStream): Boolean =
    try {
      if (fos != null) fos.getFD.sync()
      true
    } catch {
      case e: IOException =>
        Log.e(Constants.LOG_TAG, "Error syncing stream " + e.getMessage, e)
        false
    }
}
