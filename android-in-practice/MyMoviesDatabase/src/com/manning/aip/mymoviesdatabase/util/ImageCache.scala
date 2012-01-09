package com.manning.aip.mymoviesdatabase.util

import android.graphics.Bitmap

import java.util.{Collections, LinkedHashMap, Map}

/**
 * Naive cache that relies on HashMap's removeEldestEntry.
 * 
 * @author ccollins
 *
 */
@SuppressWarnings(Array("serial"))
class ImageCache {
  import ImageCache._  // companion object

  // HashMap decorator that only grows to X size
  // (note, using simple WeakHashMap is NOT a good cache for this, it uses weak references for *keys*)
  private final val cache: Map[String, Bitmap] =
    Collections.synchronizedMap(
      new LinkedHashMap[String, Bitmap](ImageCache.IMAGE_CACHE_SIZE + 1, .75F, true) {
        override def removeEldestEntry(eldest: Map.Entry[String, Bitmap]): Boolean =
          size > ImageCache.IMAGE_CACHE_SIZE
      })

  def get(urlString: String): Bitmap = cache get urlString
  def put(urlString: String, bitmap: Bitmap) { cache.put(urlString, bitmap) }
}

object ImageCache {
  private final val IMAGE_CACHE_SIZE = 250
}
