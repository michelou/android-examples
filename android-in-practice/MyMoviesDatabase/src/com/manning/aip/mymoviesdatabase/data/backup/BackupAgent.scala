package com.manning.aip.mymoviesdatabase.data.backup

import android.app.backup.{BackupAgentHelper, BackupDataInput, BackupDataOutput}
import android.app.backup.SharedPreferencesBackupHelper
import android.os.ParcelFileDescriptor

import com.manning.aip.mymoviesdatabase.util.FileUtil

import java.io.IOException

class BackupAgent extends BackupAgentHelper {
  import BackupAgent._  // companion object

  override def onCreate() {
    val prefsHelper =
       new SharedPreferencesBackupHelper(this, BackupAgent.DEFAULT_SHARED_PREFS_KEY)
    addHelper(BackupAgent.PREFS_BACKUP_KEY, prefsHelper)

    ///val csvFileHelper = new FileBackupHelper(this, DataConstants.EXPORT_FILENAME)
    ///addHelper(CSV_FILE_BACKUP_KEY, csvFileHelper)
  }

  @throws(classOf[IOException])
  override def onBackup(oldState: ParcelFileDescriptor, data: BackupDataOutput, newState: ParcelFileDescriptor) {
    // hold the lock while the FileBackupHelper performs backup
    FileUtil.DATA_LOCK synchronized {
      super.onBackup(oldState, data, newState)
    }
  }

  @throws(classOf[IOException])
  override def onRestore(data: BackupDataInput, appVersionCode: Int, newState: ParcelFileDescriptor) {
    // hold the lock while the FileBackupHelper restores the file
    FileUtil.DATA_LOCK synchronized {
      super.onRestore(data, appVersionCode, newState)
    }
  }
}

object BackupAgent {
  private final val DEFAULT_SHARED_PREFS_KEY = "com.manning.aip.mymoviesdatabase_preferences"

  private final val PREFS_BACKUP_KEY = "defaultprefs"
}
