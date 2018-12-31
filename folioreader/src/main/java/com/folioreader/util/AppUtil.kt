package com.folioreader.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.util.SharedPreferenceUtil.getSharedPreferencesString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.ServerSocket
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by mahavir on 5/7/16.
 */
class AppUtil {

    companion object {

        private val SMIL_ELEMENTS = "smil_elements"
        private val LOG_TAG = AppUtil::class.java.simpleName
        private val FOLIO_READER_ROOT = "folioreader"

        private enum class FileType {
            OPS,
            OEBPS,
            NONE
        }

        fun toMap(jsonString: String): Map<String, String> {
            val map = HashMap<String, String>()
            try {
                val jsonArray = JSONArray(jsonString)
                val jObject = jsonArray.getJSONObject(0)
                val keysItr = jObject.keys()
                while (keysItr.hasNext()) {
                    val key = keysItr.next()
                    var value: Any? = null
                    value = jObject.get(key)

                    if (value is JSONObject) {
                        value = toMap(value.toString())
                    }
                    map[key] = value!!.toString()
                }
            } catch (e: JSONException) {
                Log.e(LOG_TAG, "toMap failed", e)
            }

            return map
        }

        @JvmStatic
        fun charsetNameForURLConnection(connection: URLConnection): String {
            // see https://stackoverflow.com/a/3934280/1027646
            val contentType = connection.contentType
            val values = contentType.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var charset: String? = null

            for (_value in values) {
                val value = _value.trim { it <= ' ' }

                if (value.toLowerCase().startsWith("charset=")) {
                    charset = value.substring("charset=".length)
                    break
                }
            }

            if (charset == null || charset.isEmpty()) {
                charset = "UTF-8" //Assumption
            }

            return charset
        }

        @JvmStatic
        fun formatDate(hightlightDate: Date): String {
            val simpleDateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
            return simpleDateFormat.format(hightlightDate)
        }

        fun saveConfig(context: Context?, config: Config) {
            val obj = JSONObject()
            try {
                obj.put(Config.CONFIG_FONT, config.font)
                obj.put(Config.CONFIG_FONT_SIZE, config.fontSize)
                obj.put(Config.CONFIG_IS_NIGHT_MODE, config.isNightMode)
                obj.put(Config.CONFIG_THEME_COLOR_INT, config.themeColor)
                obj.put(Config.CONFIG_IS_TTS, config.isShowTts)
                obj.put(Config.CONFIG_ALLOWED_DIRECTION, config.allowedDirection.toString())
                obj.put(Config.CONFIG_DIRECTION, config.direction.toString())
                SharedPreferenceUtil.putSharedPreferencesString(
                    context, Config.INTENT_CONFIG,
                    obj.toString()
                )
            } catch (e: JSONException) {
                Log.e(LOG_TAG, e.message)
            }

        }

        @JvmStatic
        fun getSavedConfig(context: Context?): Config? {
            val json = getSharedPreferencesString(context, Config.INTENT_CONFIG, null)
            if (json != null) {
                try {
                    val jsonObject = JSONObject(json)
                    return Config(jsonObject)
                } catch (e: JSONException) {
                    Log.e(LOG_TAG, e.message)
                    return null
                }

            }
            return null
        }

        fun actionToString(action: Int): String {
            when (action) {
                MotionEvent.ACTION_DOWN -> return "ACTION_DOWN"
                MotionEvent.ACTION_UP -> return "ACTION_UP"
                MotionEvent.ACTION_CANCEL -> return "ACTION_CANCEL"
                MotionEvent.ACTION_OUTSIDE -> return "ACTION_OUTSIDE"
                MotionEvent.ACTION_MOVE -> return "ACTION_MOVE"
                MotionEvent.ACTION_HOVER_MOVE -> return "ACTION_HOVER_MOVE"
                MotionEvent.ACTION_SCROLL -> return "ACTION_SCROLL"
                MotionEvent.ACTION_HOVER_ENTER -> return "ACTION_HOVER_ENTER"
                MotionEvent.ACTION_HOVER_EXIT -> return "ACTION_HOVER_EXIT"
            }

            if (Build.VERSION.SDK_INT >= 23) {
                when (action) {
                    MotionEvent.ACTION_BUTTON_PRESS -> return "ACTION_BUTTON_PRESS"
                    MotionEvent.ACTION_BUTTON_RELEASE -> return "ACTION_BUTTON_RELEASE"
                }
            }

            val index = action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
            when (action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> return "ACTION_POINTER_DOWN($index)"
                MotionEvent.ACTION_POINTER_UP -> return "ACTION_POINTER_UP($index)"
                else -> return Integer.toString(action)
            }
        }

        fun hideKeyboard(activity: Activity) {

            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token
            if (view == null)
                view = View(activity)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }

        fun getAvailablePortNumber(portNumber: Int): Int {

            var serverSocket: ServerSocket? = null
            var portNumberAvailable: Int

            try {
                serverSocket = ServerSocket(portNumber)
                Log.d(LOG_TAG, "-> getAvailablePortNumber -> portNumber $portNumber available")
                portNumberAvailable = portNumber
            } catch (e: Exception) {
                serverSocket = ServerSocket(0)
                portNumberAvailable = serverSocket.localPort
                Log.w(
                    LOG_TAG, "-> getAvailablePortNumber -> portNumber $portNumber not available, " +
                            "$portNumberAvailable is available"
                )
            } finally {
                serverSocket?.close()
            }

            return portNumberAvailable
        }
    }
}







