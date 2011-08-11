package com.manning.aip.fileexplorer.util

import android.os.Environment
import android.util.Log

import com.manning.aip.fileexplorer.Constants

import java.io.{BufferedReader, BufferedWriter, File, FileInputStream}
import java.io.{FileOutputStream, FileReader, FileWriter, IOException, Writer}
import java.nio.channels.FileChannel

/**
 * FileUtil methods. 
 * 
 * @author ccollins
 *
 */
object FileUtil {

  final val LINE_SEP = System.getProperty("line.separator")

  // from the Android docs, these are the recommended paths
  private final val EXT_STORAGE_PATH_PREFIX = "/Android/data/"
  private final val EXT_STORAGE_FILES_PATH_SUFFIX = "/files/"
  private final val EXT_STORAGE_CACHE_PATH_SUFFIX = "/cache/"

  // Object for intrinsic lock (per docs 0 length array "lighter" than a normal Object)
  final val DATA_LOCK = new Array[Object](0)

  /**
   * Use Environment to check if external storage is writable.
   * 
   * @return
   */
  def isExternalStorageWritable: Boolean =
    Environment.getExternalStorageState equals Environment.MEDIA_MOUNTED

  /**
   * Use environment to check if external storage is readable.
   * 
   * @return
   */
  def isExternalStorageReadable: Boolean =
   isExternalStorageWritable ||
   (Environment.getExternalStorageState equals Environment.MEDIA_MOUNTED_READ_ONLY)

  /**
   * Return the recommended external files directory, whether using API level 8 or lower.
   * (Uses getExternalStorageDirectory and then appends the recommended path.)
   * 
   * @param packageName
   * @return
   */
  def getExternalFilesDirAllApiLevels(packageName: String): File =
    FileUtil.getExternalDirAllApiLevels(packageName, EXT_STORAGE_FILES_PATH_SUFFIX)
   
  /**
   * Return the recommended external cache directory, whether using API level 8 or lower.
   * (Uses getExternalStorageDirectory and then appends the recommended path.)
   * 
   * @param packageName
   * @return
   */
  def getExternalCacheDirAllApiLevels(packageName: String): File =
    FileUtil.getExternalDirAllApiLevels(packageName, EXT_STORAGE_CACHE_PATH_SUFFIX)

  private def getExternalDirAllApiLevels(packageName: String, suffixType: String): File = {
    val dir = new File(Environment.getExternalStorageDirectory + EXT_STORAGE_PATH_PREFIX + packageName + suffixType)
    synchronized { //(FileUtil.DATA_LOCK) {
      try {
        dir.mkdirs()
        dir.createNewFile()
      } catch {
        case e: IOException =>
          Log.e(Constants.LOG_TAG, "Error creating file", e)
      }
    }
    dir
  }

  /**
   * Copy file, return true on success, false on failure.
   * 
   * @param src
   * @param dst
   * @return
   */
  def copyFile(src: File, dst: File): Boolean = {
    var result = false
    var inChannel: FileChannel = null
    var outChannel: FileChannel = null
    synchronized { //(FileUtil.DATA_LOCK) {
      try {
        inChannel = new FileInputStream(src).getChannel
        outChannel = new FileOutputStream(dst).getChannel
        inChannel.transferTo(0, inChannel.size, outChannel)
        result = true
      } catch {
        case e: IOException => // ignore
      } finally {
        if (inChannel != null && inChannel.isOpen) {
          try inChannel.close() 
          catch { case e: IOException => /* ignore */ }
        }
        if (outChannel != null && outChannel.isOpen) {
          try outChannel.close()
          catch { case e: IOException => /* ignore */ }
        }
      }
    }
    result
  }

  /**
   * Replace entire File with contents of String, return true on success, false on failure.
   * 
   * @param fileContents
   * @param file
   * @return
   */
  def writeStringAsFile(fileContents: String, file: File): Boolean = {
    var result = false
    try {
      synchronized { //(FileUtil.DATA_LOCK) {
        if (file != null) {
          file.createNewFile() // ok if returns false, overwrite
          val out = new BufferedWriter(new FileWriter(file), 1024)
          out.write(fileContents)
          out.close()
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
   * Append String to end of File, return true on success, false on failure.
   * 
   * @param appendContents
   * @param file
   * @return
   */
  def appendStringToFile(appendContents: String, file: File): Boolean = {
    var result = false
    try {
      FileUtil.DATA_LOCK synchronized {
        if ((file != null) && file.canWrite) {
          file.createNewFile() // ok if returns false, overwrite
          val out = new BufferedWriter(new FileWriter(file, true), 1024)
          out.write(appendContents)
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
   * Read file as String, return null if file is not present or not readable.
   * 
   * @param file
   * @return
   */
  def readFileAsString(file: File): String = {
    var sb: StringBuilder = null
    try {
      FileUtil.DATA_LOCK synchronized {
        if ((file != null) && file.canRead) {
          sb = new StringBuilder()
          val in = new BufferedReader(new FileReader(file), 1024)
          var line: String = in.readLine()
          while (line != null) {
            sb append line
            sb append LINE_SEP
            line = in.readLine()
          }
        }
      }
    } catch {
      case e: IOException =>
       Log.e(Constants.LOG_TAG, "Error reading file " + e.getMessage, e)
    }
    if (sb != null) sb.toString
    else null
  }
}
