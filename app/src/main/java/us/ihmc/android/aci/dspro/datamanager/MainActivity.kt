package us.ihmc.android.aci.dspro.datamanager

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.github.chrisbanes.photoview.PhotoView
import us.ihmc.android.aci.dspro.datamanager.util.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    //constant values
    val URIIMAGE = "URIIMAGE"
    val WESPermission = 0x11
    val TAGDEBUG: String = "DEBUG"
    val TAKE_PICTURE = 2
    val JPG_SELECT_CODE = 0
    val DPR_SELECT_CODE = 1

    lateinit var gestureDetector: GestureDetectorCompat
    lateinit var zoomView: PhotoView
    lateinit var getmoreButton: Button
    lateinit var appBundle: Bundle
    lateinit var metadataView: TextView

    var mUriImage: Uri? = null
    var mCamUriImage: Uri? = null
    var mMessageId: String? = null
    var metaDataVisibile: Boolean = false
    var mMimeType: String? = null

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
        super.onCreate(savedInstanceState)
        appBundle = Bundle()
        setContentView(R.layout.activity_main)
        verifyPermission(this)
        //set the build timestamp
        this.supportActionBar?.subtitle = "Build " + SimpleDateFormat.getInstance().format(Date(BuildConfig.TIMESTAMP))
        //set the gestureDetectore for the application
        gestureDetector = GestureDetectorCompat(this, this)
        gestureDetector.setOnDoubleTapListener(this)
        //metadataView
        metadataView = findViewById(R.id.metadataView) as TextView
        //set the zoomable view
        zoomView = findViewById(R.id.zoomView) as PhotoView
        //override the onDoubleTapListener for the view, onDoubleTap browse the filesystem
        zoomView.setOnDoubleTapListener(this)
        //get more button
        getmoreButton = findViewById(R.id.button_getmore) as Button
        getmoreButton.setOnClickListener({ _ ->
            requestMoreChunks()
        })
        //select button
        val selectButton = findViewById(R.id.button_select) as Button
        selectButton.setOnClickListener({ _ ->
            selectChunks()
        })

        //the application has received an intent to take a picture
        val action = intent.action.toString()
        val type = intent.type
        val filename = intent.getStringExtra(Key.NAME.toString())
        mMessageId = intent.getStringExtra(Key.MESSAGE_ID.toString())
        mMimeType = intent.getStringExtra(Key.MIME_TYPE.toString())
        handleIntentOnActivity(action, filename, type)
    }

    /**
     * Preserve the mUriImage after configuration changes
     */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE
                || newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT)
            onSaveInstanceState(appBundle)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(URIIMAGE, mUriImage)
        Log.d(TAGDEBUG, "Called onSaveInstanceState, value for mUriImage = " + mUriImage)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAGDEBUG, "Called onRestoreInstanceState")
        mUriImage = savedInstanceState?.getParcelable(URIIMAGE)
        setImage(mUriImage)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        Log.d(TAGDEBUG, "Calling the superclass implementation")
        return super.onTouchEvent(event)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.d(TAGDEBUG, "onDoubleTap Updating the image")
        setImage(mUriImage)
        return true
    }


    /**
     * Switch the View between METADATA and IMAGE
     * The boolean logic will be replaced
     */
    private fun switchView() {
        if (metaDataVisibile) {
            metaDataVisibile = false
            metadataView.visibility = View.INVISIBLE
            //also set the mUriImage
            setImage(mUriImage)
        } else {
            metaDataVisibile = true
            metadataView.visibility = View.VISIBLE
            zoomView.visibility = View.INVISIBLE
        }

    }

    /**
     * Handle the selection of a picture
     * display the image in the Activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAGDEBUG, "onActivityResult called!")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_PICTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    sendAddMessege(mCamUriImage?.path, "image/jpg")
                    Log.d(TAGDEBUG, "ActivityResult OK, send report to DSPro:  $mCamUriImage?.path")
                    onBackPressed()
                }
            }
            JPG_SELECT_CODE ->
                if (resultCode == RESULT_OK) {
                    //send disseminate method to implement
                    onBackPressed()
                }
            DPR_SELECT_CODE -> {
                if (resultCode == RESULT_OK) {
                    val path = getFilePath(data)
                    /*val pathParts = wrongPath.split(":")
                    if (pathParts.size != 2) {
                        Log.e(TAGDEBUG, "Unable to split wrong path: " + wrongPath)
                        return;
                    }

                    val pathSuffix = pathParts[1];
                    val adjustedPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + pathSuffix
                    Log.d(TAGDEBUG, "Submitting adjusted path: " + adjustedPath)
                    sendRegisterPath(adjustedPath)*/
                    sendRegisterPath(path)
                    //go back to previous activity
                    onBackPressed()
                }
            }
        }
    }


    override fun onLongPress(e: MotionEvent?) {
        Log.d(TAGDEBUG, "onLongPress Recognized")
        switchView()
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.d(TAGDEBUG, "onSingle tap recognized " + e.toString())
        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(TAGDEBUG, "action currently not implemented " + e.toString())
        return false
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        Log.d(TAGDEBUG, "Swipe do not recognized $e1 $e2 $velocityX $velocityY")
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        Log.d(TAGDEBUG, "action currently not implemented " + e1.toString())
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        Log.d(TAGDEBUG, "action currently not implemented " + e.toString())
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.d(TAGDEBUG, "onSingleTap confirmed, calling switchToMetadata() " + e.toString())
        switchView()
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
        Log.d(TAGDEBUG, "action currently not implemented " + e.toString())
    }


    /**
     * Show file manager to upload file or route
     */
    private fun showFileChooser(inputCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val msg: String
        when (inputCode) {
            JPG_SELECT_CODE -> {
                intent.type = "image/jpg"
                msg = "Select a .jpg file to upload"
            }
            DPR_SELECT_CODE -> {
                intent.type = "*/*"
                msg = "Select a .dpr file to upload"
            }
            else -> {
                Log.d(TAGDEBUG, "Invalid input code $inputCode returning")
                return
            }
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(
                    Intent.createChooser(intent, msg), inputCode)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateChunkCount(intent: Intent?) {
        Log.d(TAGDEBUG, "updateChunkCount called()")
        val hasMoreChunks = intent?.getBooleanExtra(Key.HAS_MORE_CHUNKS.toString(), false)
        val chunkCount = intent?.getStringExtra(Key.CHUNK_COUNT.toString())
        Log.d(TAGDEBUG, "hasMoreChunks: $hasMoreChunks chunckCount: $chunkCount")
        if (hasMoreChunks == false) {
            Log.d(TAGDEBUG, "The image has not more chunks")
            getmoreButton.isEnabled = false
        } else {
            getmoreButton.isEnabled = true
        }
        if (!chunkCount.equals("")) {
            getmoreButton.text = getString(R.string.action_get_more_chunks) + " " + chunkCount
        }
    }


    /**
     * Request a CUSTOM chunk
     */
    private fun selectChunks() {
        if (mUriImage == null) {
            Toast.makeText(this, "uriImage not set", Toast.LENGTH_SHORT)
            return
        }
        if (mMessageId == null) {
            Toast.makeText(this, "messageID not set", Toast.LENGTH_SHORT)
        }
        val intent = Intent(this, SelectActivity::class.java)
        intent.putExtra("uri", mUriImage)
        intent.putExtra("messageid", mMessageId)
        intent.putExtra("mimetype", mMimeType)
        startActivity(intent)
    }

    /**
     * getMore onClickListener
     */
    private fun requestMoreChunks() {
        if (mMessageId == null) {
            Toast.makeText(this, "Unable to request, no MessageID found", Toast.LENGTH_LONG).show()
            Log.d(TAGDEBUG, "requestMoreChunks() Unable to request, no MessageID found")
            return
        }
        val intent = Intent()
        intent.action = Action.REQUEST_MORE_CHUNKS.toString()
        val bundle = Bundle()
        bundle.putString(Key.MESSAGE_ID.toString(), mMessageId)
        intent.putExtras(bundle)
        applicationContext.sendBroadcast(intent)
        Log.d(TAGDEBUG, "Sent " + Action.REQUEST_MORE_CHUNKS.toString() + " for mMessageId: " + mMessageId)
        setLoadingStatus()
    }

    /**
     * Open the camera app to take a picture
     */
    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val filename = SimpleDateFormat("yyyyMMddHHmm", Locale.US).format(Date())
        val photo = File(StoreTask.getStorageDirectory(), "$filename.jpg")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo))
        mCamUriImage = Uri.fromFile(photo)
        startActivityForResult(intent, TAKE_PICTURE)
    }

    /**
     * ADD a Message using DSPro
     */
    private fun sendAddMessege(filename: String?, mimeType: String?) {
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
        intent.action = Action.GET_DATA.toString()
        val bundle = Bundle()
        bundle.putString(Key.MESSAGE_ID.toString(), messageId)
        bundle.putString(Key.NAME.toString(), filename)
        bundle.putString(Key.MIME_TYPE.toString(), mimeType)
        intent.putExtras(bundle)
        Log.d(TAGDEBUG, "Sending  ${intent.action} in broadcast with mMessageId: $messageId  filename: $filename")
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
     * probably it is never used
     */
    private fun sendRegisterPath(filePath: String) {
        //prepare bundle
        val intent = Intent()
        intent.action = Action.REGISTER_PATH.toString()
        val extra = Bundle()
        extra.putString(Key.NAME.toString(), filePath)
        intent.putExtras(extra)
        Log.d(TAGDEBUG, "Sending " + Action.REGISTER_PATH.toString()
                + " in broadcast with filename: " + filePath)
        applicationContext.sendBroadcast(intent)
    }

    /**
     * probably it is never used
     */
    private fun getFilePath(data: Intent?): String {
        // Get the Uri of the selected file
        val uri = data?.data
        Log.d(TAGDEBUG, "File Uri: " + uri!!.toString())
        val filePath = uri.path
        Log.d(TAGDEBUG, "File Path: " + filePath)
        return filePath
    }


    /**
     * Set a BroadcastReceiver for the application
     */
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = Action.Companion.fromString(intent?.action.toString())
            Log.d(TAGDEBUG, "Received an intent: ${intent} with Action: $action while intent.action = ${intent?.action} ")
            when (action) {
                Action.DATA_ARRIVED -> {
                    val uri = intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    val mimeType = intent?.getStringExtra(Key.MIME_TYPE.toString())
                    val fileName = intent?.getStringExtra(Key.NAME.toString())
                    val messageId = intent?.getStringExtra(Key.MESSAGE_ID.toString())
                    val isImage = MIMEUtils.isImage(mimeType)
                    Log.d(TAGDEBUG, "Received URI: $uri with mimeType: $mimeType filename $fileName")
                    if (isImage && (uri?.path?.equals(mUriImage?.path) == false)) {
                        Log.d(TAGDEBUG, "Data for different pic " + uri?.path + " " + mUriImage?.path)
                        return
                    }
                    if (isImage) mUriImage = uri
                    val isgetData = intent?.getBooleanExtra(Key.IS_A_GET_DATA.toString(), false)
                    Log.d(TAGDEBUG, "isgetData: " + isgetData)
                    if (isgetData == true) {
                        Log.d(TAGDEBUG, "Received return from GET_DATA")
                        if (isImage) {
                            //try to sleep before loading the Image
                            //the plugin is still finalizing the Image?
                            Handler().postDelayed(
                                    {
                                        setImage(mUriImage)
                                    }
                            , 200)
                        }
                    } else {
                        Log.d(TAGDEBUG, "Received callback DATA_ARRIVED, requesting DATA")
                        mDiscoveredChunks.put(fileName, intent)
                        updateChunkCount(intent)
                        sendGetData(fileName, messageId, mimeType)
                        //TODO remove the if (isImage) code
                        if (MIMEUtils.isPresentation(mimeType)) {
                            getmoreButton.setOnClickListener(
                                    { _ ->
                                        Log.d(TAGDEBUG, "Trying to open the document $fileName mimeType $mimeType")
                                        sendOpenDocumentWith(fileName, mimeType)
                                    }
                            )
                        }
                        return
                    }
                }
                Action.REQUESTED_CUSTOM_CHUNK -> {
                    //visualize the loading bar
                    setImage(mUriImage)
                    setLoadingStatus()
                }
                else -> {
                    Log.d(TAGDEBUG, "Unrecognized action " + action)
                }
            }
        }
    }

    private fun setLoadingStatus() {
        findViewById(R.id.loadingBar).visibility = View.VISIBLE
        zoomView.alpha = 0.5F
    }

    private fun setImage(uri: Uri?) {
        findViewById(R.id.loadingBar).visibility = View.INVISIBLE
        try {
            zoomView.alpha = 1.0F
            zoomView.invalidate()
            zoomView.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
            //the only way to prevent the Exception is to open the file here
            //and catch the Exception in this method
            //null check, a NPE is thrown elsewhere
            if (mUriImage?.path != null) {
                val image = FileInputStream(mUriImage?.path)
                image.close()
            }
            zoomView.visibility = View.VISIBLE
            metadataView.visibility = View.INVISIBLE
            zoomView.setImageURI(uri)
        } catch (e: FileNotFoundException) {
            Log.d(TAGDEBUG, "Catched Exception ${e.message} in setImage()")
            metaDataVisibile = false
            switchView()
        }
    }

    private fun handleIntentOnActivity(action: String?, filename: String?, type: String?) {
        when (action) {
            Intent.ACTION_SEND -> {
                if (type == null) return
                //load metadata
                val metadataUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_TEXT)
                try {
                    val strMetadata = Util.readFile(metadataUri.path)
                    //set the metadataView
                    metadataView.text = strMetadata
                } catch (e: IOException) {
                    Log.d(TAGDEBUG, "Unable to read file $metadataUri")
                }
                Log.d(TAGDEBUG, "Received type: $type")
                if (MIMEUtils.isImage(type)) {
                    mUriImage = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    Log.d(TAGDEBUG, "Received uri: " + mUriImage)
                    setImage(mUriImage)
                } else if (MIMEUtils.isPresentation(type)) {
                    mUriImage = Util.getUriToDrawable(applicationContext, R.drawable.powerpoint)
                    zoomView.setImageURI(mUriImage)
                    //Open Document
                    getmoreButton.setOnClickListener(
                            { _ ->
                                Log.d(TAGDEBUG, "Opening document $filename")
                                sendOpenDocumentWith(filename, mMimeType)
                            }
                    )
                } else {
                    //load the JSON metadata
                    val jsonUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_SUBJECT)
                    try {
                        val formattedJSON = Util.formatJSON(Util.readFile(jsonUri.path))
                        metadataView.text = formattedJSON
                        metaDataVisibile = true
                        metadataView.visibility = View.VISIBLE
                        //set the metadata
                    } catch (e: IOException) {
                        Log.d(TAGDEBUG, "Unable to read file " + metadataUri.path)
                    }
                }
                //update the chunks
                if (mDiscoveredChunks[filename] != null) {
                    Log.d(TAGDEBUG, "Previously discovered CHUNK, updating chunk count")
                    val intentChunks = mDiscoveredChunks[filename]
                    updateChunkCount(intentChunks)
                } else {
                    Log.d(TAGDEBUG, "Discovered first chunk for $mUriImage")
                }
            }
        //take a photo using the camera
            Action.ADD_MESSAGE.toString() -> {
                Log.d(TAGDEBUG, "Received intent with action: $action")
                takePhoto()
            }
        //disseminate file, never tested
            Action.DISSEMINATE.toString() -> {
                Log.d(TAGDEBUG, "Received intent with $action, opening the file manager")
                showFileChooser(JPG_SELECT_CODE)
            }
        //upload a dpr file
            Action.LOAD_ROUTE.toString() -> {
                Log.d(TAGDEBUG, "Received intent with $action, loading a route")
                showFileChooser(DPR_SELECT_CODE)
            }
        //set loadingStatus after SELECT
            Action.REQUESTED_CUSTOM_CHUNK.toString() -> {
                setLoadingStatus()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAGDEBUG, "Called onResume() registering the broadcastReceiver")
        val dataViewerFilter = IntentFilter()
        dataViewerFilter.addAction(Action.DATA_ARRIVED.toString())
        dataViewerFilter.addAction(Action.REQUESTED_CUSTOM_CHUNK.toString())
        registerReceiver(broadcastReceiver, dataViewerFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAGDEBUG, "Called function onDestroy()")
        unregisterReceiver(broadcastReceiver)
    }

}
