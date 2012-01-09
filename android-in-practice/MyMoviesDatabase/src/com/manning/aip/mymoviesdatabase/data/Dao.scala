package com.manning.aip.mymoviesdatabase.data

trait Dao[T] {
  def save(typ: T): Long
  def update(typ: T)
  def delete(typ: T)
  def get(id: Long): T
  def getAll: List[T]
}

