package com.androidbook.triviaquiz

import java.io.{File, IOException, UnsupportedEncodingException}
import java.net.MalformedURLException
import java.security.{MessageDigest, NoSuchAlgorithmException}
import java.util.{Calendar, Iterator, List, Vector}

import org.apache.http.NameValuePair
import org.apache.http.client.{ClientProtocolException, HttpClient}
import org.apache.http.client.ResponseHandler
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import org.apache.http.message.BasicNameValuePair

import android.app.{AlertDialog, DatePickerDialog, Dialog, ProgressDialog}
import android.content.{Context, DialogInterface, Intent, SharedPreferences}
import android.content.DialogInterface.OnCancelListener
import android.content.SharedPreferences.Editor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.location.{Address, Geocoder, Location, LocationManager}
import android.net.Uri
import android.os.{AsyncTask, Bundle}
import android.provider.MediaStore.Images.Media
import android.telephony.TelephonyManager
import android.text.{Editable, TextWatcher}
import android.text.format.{DateFormat, Time}
import android.util.Log
import android.view.{KeyEvent, View, Window}
import android.widget.{AdapterView, ArrayAdapter, Button, DatePicker, EditText}
import android.widget.{ImageButton, Spinner, TextView}

import scala.android._ // implicits
import scala.android.app.Activity

class QuizSettingsActivity extends Activity {
  import QuizConstants._, QuizSettingsActivity._

  private var accountUpload: AccountTask = _
  private var imageUpload: ImageUploadTask = _
  private var friendRequest: FriendRequestTask = _
  private var mGameSettings: SharedPreferences = _
  private var mFavPlaceCoords: GPSCoords = _

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
    setProgressBarIndeterminateVisibility(false)

    setContentView(R.layout.settings)

    // Retrieve the shared preferences
    mGameSettings = getSharedPreferences(GAME_PREFERENCES, Context.MODE_PRIVATE)

    // Initialize the avatar button
    initAvatar()
    // Initialize the nickname entry
    initNicknameEntry()
    // Initialize the email entry
    initEmailEntry()
    // Initialize the Password chooser
    initPasswordChooser()
    // Initialize the Date picker
    initDatePicker()
    // Initialize the spinner
    initGenderSpinner()
    // Initialize the favorite place picker
    initFavoritePlacePicker()
    // initialize friend email entry
    initFriendEmailEntry()

