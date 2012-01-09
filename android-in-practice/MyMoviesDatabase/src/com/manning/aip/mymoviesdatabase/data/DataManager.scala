package com.manning.aip.mymoviesdatabase
package data

import android.database.Cursor

import model.{Category, Movie}

/**
 * Android DataManager interface used to define data operations.
 * 
 * @author ccollins
 *
 */
trait DataManager {  
   
  // movie
  def getMovie(movieId: Long): Movie

  def getMovieHeaders: List[Movie]

  def findMovie(name: String): Movie

  def saveMovie(movie: Movie): Long
   
  def deleteMovie(movieId: Long): Boolean
   
  // optional -- used for CursorAdapter
  def getMovieCursor: Cursor
   
  // category
  def getCategory(categoryId: Long): Category

  def getAllCategories: List[Category]

  def findCategory(name: String): Category

  def saveCategory(category: Category): Long

  def deleteCategory(category: Category)
}
