package com.manning.aip.mymoviesdatabase
package data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteStatement}
import android.provider.BaseColumns

import scala.collection.mutable.ListBuffer

import CategoryTable.CategoryColumns
import model.Category

class CategoryDao(db: SQLiteDatabase) extends Dao[Category] {

  private val insertStatement: SQLiteStatement =
    db compileStatement CategoryDao.INSERT

  override def save(entity: Category): Long = {
    insertStatement.clearBindings()
    insertStatement.bindString(1, entity.getName)
    insertStatement.executeInsert()
  }

  override def update(entity: Category) {
    val values = new ContentValues()
    values.put(CategoryColumns.NAME, entity.getName)
    db.update(CategoryTable.TABLE_NAME, values, BaseColumns._ID + " = ?",
              Array(entity.getName.toString))
  }

  override def delete(entity: Category) {
    if (entity.getId > 0) {
      db.delete(CategoryTable.TABLE_NAME, BaseColumns._ID + " = ?", Array(entity.getId.toString))
    }
  }

  override def get(id: Long): Category = {
    val c: Cursor =
               db.query(CategoryTable.TABLE_NAME, Array(BaseColumns._ID, CategoryColumns.NAME),
                        BaseColumns._ID + " = ?", Array(id.toString), null, null, null, "1")
    val category = 
      if (c.moveToFirst()) {
        val category = new Category()
        category setId (c getLong 0)
        category setName (c getString 1)
        category
      }
      else
        null
    if (!c.isClosed) c.close()
    category
  }

  override def getAll: List[Category] = {
    val c: Cursor = db.query(
      CategoryTable.TABLE_NAME,
      Array(BaseColumns._ID, CategoryColumns.NAME),
      null, null, null, null, CategoryColumns.NAME, null)
    val list =
      if (c.moveToFirst()) {
        val buf = new ListBuffer[Category]
        do {
            val category = new Category()
            category setId (c getLong 0)
            category setName(c getString 1)
            buf += category
         } while (c.moveToNext())
        buf.toList
      }
      else
        null
    if (!c.isClosed) c.close()
    list
  }

  def find(name: String): Category = {
    val sql =
      "select _id, name from " + CategoryTable.TABLE_NAME +
      " where upper(" + CategoryColumns.NAME + ") = ? limit 1"
    val c = db.rawQuery(sql, Array(name.toUpperCase))
    val category =
      if (c.moveToFirst()) {
        val category = new Category()
        category setId (c getLong 0)
        category setName (c getString 1)
        category
      }
      else
        null
    if (!c.isClosed) c.close()
    category
  }
}

object CategoryDao {
  private final val INSERT =
    "insert into " + CategoryTable.TABLE_NAME + "(" + CategoryColumns.NAME + ") values (?)"
}
