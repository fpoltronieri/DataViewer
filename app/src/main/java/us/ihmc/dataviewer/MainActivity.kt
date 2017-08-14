package us.ihmc.dataviewer

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import com.github.chrisbanes.photoview.PhotoView
import us.ihmc.dataviewer.util.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    //constant values
    val URIIMAGE = "URIIMAGE"
    val WESPermission = 0x11
    val TAGDEBUG: String = "DEBUG"
    val TAKE_PICTURE = 2
    //constant values for SWIPE
    val SWIPE_MIN_DIST = 100
    val SWIPE_MIN_VEL = 100

    lateinit var gestureDetector: GestureDetectorCompat
    lateinit var zoomView: PhotoView
    lateinit var getmoreButton: Button
    lateinit var appBundle: Bundle

    var uriImage: Uri? = null
    var camUriImage: Uri? = null

    private var mDiscoveredChunks = ConcurrentHashMap<String?, Intent?>()
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
                    System.exit(-1)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAGDEBUG, "onCreate() called")
        super.onCreate(savedInstanceState)
        appBundle = Bundle()
        setContentView(R.layout.activity_main)
        verifyPermission(this)
        //the application has received an intent to take a picture
        val action = intent.action.toString()
        if (Action.ADD_MESSAGE.toString().equals(action)) {
            Log.d(TAGDEBUG, "Received intent with action: $action")
            takePhoto()
        }
        //set the gestureDetectore for the application
        gestureDetector = GestureDetectorCompat(this, this)
        gestureDetector.setOnDoubleTapListener(this)
        //set the zoomable view
        zoomView = findViewById(R.id.zoomView) as PhotoView
        //override the onDoubleTapListener for the view, onDoubleTap browse the filesystem
        zoomView.setOnDoubleTapListener(this)
        //swipe to show the MetaData
        zoomView.setOnSingleFlingListener { e1, e2, velocityX, velocityY ->
            this.onFling(e1, e2, velocityX, velocityY)
        }
        //get more button
        getmoreButton = findViewById(R.id.button_getmore) as Button
        getmoreButton.setOnClickListener(
                { _ -> Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show() }
        )

        //select button
        val selectButton = findViewById(R.id.button_select) as Button
        selectButton.setOnClickListener({
            _ ->
            val intent = Intent(this, SelectActivity::class.java)
            intent.putExtra("uri", uriImage)
            startActivity(intent)
        })
    }

    /**
     * Preserve the uriImage after configuration changes
     */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE
                || newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT)
            onSaveInstanceState(appBundle)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(URIIMAGE, uriImage)
        Log.d(TAGDEBUG, "Called onSaveInstanceState, value for uriImage = " + uriImage)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAGDEBUG, "Called onRestoreInstanceState")
        uriImage = savedInstanceState?.getParcelable(URIIMAGE)
        zoomView.setImageURI(uriImage)
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
        when (requestCode) {
            1 -> {
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
            TAKE_PICTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    sendAddMessege(camUriImage?.path, "image/jpg")
                    Log.d(TAGDEBUG, "ActivityResult OK, send report to DSPro:  $camUriImage?.path")
                    onBackPressed()
                }
            }
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

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        var xDistance : Float = Math.abs(e1.x - e2.x)
        var xVelocity = Math.abs(velocityX)
        if (xVelocity >= SWIPE_MIN_VEL && xDistance >= SWIPE_MIN_DIST) {
            Toast.makeText(applicationContext, "Swipe with distance: $xDistance and velocity $xVelocity", Toast.LENGTH_SHORT).show()
            return true
        }
        Log.d(TAGDEBUG, "Swipe do not recognized $e1 $e2 $velocityX $velocityY")
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

    private fun updateChunkCount(intent: Intent?) {

        val hasMoreChunks = intent?.getBooleanExtra(Key.HAS_MORE_CHUNKS.toString(), false)
        val chunkCount = intent?.getStringExtra(Key.CHUNK_COUNT.toString())
        Log.d(TAGDEBUG, "hasMoreChunks: $hasMoreChunks chunckCount: $chunkCount")
        if (hasMoreChunks?:false) {
            getmoreButton.isEnabled = false
        }
        if (!chunkCount.equals("")) {
            getmoreButton.text = getString(R.string.action_get_more_chunks) + " " + chunkCount
        }
        //Update the counter on the button
        /*
        if (hasMoreChunks) {
            mBtnGetMoreChunks.setEnabled(true)
            mBtnGetMoreChunks.setTextColor(Color.WHITE)
        } else {
            mBtnGetMoreChunks.setEnabled(false)
            mBtnGetMoreChunks.setTextColor(Color.GRAY)
        }
        if (chunkCount != "") {
            mBtnGetMoreChunks.setText(getString(R.string.action_get_more_chunks) + " " + chunkCount)
        }
        */
    }

    /**
     * Open the camera app to take a picture
     */
    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val filename = SimpleDateFormat("yyyyMMddHHmm", Locale.US).format(Date())
        val photo = File(StoreTask.getStorageDirectory(), "$filename.jpg")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo))
        camUriImage = Uri.fromFile(photo)
        startActivityForResult(intent, TAKE_PICTURE)
    }

    /**
     * ADD a Message using DSPro
     */
    private fun sendAddMessege(filename : String?, mimeType: String?) {
        val intent = Intent()
        intent.setAction(Action.ADD_MESSAGE.toString())
        val bundle = Bundle()
        bundle.putString(Key.NAME.toString(), filename)
        bundle.putString(Key.MIME_TYPE.toString(), mimeType)
        intent.putExtras(bundle)
        Log.d(TAGDEBUG, "Sending ${Action.ADD_MESSAGE.toString()} in broadcast with mime type: $mimeType filenameL $filename")
        applicationContext.sendBroadcast(intent)
    }


    /**
     * Request a GET_DATA to the DSProProxy
     */
    private fun sendGetData(filename: String?, messageId: String?, mimeType: String?) {
        val intent = Intent()
        intent.setAction(Action.GET_DATA.toString())
        val bundle = Bundle()
        bundle.putString(Key.MESSAGE_ID.toString(), messageId)
        bundle.putString(Key.NAME.toString(), filename)
        bundle.putString(Key.MIME_TYPE.toString(), mimeType)
        intent.putExtras(bundle)
        Log.d(TAGDEBUG, "Sending  $Action.GET_DATA.toString() in broadcast with messageId: $messageId  filename: $filename")
        applicationContext.sendBroadcast(intent)
    }

    /** Open a document **/
    private fun sendOpenDocumentWith(filename: String?, mimeType: String?) {
        val openDocIntent = Intent()
        openDocIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        openDocIntent.action = Intent.ACTION_VIEW
        openDocIntent.putExtra(Key.NAME.toString(), filename)
        Log.d(TAGDEBUG, "Document name is: $filename")
        openDocIntent.putExtra(Key.MIME_TYPE.toString(), mimeType)
        Log.d(TAGDEBUG, "Document MimeType is: $mimeType")
        openDocIntent.setDataAndType(
                Uri.fromFile(File(StoreTask.getStorageDirectory(), filename)), mimeType)
        if (openDocIntent.resolveActivity(applicationContext.packageManager) != null) {
            applicationContext.startActivity(openDocIntent)
        } else {
            Toast.makeText(applicationContext,
                    "Unable to find viewer to open specified Activity", Toast.LENGTH_LONG).show()
        }
    }


    /**
     * Set a BroadcastReceiver for the application
     */
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = Action.fromString(intent?.action)
            Log.d(TAGDEBUG, "Received an intent: " + intent + " with Action: " + action)
            when (action) {
                Action.DATA_ARRIVED -> {
                    val uri = intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    val mimeType = intent?.getStringExtra(Key.MIME_TYPE.toString())
                    val fileName = intent?.getStringExtra(Key.MESSAGE_ID.toString())
                    var messageId = intent?.getStringExtra(Key.MESSAGE_ID.toString())
                    Log.d(TAGDEBUG, "Received URI: " + uri + " with mimeType: " + mimeType)
                    if (MIMEUtils.isImage(mimeType)) {
                        //if the data is for a different image return
                        if ((uri?.path?.equals(uriImage?.path)) ?: false) return
                        uriImage = uri
                        val isgetData = intent?.getBooleanExtra(Key.IS_A_GET_DATA.toString(), false)
                        if (isgetData ?: true) {
                            Log.d(TAGDEBUG, "Received return from GET_DATA, settting the image")
                            zoomView.setImageURI(uriImage)
                        } else {
                            Log.d(TAGDEBUG, "Received callback DATA_ARRIVED, sending GET_DATA request")
                            updateChunkCount(intent)
                            mDiscoveredChunks.put(fileName, intent)
                            sendGetData(fileName, messageId, mimeType)
                            return
                        }
                    } else if (MIMEUtils.isPresentation(mimeType)) {
                        val isgetData = intent?.getBooleanExtra(Key.IS_A_GET_DATA.toString(), false)
                        if (isgetData ?: true) {
                            Log.d(TAGDEBUG, "Received actual return from GET_DATA, presentation data")
                        } else {
                            Log.d(TAGDEBUG, "Received callback DATA_ARRIVED, sending GET_DATA request");
                            updateChunkCount(intent)
                            mDiscoveredChunks.put(fileName, intent)
                            sendGetData(fileName, messageId, mimeType)
                        }
                        getmoreButton.setOnClickListener(
                                { _ ->
                                    Log.d(TAGDEBUG, "Trying to open the document $fileName mimeType $mimeType")
                                    sendOpenDocumentWith(fileName, mimeType)
                                }
                        )
                    }

                }
                else -> {
                    Log.d(TAGDEBUG, "Unrecognized action " + action)
                }
            }


        }
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAGDEBUG, "Called onResume()")
        val dataViewerFilter = IntentFilter()
        registerReceiver(broadcastReceiver, dataViewerFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAGDEBUG, "Called function onDestroy")
        unregisterReceiver(broadcastReceiver)
    }
}
