package com.manning.aip.mymoviesdatabase.data

import android.os.Environment

object DataConstants {

  private final val APP_PACKAGE_NAME = "com.manning.aip.mymoviesdatabase"

  private final val EXTERNAL_DATA_DIR_NAME = "mymoviesdata"
  final val EXTERNAL_DATA_PATH =
    Environment.getExternalStorageDirectory + "/" + DataConstants.EXTERNAL_DATA_DIR_NAME

  final val DATABASE_NAME = "mymovies.db"
  final val DATABASE_PATH =
    Environment.getDataDirectory + "/data/" + DataConstants.APP_PACKAGE_NAME +
    "/databases/" + DataConstants.DATABASE_NAME
}