    // if we don't have a serverId yet, we need to get one
    val serverId = mGameSettings.getInt(GAME_PREFERENCES_PLAYER_ID, -1)
    if (serverId == -1) updateServerData()
  }

  override protected def onPause() {
    if (accountUpload != null) accountUpload cancel true
    if (imageUpload != null) imageUpload cancel true
    if (friendRequest != null) friendRequest cancel true
    super.onPause()
  }

  private def canBeExecuted(task: AsyncTask[_, _, _]): Boolean =
    task == null || task.getStatus == AsyncTask.Status.FINISHED ||
    task.isCancelled

  /**
   * Upload a new or modified image to the server
   */
  private def uploadAvatarImage() {
    // make sure we don't collide with another pending update
    if (canBeExecuted(imageUpload)) {
      imageUpload = new ImageUploadTask()
      imageUpload.execute()
    } else {
      Log.w(DEBUG_TAG, "Warning: upload task already going")
    }
  }

  /**
   * update the server with the latest settings data - everything but the image
   *
   */
  private def updateServerData() {
    // make sure we don't collide with another pending update
    if (canBeExecuted(accountUpload)) {
      accountUpload = new AccountTask()
      accountUpload.execute()
    } else {
      Log.w(DEBUG_TAG, "Warning: update task already going")
    }
  }

  /**
   * update the server with the latest settings data - everything but the image
   *
   */
  private def doFriendRequest(friendEmail: String) {
    // make sure we don't collide with another pending update
    if (canBeExecuted(friendRequest)) {
      friendRequest = new FriendRequestTask()
      friendRequest execute friendEmail
    } else {
      Log.w(DEBUG_TAG, "Warning: friendRequestTask already going")
    }
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    requestCode match {
      case TAKE_AVATAR_CAMERA_REQUEST =>
        if (resultCode == Activity.RESULT_CANCELED) {
          // Avatar camera mode was canceled.
        } else if (resultCode == Activity.RESULT_OK) {
          // Took a picture, use the downsized camera image provided by default
          val cameraPic = data.getExtras.get("data").asInstanceOf[Bitmap]
          if (cameraPic != null) {
            try
              saveAvatar(cameraPic)
            catch {
              case e: Exception =>
                Log.e(DEBUG_TAG, "saveAvatar() with camera image failed.", e)
            }
          }
        }

      case TAKE_AVATAR_GALLERY_REQUEST =>
        if (resultCode == Activity.RESULT_CANCELED) {
          // Avatar gallery request mode was canceled.
        } else if (resultCode == Activity.RESULT_OK) {
          // Determine which picture was chosen
          val photoUri = data.getData
          if (photoUri != null) {
            try {
              val maxLength = 75
              // Full size image likely will be large. Let's scale the graphic
              // to a more appropriate size for an avatar
              val galleryPic = Media.getBitmap(getContentResolver, photoUri)
              val scaledGalleryPic = createScaledBitmapKeepingAspectRatio(galleryPic, maxLength)
              saveAvatar(scaledGalleryPic)
            } catch {
              case e: Exception =>
                Log.e(DEBUG_TAG, "saveAvatar() with gallery picker failed.", e)
            }
          }
        }

      }
  }

  /**
   * Scale a Bitmap, keeping its aspect ratio
   *
   * @param bitmap
   *            Bitmap to scale
   * @param maxSide
   *            Maximum length of either side
   * @return a new, scaled Bitmap
   */
  private def createScaledBitmapKeepingAspectRatio(bitmap: Bitmap, maxSide: Int): Bitmap = {
    val orgHeight = bitmap.getHeight
    val orgWidth = bitmap.getWidth

    // scale to no longer any either side than 75px
    val scaledWidth =
      if (orgWidth >= orgHeight) maxSide
      else (maxSide * (orgWidth.toFloat / orgHeight)).toInt
    val scaledHeight =
      if (orgHeight >= orgWidth) maxSide
      else (maxSide * (orgHeight.toFloat / orgWidth)).toInt

    // create the scaled bitmap
    val scaledGalleryPic = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    scaledGalleryPic
  }

  /**
   * Save a Bitmap as avatar.jpg
   *
   * @param avatar
   *            Bitmap to save to file system
   */
  private def saveAvatar(avatar: Bitmap) {
    val strAvatarFilename = "avatar.jpg"
    try
      avatar.compress(CompressFormat.JPEG, 100,
        openFileOutput(strAvatarFilename, Context.MODE_PRIVATE))
    catch {
      case e: Exception =>
        Log.e(DEBUG_TAG, "Avatar compression and save failed.", e)
    }

    val imageUriToSaveCameraImageTo = Uri.fromFile(
      new File(QuizSettingsActivity.this.getFilesDir, strAvatarFilename))

    val editor = mGameSettings.edit
    editor.putString(GAME_PREFERENCES_AVATAR, imageUriToSaveCameraImageTo.getPath)
    editor.commit()

    // upload image to server
    uploadAvatarImage()

    // Update the settings screen
    val avatarButton = findImageButton(R.id.ImageButton_Avatar)
    val strAvatarUri = mGameSettings.getString(GAME_PREFERENCES_AVATAR, "android.resource://com.androidbook.triviaquiz/drawable/avatar")
    val imageUri = Uri.parse(strAvatarUri)
    avatarButton setImageURI null  // Workaround for refreshing an ImageButton, which tries to cache the previous image Uri. Passing null effectively resets it.
    avatarButton setImageURI imageUri
  }

  override protected def onDestroy() {
    Log.d(DEBUG_TAG, "SHARED PREFERENCES")
    Log.d(DEBUG_TAG, "Nickname is: " + mGameSettings.getString(GAME_PREFERENCES_NICKNAME, "Not set"))
    Log.d(DEBUG_TAG, "Email is: " + mGameSettings.getString(GAME_PREFERENCES_EMAIL, "Not set"))
    Log.d(DEBUG_TAG, "Gender (M=1, F=2, U=0) is: " + mGameSettings.getInt(GAME_PREFERENCES_GENDER, 0))
    Log.d(DEBUG_TAG, "Password is: " + mGameSettings.getString(GAME_PREFERENCES_PASSWORD, "Not set"))
    Log.d(DEBUG_TAG, "DOB is: " + DateFormat.format("MMMM dd, yyyy", mGameSettings.getLong(GAME_PREFERENCES_DOB, 0)))
    Log.d(DEBUG_TAG, "Avatar is: " + mGameSettings.getString(GAME_PREFERENCES_AVATAR, "Not set"))
    Log.d(DEBUG_TAG, "Fav Place Name is: " + mGameSettings.getString(GAME_PREFERENCES_FAV_PLACE_NAME, "Not set"))
    Log.d(DEBUG_TAG, "Fav Place GPS Lat is: " + mGameSettings.getFloat(GAME_PREFERENCES_FAV_PLACE_LAT, 0))
    Log.d(DEBUG_TAG, "Fav Place GPS Lon is: " + mGameSettings.getFloat(GAME_PREFERENCES_FAV_PLACE_LONG, 0))

    super.onDestroy()
  }

  /**
   * Initialize the Avatar
   */
  private def initAvatar() {
    // Handle password setting dialog
    val avatarButton = findImageButton(R.id.ImageButton_Avatar)

    if (mGameSettings contains GAME_PREFERENCES_AVATAR) {
      val strAvatarUri = mGameSettings.getString(GAME_PREFERENCES_AVATAR, "android.resource://com.androidbook.triviaquiz/drawable/avatar")
      val imageUri = Uri.parse(strAvatarUri)
      avatarButton setImageURI imageUri
    } else {
      avatarButton setImageResource R.drawable.avatar
    }

    avatarButton setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        val strAvatarPrompt = "Take your picture to store as your avatar!"
        val pictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(Intent.createChooser(pictureIntent, strAvatarPrompt), TAKE_AVATAR_CAMERA_REQUEST)
      }
    }

    avatarButton setOnLongClickListener new View.OnLongClickListener() {
      def onLongClick(v: View): Boolean = {
        val strAvatarPrompt = "Choose a picture to use as your avatar!"
        val pickPhoto = new Intent(Intent.ACTION_PICK)
        pickPhoto setType "image/*"
        startActivityForResult(Intent.createChooser(pickPhoto, strAvatarPrompt), TAKE_AVATAR_GALLERY_REQUEST)
        true
      }
    }
  }

  /**
   * Initialize the nickname entry
   */
  private def initNicknameEntry() {
    // Save Nickname
    val nicknameText = findEditText(R.id.EditText_Nickname)

    if (mGameSettings contains GAME_PREFERENCES_NICKNAME) {
      nicknameText setText mGameSettings.getString(GAME_PREFERENCES_NICKNAME, "")
    }

    nicknameText.setOnKeyListener(new View.OnKeyListener() {
      def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
        if ((event.getAction == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
          val strNickname = nicknameText.getText.toString
          val editor = mGameSettings.edit
          editor.putString(GAME_PREFERENCES_NICKNAME, strNickname)
          editor.commit()

          // ... and server data
          updateServerData()
          true
        } else
          false
      }
    })
  }

  /**
   * Initialize the email entry
   */
  private def initEmailEntry() {
    // Save Email
    val emailText = findEditText(R.id.EditText_Email)

    if (mGameSettings contains GAME_PREFERENCES_EMAIL)
      emailText setText mGameSettings.getString(GAME_PREFERENCES_EMAIL, "")

    emailText.setOnKeyListener(new View.OnKeyListener() {
      def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
        if ((event.getAction == KeyEvent.ACTION_DOWN) && 
            (keyCode == KeyEvent.KEYCODE_ENTER)) {
          val editor = mGameSettings.edit
          editor.putString(GAME_PREFERENCES_EMAIL, emailText.getText.toString)
          editor.commit()
          // ... and server data
          updateServerData()
          true
        } else
          false
      }
    })

  }

  /**
   * Initialize the Password chooser
   */
  private def initPasswordChooser() {
    // Set password info
    val passwordInfo = findTextView(R.id.TextView_Password_Info)

    passwordInfo setText (
      if (mGameSettings contains GAME_PREFERENCES_PASSWORD) R.string.settings_pwd_set
      else R.string.settings_pwd_not_set
    )

    // Handle password setting dialog
    val setPassword = findButton(R.id.Button_Password)
    setPassword setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        showDialog(PASSWORD_DIALOG_ID)
      }
    }
  }

  /**
   * 
   */
  private def initFriendEmailEntry() {
    // Set button handler to Load friend email entry dialog
    val addFriend = findButton(R.id.Button_Friend_Email)
    addFriend setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        showDialog(FRIEND_EMAIL_DIALOG_ID)
      }
    }
  }

  /**
   * Initialize the Date picker
   */
  private def initDatePicker() {
    // Set password info
    val dobInfo = findTextView(R.id.TextView_DOB_Info)

    dobInfo setText (
      if (mGameSettings contains GAME_PREFERENCES_DOB)
        DateFormat.format("MMMM dd, yyyy", mGameSettings.getLong(GAME_PREFERENCES_DOB, 0))
      else
        R.string.settings_dob_not_set.toString
    )

    // Handle date picking dialog
    val pickDate = findButton(R.id.Button_DOB)
    pickDate setOnClickListener {
      showDialog(DATE_DIALOG_ID)
    }
  }

  /**
   * Initialize the spinner
   */
  var spinnerInitCall = true

  private def initGenderSpinner() {
    // Populate Spinner control with genders
    val spinner = findSpinner(R.id.Spinner_Gender)
    val adapter = ArrayAdapter.createFromResource(this, R.array.genders, android.R.layout.simple_spinner_item)
    adapter setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    spinner setAdapter adapter

    if (mGameSettings contains GAME_PREFERENCES_GENDER)
      spinner.setSelection(mGameSettings.getInt(GAME_PREFERENCES_GENDER, 0))

    // Handle spinner selections
    spinner setOnItemSelectedListener new AdapterView.OnItemSelectedListener() {
      def onItemSelected(parent: AdapterView[_], itemSelected: View,
                         selectedItemPosition: Int, selectedId: Long) {
        if (!spinnerInitCall) {
          val editor = mGameSettings.edit
          editor.putInt(GAME_PREFERENCES_GENDER, selectedItemPosition)
          editor.commit()
          // ... and server data
          updateServerData()
        } else {
          spinnerInitCall = false
        }
      }

      def onNothingSelected(parent: AdapterView[_]) {}
    }

  }

  /**
   * Initialize the Favorite Place picker
   */
  private def initFavoritePlacePicker() {
    // Set place info
    val placeInfo = findTextView(R.id.TextView_FavoritePlace_Info)

    placeInfo setText (
      if (mGameSettings contains GAME_PREFERENCES_FAV_PLACE_NAME)
        mGameSettings.getString(GAME_PREFERENCES_FAV_PLACE_NAME, "")
      else
        R.string.settings_favoriteplace_not_set.toString
    )

    // Handle place picking dialog
    val pickPlace = findButton(R.id.Button_FavoritePlace)
    pickPlace setOnClickListener {
      showDialog(PLACE_DIALOG_ID)
    }
  }

  override protected def onCreateDialog(id: Int): Dialog = {
    val layoutInflater = getLayoutInflater
    id match {
      case PLACE_DIALOG_ID =>
        val dialogLayout = layoutInflater.inflate(R.layout.fav_place_dialog, findViewGroup(R.id.root))

        val placeCoordinates = dialogLayout findTextView R.id.TextView_FavPlaceCoords_Info
        val placeName = dialogLayout findTextView R.id.EditText_FavPlaceName
        placeName setOnKeyListener new View.OnKeyListener() {
          def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
            if ((event.getAction == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
              val strPlaceName = placeName.getText.toString
              if ((strPlaceName != null) && (strPlaceName.length > 0)) {
                // Try to resolve string into GPS coords
                resolveLocation(strPlaceName)

                val editor = mGameSettings.edit
                editor.putString(GAME_PREFERENCES_FAV_PLACE_NAME, placeName.getText.toString)
                editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LONG, mFavPlaceCoords.longitude)
                editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LAT, mFavPlaceCoords.latitude)
                editor.commit()

                // ... and server data
                updateServerData()

                placeCoordinates setText formatCoordinates(mFavPlaceCoords.latitude, mFavPlaceCoords.longitude)
                true
              } else
                false
            }
            false
          } // onKey
        }

        val mapButton = dialogLayout findButton R.id.Button_MapIt
        mapButton setOnClickListener new View.OnClickListener() {
          def onClick(v: View) {
            // Try to resolve string into GPS coords
            val placeToFind = placeName.getText.toString
            resolveLocation(placeToFind)

            val editor = mGameSettings.edit
            editor.putString(GAME_PREFERENCES_FAV_PLACE_NAME, placeToFind)
            editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LONG, mFavPlaceCoords.longitude)
            editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LAT, mFavPlaceCoords.latitude)
            editor.commit()

            // ... and server data
            updateServerData()

            placeCoordinates setText formatCoordinates(mFavPlaceCoords.latitude, mFavPlaceCoords.longitude)

            // Launch map with gps coords
            val geoURI = String.format("geo:%f,%f?z=10",
              mFavPlaceCoords.latitude.asInstanceOf[AnyRef],
              mFavPlaceCoords.longitude.asInstanceOf[AnyRef])
            val geo = Uri.parse(geoURI)
            val geoMap = new Intent(Intent.ACTION_VIEW, geo)
            startActivity(geoMap)
          }
        }

        val dialogBuilder = new AlertDialog.Builder(this)
        dialogBuilder setView dialogLayout

        // Now configure the AlertDialog
        dialogBuilder setTitle R.string.settings_button_favoriteplace

        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, whichButton: Int) {
            // We forcefully dismiss and remove the Dialog, so it cannot
            // be used again (no cached info)
            QuizSettingsActivity.this removeDialog PLACE_DIALOG_ID
          }
        })

        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, which: Int) {

          val placeInfo = findTextView(R.id.TextView_FavoritePlace_Info)
          val strPlaceName = placeName.getText.toString

          if ((strPlaceName != null) && (strPlaceName.length > 0)) {
            val editor = mGameSettings.edit
            editor.putString(GAME_PREFERENCES_FAV_PLACE_NAME, strPlaceName)
            editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LONG, mFavPlaceCoords.longitude)
            editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LAT, mFavPlaceCoords.latitude)
            editor.commit()

            // ... and server data
            updateServerData()

            placeInfo setText strPlaceName
          }

          // We forcefully dismiss and remove the Dialog, so it cannot be used again
          QuizSettingsActivity.this removeDialog PLACE_DIALOG_ID
        }
      })

      // Create the AlertDialog and return it
      val placeDialog = dialogBuilder.create()
      placeDialog

    case DATE_DIALOG_ID =>
      val dob = findTextView(R.id.TextView_DOB_Info)

      val dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
        def onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
          val dateOfBirth = new Time()
          dateOfBirth.set(dayOfMonth, monthOfYear, year)
          val dtDob = dateOfBirth.toMillis(true)
          dob setText DateFormat.format("MMMM dd, yyyy", dtDob)

          val editor = mGameSettings.edit
          editor.putLong(GAME_PREFERENCES_DOB, dtDob)
          editor.commit()

          // ... and server data
          updateServerData()
        }
      }, 0, 0, 0)
      dateDialog

    case FRIEND_EMAIL_DIALOG_ID =>
      val friendDialogLayout = layoutInflater.inflate(R.layout.friend_entry, findViewGroup(R.id.root))

      val friendDialogBuilder = new AlertDialog.Builder(this)
      friendDialogBuilder setView friendDialogLayout
      val emailText = friendDialogLayout findTextView R.id.EditText_Friend_Email

      friendDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which: Int) {
          val friendEmail = emailText.getText.toString
          if (friendEmail != null && friendEmail.length > 0) {
            doFriendRequest(friendEmail)
          }
        }
      })
      friendDialogBuilder.create()

    case PASSWORD_DIALOG_ID =>
      val layout = layoutInflater.inflate(R.layout.password_dialog, findViewGroup(R.id.root))

      val p1 = layout findEditText R.id.EditText_Pwd1
      val p2 = layout findEditText R.id.EditText_Pwd2
      val error = layout findTextView R.id.TextView_PwdProblem

      p2 addTextChangedListener new TextWatcher() {
        override def afterTextChanged(s: Editable) {
          val strPass1 = p1.getText.toString
          val strPass2 = p2.getText.toString

          error setText (
            if (strPass1 equals strPass2) R.string.settings_pwd_equal
            else R.string.settings_pwd_not_equal
          )
        }
        // ... other required overrides do nothing
        override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
      }

      val builder = new AlertDialog.Builder(this)
      builder setView layout

      // Now configure the AlertDialog
      builder setTitle R.string.settings_button_pwd

      builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, whichButton: Int) {
          // We forcefully dismiss and remove the Dialog, so it cannot be used again (no cached info)
          QuizSettingsActivity.this.removeDialog(PASSWORD_DIALOG_ID)
        }
      })

      builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which: Int) {
          val passwordInfo = findTextView(R.id.TextView_Password_Info)

          val strPassword1 = p1.getText.toString
          val strPassword2 = p2.getText.toString

          if (strPassword1 equals strPassword2) {
            val editor = mGameSettings.edit
            editor.putString(GAME_PREFERENCES_PASSWORD, strPassword1)
            editor.commit()

            // ... and server data
            updateServerData()

            passwordInfo setText R.string.settings_pwd_set
          } else {
            Log.d(DEBUG_TAG, "Passwords do not match. Not saving. Keeping old password (if set).")
          }

          // We forcefully dismiss and remove the Dialog, so it cannot be used again
          QuizSettingsActivity.this removeDialog PASSWORD_DIALOG_ID
        }
      })

      // Create the AlertDialog and return it
      val passwordDialog = builder.create()
      passwordDialog
    }
    null
  }

  override protected def onPrepareDialog(id: Int, dialog: Dialog) {
    super.onPrepareDialog(id, dialog)

    id match {
      case PLACE_DIALOG_ID =>
        // Handle any Favorite Place Dialog initialization here
        val placeDialog = dialog.asInstanceOf[AlertDialog]

        var strFavPlaceName: String = null

        // Check for favorite place preference
        if (mGameSettings contains GAME_PREFERENCES_FAV_PLACE_NAME) {
          // Retrieve favorite place from preferences
          strFavPlaceName = mGameSettings.getString(GAME_PREFERENCES_FAV_PLACE_NAME, "")
          mFavPlaceCoords = new GPSCoords(mGameSettings.getFloat(GAME_PREFERENCES_FAV_PLACE_LAT, 0), mGameSettings.getFloat(GAME_PREFERENCES_FAV_PLACE_LONG, 0))
        } else {
          // No favorite place set, set coords to current location
          strFavPlaceName = getResources getString R.string.settings_favplace_currentlocation
          // We do not name this place ("here"), but use it as a map point.
          // User can supply the name if they like
          calculateCurrentCoordinates()
        }

        // Set the placename text and coordinates either to the saved values,
        // or just set the GPS coords to the current location
        val placeName = placeDialog findEditText R.id.EditText_FavPlaceName
        placeName setText strFavPlaceName

        val placeCoordinates = placeDialog findTextView R.id.TextView_FavPlaceCoords_Info
        placeCoordinates setText formatCoordinates(mFavPlaceCoords.latitude, mFavPlaceCoords.longitude)

      case DATE_DIALOG_ID =>
        // Handle any DatePickerDialog initialization here
        val dateDialog = dialog.asInstanceOf[DatePickerDialog]
        var iDay, iMonth, iYear = 0

        // Check for date of birth preference
        if (mGameSettings contains GAME_PREFERENCES_DOB) {
          // Retrieve Birth date setting from preferences
          val msBirthDate = mGameSettings.getLong(GAME_PREFERENCES_DOB, 0)
          val dateOfBirth = new Time()
          dateOfBirth set msBirthDate

          iDay = dateOfBirth.monthDay
          iMonth = dateOfBirth.month
          iYear = dateOfBirth.year
        } else {
          val cal = Calendar.getInstance

          // Today's date fields
          iDay = cal get Calendar.DAY_OF_MONTH
          iMonth = cal get Calendar.MONTH
          iYear = cal get Calendar.YEAR
        }

        // Set the date in the DatePicker to the date of birth OR to the current date
        dateDialog.updateDate(iYear, iMonth, iDay)

      case PASSWORD_DIALOG_ID =>
        // Handle any Password Dialog initialization here
        // Since we don't want to show old password dialogs, just set new ones,
        // we need not do anything here
        // Because we are not "reusing" password dialogs once they have
        // finished, but removing them from
        // the Activity Dialog pool explicitly with removeDialog() and
        // recreating them as needed.
    }
  }

  /**
   * Helper to format coordinates for screen display
   *
   * @param lat
   * @param lon
   * @return A string formatted accordingly
   */
  private def formatCoordinates(lat: Float, lon: Float): String = {
    val strCoords = new StringBuilder()
    strCoords append lat append "," append lon
    strCoords.toString
  }

  /**
   * If location name can't be determined, try to determine location
   * based on current coords
   *
   * @param strLocation
   *            Location or place name to try
   */
  private def resolveLocation(strLocation: String) {
    val curLocation = getResources getString R.string.settings_favplace_currentlocation

    val bResolvedAddress =
      if (strLocation equalsIgnoreCase curLocation) false
      else lookupLocationByName(strLocation)

    if (!bResolvedAddress) {
      // If String place name could not be determined (or matches the string
      // for "current location", assume this is a custom name of the current location
      calculateCurrentCoordinates()
    }
  }

  /**
   * Attempt to get the last known location of the device. Usually this is
   * the last value that a location provider set
   */
  private def calculateCurrentCoordinates() {
    val (latitude, longitude) = try {
      val locMgr = this getLocationManager
      val recentLoc = locMgr getLastKnownLocation LocationManager.GPS_PROVIDER
      (recentLoc.getLatitude.toFloat, recentLoc.getLongitude.toFloat)
    } catch {
      case e: Exception =>
        Log.e(DEBUG_TAG, "Location failed", e)
        (0f, 0f)
    }
    mFavPlaceCoords = new GPSCoords(latitude, longitude)
  }

  /**
   * Take a description of a location, store the coordinates in mFavPlaceCoords
   *
   * @param strLocation
   *            The location or placename to look up
   * @return true if the address or place was recognized, otherwise false
   */
  private def lookupLocationByName(strLocation: String): Boolean = {
    val coder = new Geocoder(getApplicationContext)
    var bResolvedAddress = false

    try {
      val geocodeResults = coder.getFromLocationName(strLocation, 1)
      val locations = geocodeResults.iterator

      while (locations.hasNext) {
        val loc = locations.next()
        mFavPlaceCoords = new GPSCoords(loc.getLatitude.toFloat, loc.getLongitude.toFloat)
        bResolvedAddress = true
      }
    } catch {
      case e: Exception =>
        Log.e(DEBUG_TAG, "Failed to geocode location", e)
    }
    bResolvedAddress
  }

  private case class GPSCoords(latitude: Float, longitude: Float)

  private class FriendRequestTask extends AsyncTask[String, AnyRef, Boolean] {
    override protected def onPostExecute(result: Boolean) {
      QuizSettingsActivity.this setProgressBarIndeterminateVisibility false
    }

    override protected def onPreExecute() {
      QuizSettingsActivity.this setProgressBarIndeterminateVisibility true
    }

    override protected def doInBackground(params: String*): Boolean = {
      var succeeded = false
      try {
        val friendEmail = params(0)

        val prefs = getSharedPreferences(GAME_PREFERENCES, Context.MODE_PRIVATE)
        val playerId = prefs.getInt(GAME_PREFERENCES_PLAYER_ID, -1)

        val vars = new Vector[NameValuePair]()
        vars add new BasicNameValuePair("command", "add")
        vars add new BasicNameValuePair("playerId", playerId.toString)
        vars add new BasicNameValuePair("friend", friendEmail)

        val client = new DefaultHttpClient()

        // an example of using HttpClient with HTTP POST and form variables
        val request = new HttpPost(TRIVIA_SERVER_FRIEND_EDIT)
        request setEntity new UrlEncodedFormEntity(vars)

        val responseHandler: ResponseHandler[String] = new BasicResponseHandler()
        val responseBody = client.execute(request, responseHandler)

        Log.d(DEBUG_TAG, "Add friend result: " + responseBody)
        if (responseBody != null) {
          succeeded = true
        }

      } catch {
        case e: MalformedURLException =>
          Log.e(DEBUG_TAG, "Failed to add friend", e)
        case e: IOException =>
          Log.e(DEBUG_TAG, "Failed to add friend", e)
      }

      succeeded
    }

  }

  private object ImageUploadTask {
    private final val DEBUG_TAG = "ImageUploadTask"
  }

  private class ImageUploadTask extends AsyncTask[AnyRef, String, Boolean] {
    import ImageUploadTask._ // companion object

    var pleaseWaitDialog: ProgressDialog = _

    override protected def onCancelled() {
      Log.i(DEBUG_TAG, "onCancelled")
      pleaseWaitDialog.dismiss()
    }

    override protected def onPostExecute(result: Boolean) {
      Log.i(DEBUG_TAG, "onPostExecute")
      pleaseWaitDialog.dismiss()
    }

    override protected def onPreExecute() {
      pleaseWaitDialog = ProgressDialog.show(QuizSettingsActivity.this, "Trivia Quiz", "Uploading avatar image...", true, true)
      pleaseWaitDialog setOnCancelListener new OnCancelListener() {
        def onCancel(dialog: DialogInterface) {
          ImageUploadTask.this cancel true
        }
      }
    }

    override protected def doInBackground(params: AnyRef*): Boolean = {
      // an example using HttpClient and HttpMime to upload a file via HTTP POST
      // in the same way a web browser might, using multipart MIME encoding
      val avatar = mGameSettings.getString(GAME_PREFERENCES_AVATAR, "")
      val playerId = mGameSettings.getInt(GAME_PREFERENCES_PLAYER_ID, -1)

      val entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
      val file = new File(avatar)
      val encFile = new FileBody(file)

      entity.addPart("avatar", encFile)
      try {
        entity.addPart("updateId", new StringBody(playerId.toString))
      } catch {
        case e: UnsupportedEncodingException =>
          Log.e(DEBUG_TAG, "Failed to add form field.", e)
      }

      val request = new HttpPost(TRIVIA_SERVER_ACCOUNT_EDIT)
      request setEntity entity

      val client = new DefaultHttpClient()

      try {
        val responseHandler: ResponseHandler[String] = new BasicResponseHandler()
        val responseBody = client.execute(request, responseHandler)

        if (responseBody != null && responseBody.length > 0) {
          Log.w(DEBUG_TAG, "Unexpected response from avatar upload: " + responseBody)
        }
      } catch {
        case e: ClientProtocolException =>
          // TODO Auto-generated catch block
          e.printStackTrace()
        case e: IOException =>
         // TODO Auto-generated catch block
          e.printStackTrace()
      }
      true
    }

  }

  private class AccountTask extends AsyncTask[AnyRef, String, Boolean] {

    override protected def onPostExecute(result: Boolean) {
      QuizSettingsActivity.this setProgressBarIndeterminateVisibility false
    }

    override protected def onPreExecute() {
      QuizSettingsActivity.this setProgressBarIndeterminateVisibility true
    }

    override protected def doInBackground(params: AnyRef*): Boolean = {
      var succeeded = false

      // an example of using HttpClient with HTTP GET and form variables

      val playerId = mGameSettings.getInt(GAME_PREFERENCES_PLAYER_ID, -1)
      val nickname = mGameSettings.getString(GAME_PREFERENCES_NICKNAME, "")
      val email = mGameSettings.getString(GAME_PREFERENCES_EMAIL, "")
      val password = mGameSettings.getString(GAME_PREFERENCES_PASSWORD, "")
      val score = mGameSettings.getInt(GAME_PREFERENCES_SCORE, -1)
      val gender = mGameSettings.getInt(GAME_PREFERENCES_GENDER, -1)
      val birthdate = mGameSettings.getLong(GAME_PREFERENCES_DOB, 0)
      val favePlaceName = mGameSettings.getString(GAME_PREFERENCES_FAV_PLACE_NAME, "")

      val vars = new Vector[NameValuePair]()

      if (playerId == -1) {
        // if we don't have a playerId yet, we must pass up a uniqueId
        // A good place to get a unique identifier from is the device Id
        // which is conveniently store in the TelephonyManager data
        // This requires the use of READ_PHONE_STATE permission
        val telManager = QuizSettingsActivity.this getTelephonyManager
        val uniqueId = telManager.getDeviceId

        // hash the value to get a unique, but non-identifiable value to use
        val mdUniqueId = try {
          val sha = MessageDigest.getInstance("SHA")

          val enc = sha digest uniqueId.getBytes
          val sb = new StringBuilder()

          for (enc1 <- enc) {
            sb append Integer.toHexString(enc1 & 0xFF)
          }
          sb.toString
        } catch {
          case e: NoSuchAlgorithmException =>
            Log.w(DEBUG_TAG, "Failed to get SHA, using hashcode()")
            String.valueOf(uniqueId.hashCode)
        }
        vars add new BasicNameValuePair("uniqueId", mdUniqueId)

      } else {
        // otherwise, we use the playerId to update data
        vars add new BasicNameValuePair("updateId", playerId.toString)
        // and we go ahead and push up the latest score
        vars add new BasicNameValuePair("score", score.toString)
      }
      vars add new BasicNameValuePair("nickname", nickname)
      vars add new BasicNameValuePair("email", email)
      vars add new BasicNameValuePair("password", password)
      vars add new BasicNameValuePair("gender", gender.toString)
      vars add new BasicNameValuePair("faveplace", favePlaceName)
      vars add new BasicNameValuePair("dob", birthdate.toString)

      val url = TRIVIA_SERVER_ACCOUNT_EDIT + "?" + URLEncodedUtils.format(vars, null)

      val request = new HttpGet(url)
      try {
        val responseHandler: ResponseHandler[String] = new BasicResponseHandler()
        val client = new DefaultHttpClient()
        val responseBody = client.execute(request, responseHandler)

        if (responseBody != null && responseBody.length > 0) {
          val resultId = Integer.parseInt(responseBody)
          val editor = mGameSettings.edit
          editor.putInt(GAME_PREFERENCES_PLAYER_ID, resultId)
          editor.commit()
          succeeded = true
        }

      } catch {
        case e: ClientProtocolException =>
          Log.e(DEBUG_TAG, "Failed to get playerId (protocol): ", e)
        case e: IOException =>
          Log.e(DEBUG_TAG, "Failed to get playerId (io): ", e)
      }
      succeeded
    }
  }
}

object QuizSettingsActivity {
  final val DATE_DIALOG_ID = 0
  final val PASSWORD_DIALOG_ID = 1
  final val PLACE_DIALOG_ID = 2
  final val FRIEND_EMAIL_DIALOG_ID = 3

  final val TAKE_AVATAR_CAMERA_REQUEST = 1
  final val TAKE_AVATAR_GALLERY_REQUEST = 2
}
