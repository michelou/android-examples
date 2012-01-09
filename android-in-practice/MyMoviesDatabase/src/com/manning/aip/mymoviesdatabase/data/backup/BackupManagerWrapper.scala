package com.manning.aip.mymoviesdatabase.data.backup

import android.app.backup.BackupManager
import android.content.Context

/**
 * Use a wrapper BackupManager, because regular is only available on API level 8 and above.
 * 
 * @author ccollins
 *
 */
class BackupManagerWrapper(context: Context) {
  private var instance = new BackupManager(context)

  def dataChanged() { instance.dataChanged() }
}

object BackupManagerWrapper {
  try Class.forName("android.app.backup.BackupManager")
  catch { case e: Exception => throw new RuntimeException(e) }

  def isAvailable() {}
}
