package us.ihmc.android.aci.dspro.datamanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.theartofdev.edmodo.cropper.CropImageView
import us.ihmc.android.aci.dspro.datamanager.util.Action
import us.ihmc.android.aci.dspro.datamanager.util.Key


class SelectActivity : AppCompatActivity() {

    val TAGDEBUG: String = "DEBUG"
    lateinit var cropView : CropImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        //get the intent and the uri
        val uriImage = intent.extras.get("uri") as Uri?
        val messageId = intent.extras.get("messageid") as String?
        val mimeType = intent.extras.get("mimetype") as String?
        Log.d(TAGDEBUG, "Uri retrieved " + uriImage)
        cropView = findViewById(R.id.cropImageView) as CropImageView
        cropView.setImageUriAsync(uriImage)
        //define the behaviour of the select button
        val select_button  = findViewById(R.id.select_area_button) as Button
        select_button.setOnClickListener(
                {
                    _ -> getCurrentSelection(messageId, mimeType)
                }
        )

    }

    /**
     * get the Current selection of the Crop
     */
    private fun getCurrentSelection(messageId : String?, mimeType: String?)  {
        val points = cropView.cropPoints
        Log.d(TAGDEBUG, "Selected points: " )
        var pointString = ""
        for (p in points) {
            Log.d(TAGDEBUG, "" + p)
            pointString = pointString + p + " "
        }
        Log.d(TAGDEBUG, "Points: $pointString")
        //Toast.makeText(this, "Points: " + pointString, Toast.LENGTH_SHORT).show()
        val x0 = points[0].toDouble()
        val y0 = points[1].toDouble()
        val x1 = points[2].toDouble()
        val y3 = points[7].toDouble()
        var startX : Int = x0.toInt()
        var endX : Int = x1.toInt()
        //The points are inverted so
        var startY : Int = Math.min(cropView.height - y0.toInt(), cropView.height - y3.toInt())
        var endY : Int =  Math.max(cropView.height - y0.toInt(), cropView.height - y3.toInt())
        if (startY < 0)
            startY = 0
        Log.d(TAGDEBUG, "Cordinates: sx $startX ex $endX sy $startY ey $endY")
        val bundle = Bundle()
        bundle.putInt(Key.START_X.toString(), startX)
        bundle.putInt(Key.END_X.toString(), endX)
        bundle.putInt(Key.START_Y.toString(), startY)
        bundle.putInt(Key.END_Y.toString(), endY)
        bundle.putString(Key.MESSAGE_ID.toString(), messageId)
        bundle.putString(Key.MIME_TYPE.toString(), mimeType)
        val intent = Intent()
        intent.action = Action.REQUEST_CUSTOM_CHUNK.toString()
        intent.putExtras(bundle)
        applicationContext.sendBroadcast(intent)
        Log.d(TAGDEBUG, "Sent intent with action ${intent.action}")
        val loadingIntent = Intent()
        loadingIntent.action = Action.REQUESTED_CUSTOM_CHUNK.toString()
        Log.d(TAGDEBUG, "Sent intent with action ${loadingIntent.action}")
        applicationContext.sendBroadcast(loadingIntent)
        onBackPressed()
    }

}
