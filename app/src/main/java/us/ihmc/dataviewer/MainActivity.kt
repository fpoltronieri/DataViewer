package us.ihmc.dataviewer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import com.github.chrisbanes.photoview.PhotoView


class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    //constant values
    val WESPermission = 0x11
    val TAGDEBUG: String = "DEBUG"

    lateinit  var gestureDetector: GestureDetectorCompat
    lateinit var zoomView : PhotoView
    var uriImage : Uri? = null

    /**
     * Code to require permission at the stratup
     * Requiring permission to write on the External Storage
     */
    fun verifyPermission(activity: Activity) {
        val permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WESPermission)
            Log.d(TAGDEBUG, "Requesting permission")
        } else {
            Log.d(TAGDEBUG, "The app has already the permissions")
        }
    }

    /**
     * the function handles the result of the permissions request
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WESPermission -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAGDEBUG, "Permission granted")
                } else {
                    Log.d(TAGDEBUG, "Permission not granted closing the application")
                    System.exit(-1);
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAGDEBUG, "onCreate() called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        verifyPermission(this)
        //set the gestureDetectore for the application
        gestureDetector = GestureDetectorCompat(this,this)
        gestureDetector.setOnDoubleTapListener(this)
        //set the zoomable view
        zoomView = findViewById(R.id.zoomView) as PhotoView
        //override the onDoubleTapListener for the view, onDoubleTap browse the filesystem
        zoomView.setOnDoubleTapListener(this)

        //get more button
        var getmoreButton = findViewById(R.id.button_getmore) as Button
        getmoreButton.setOnClickListener(
                {v -> Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show()}
        )

        //select button
        val selectButton = findViewById(R.id.button_select) as Button
        selectButton.setOnClickListener({
            v-> val intent = Intent(this, SelectActivity::class.java)
            intent.putExtra("uri", uriImage)
            startActivity(intent)
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector?.onTouchEvent(event)
        Log.d(TAGDEBUG, "Calling the superclass implementation")
        return super.onTouchEvent(event)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.d("DEBUG:", "DoubleTap Recognized");
        var intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, "Select an Image to display"), 1)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
        }

        return true
    }


    /**
     * Handle the selection of a picture
     * display the image in the Activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAGDEBUG, "onActivityResult called!")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (data == null) {
                Toast.makeText(this, "Error, something went wrong", Toast.LENGTH_SHORT).show()
                return
            }
            Log.d(TAGDEBUG, "Picture selected: " + data.data.path)
            Toast.makeText(this, "Picture selected " + data.data.path, Toast.LENGTH_SHORT).show()
            uriImage = data.data
            Log.d(TAGDEBUG, "Uri set for the last image " + uriImage)
            zoomView.setImageURI(uriImage)
        }
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(TAGDEBUG, "onLongPress Recognized")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.d(TAGDEBUG, "action currently not implemented " + e.toString())
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(TAGDEBUG, "action currently not implemented " + e.toString())
        return false
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        Log.d(TAGDEBUG, "action currently not implemented " + e1.toString())
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        Log.d(TAGDEBUG, "action currently not implemented " + e1.toString())
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        Log.d(TAGDEBUG, "action currently not implemented " + e.toString())
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.d(TAGDEBUG, "action currently not implemented " + e.toString())
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
        Log.d(TAGDEBUG, "action currently not implemented " + e.toString())
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAGDEBUG, "Called onResume()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAGDEBUG, "Called function onDestroy")
    }
}
