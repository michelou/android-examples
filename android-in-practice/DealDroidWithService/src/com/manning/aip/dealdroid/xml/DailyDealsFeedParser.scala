package com.manning.aip.dealdroid.xml

import com.manning.aip.dealdroid.model.Section

trait DailyDealsFeedParser {
  def parse(): List[Section]
}
