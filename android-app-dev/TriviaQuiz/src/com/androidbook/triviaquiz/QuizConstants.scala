package com.androidbook.triviaquiz

object QuizConstants {

  // Game preference values
  final val GAME_PREFERENCES = "GamePrefs"
  final val GAME_PREFERENCES_NICKNAME = "Nickname" // String
  final val GAME_PREFERENCES_EMAIL = "Email" // String
  final val GAME_PREFERENCES_PASSWORD = "Password" // String
  final val GAME_PREFERENCES_DOB = "DOB" // Long
  final val GAME_PREFERENCES_GENDER = "Gender" // Integer, in array order: Male (1), Female (2), and Undisclosed (0)
  final val GAME_PREFERENCES_SCORE = "Score" // Integer
  final val GAME_PREFERENCES_CURRENT_QUESTION = "CurQuestion" // Integer
  final val GAME_PREFERENCES_AVATAR = "Avatar" // String URL to image

  final val GAME_PREFERENCES_FAV_PLACE_NAME = "FavPlaceName" // String
  final val GAME_PREFERENCES_FAV_PLACE_LONG = "FavPlaceLong" // float
  final val GAME_PREFERENCES_FAV_PLACE_LAT = "FavPlaceLat" // float

  final val GAME_PREFERENCES_PLAYER_ID = "ServerId" // Integer

  // XML Tag Names
  final val XML_TAG_QUESTION_BLOCK = "questions"
  final val XML_TAG_QUESTION = "question"
  final val XML_TAG_QUESTION_ATTRIBUTE_NUMBER = "number"
  final val XML_TAG_QUESTION_ATTRIBUTE_TEXT = "text"
  final val XML_TAG_QUESTION_ATTRIBUTE_IMAGEURL = "imageUrl"
  final val QUESTION_BATCH_SIZE = 5 // 15

  // Server URLs
  final val TRIVIA_SERVER_BASE = "http://tqs.mamlambo.com/"
  final val TRIVIA_SERVER_SCORES = TRIVIA_SERVER_BASE + "scores.jsp"
  final val TRIVIA_SERVER_QUESTIONS = TRIVIA_SERVER_BASE + "questions.jsp"
  final val TRIVIA_SERVER_FRIEND_EDIT = TRIVIA_SERVER_BASE + "friend"
  final val TRIVIA_SERVER_ACCOUNT_EDIT = TRIVIA_SERVER_BASE + "receive"

  final def DEBUG_TAG = "Trivia Quiz Log"
}
