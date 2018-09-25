package nl.utwente.ewi.scs.secretnotestaker.secretnotestakerultra

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_takes_notes.*
import android.media.AudioManager
import android.content.Context.AUDIO_SERVICE




class TakesNotesActivity : AppCompatActivity() {
    var mMyApp: MyApplication? = null
    lateinit var updater: BackgroundUpdater
    var etNotes :EditText?=null

    fun updateImage(bitmap: Bitmap) {
        val view = findViewById<ImageView>(R.id.imageView)
        view.setImageBitmap(bitmap)
    }

    override fun onResume() {
        super.onResume()
        mMyApp!!.currentActivity = this
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_takes_notes)
        setSupportActionBar(toolbar)
        mMyApp = this.applicationContext as MyApplication?
        etNotes = findViewById<EditText>(R.id.editText)

        m = MediaPlayer()


        val et2 = etNotes

        et2!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(getNumber()!=-1)
                {
                    //Block the ET
                    et2.isEnabled = false

                    sendPinCode(getNumber())
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mMyApp!!.currentActivity = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mMyApp!!.currentActivity = null
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_takes_notes, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true

            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getNumber(): Int {
        val pinStr = etNotes!!.text.toString()
        if(pinStr.length!=4)
        {
            return -1
        }
        try {
            val pinInt = pinStr.toInt()
            if (pinInt>=0 && pinInt<=9999)
            {
                return pinInt
            }else
            {
                return -1
            }
        }catch (e: Exception) {
            Log.e("Network", "Not a valid number", e)
            return -1
        }

    }

    private fun sendPinCode(number: Int) {
        //Set volume to max
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0)

        //Select the file
        val descriptor = getAssets().openFd("17000x.wav")
        m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength())
        descriptor.close()
        m.prepare()

        playBeep(number,1)
    }

    private fun getNumberDigitSum(number: Int): Int
    {
        var sum = 0
        var num = number
        while (num > 0) {
            sum = sum + num % 10
            num = num / 10
        }
        return sum + 4
    }

    val LOG_TAG = "PlaySound"
    lateinit var m: MediaPlayer
    var i = 0
    fun playBeep(number: Int, i1: Int) {

        try {
            // m.setVolume(1f, 1f)
            m.setLooping(true)
            m.start()
            Log.i(LOG_TAG,"start sound: " + number%10 + " wtf" + 23)

            val handler = Handler()
            handler.postDelayed(Runnable {
                m.pause()

                if(i1<4)
                {
                   /* m = MediaPlayer()
                    val descriptor = getAssets().openFd("17000x.wav")
                    m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength())
                    descriptor.close()
                    m.prepare()*/

                    handler.postDelayed(Runnable {
                        playBeep(number/10, i1+1)
                    },1000)
                }else
                {
                    etNotes!!.setText("")
                    etNotes!!.isEnabled = true
                    m.stop()
                    m = MediaPlayer()
                }

                Log.i(LOG_TAG,"stop sound")
            }, ((number%10 + 1)*1000).toLong())


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
