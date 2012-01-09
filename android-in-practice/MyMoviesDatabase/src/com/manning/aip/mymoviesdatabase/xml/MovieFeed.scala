package com.manning.aip.mymoviesdatabase
package xml

import model.{Movie, MovieSearchResult}

trait MovieFeed {
  def search(name: String): List[MovieSearchResult]
  def get(tmdbId: String): Movie
}
