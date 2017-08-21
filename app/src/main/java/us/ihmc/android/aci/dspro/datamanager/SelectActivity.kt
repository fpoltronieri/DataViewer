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
        Toast.makeText(this, "Points: " + pointString, Toast.LENGTH_SHORT).show()
        //TODO simplify this orrible casting
        val x0 = points[0].toDouble()
        val y0 = points[1].toDouble()
        val x1 = points[2].toDouble()
        val y1 = points[3].toDouble()
        val x3 = points[4].toDouble()
        val y3 = points[5].toDouble()
        val x2 = points[6].toDouble()
        val y2 = points[7].toDouble()
        val startX : Int = x0.toInt()
        var startY : Int = y2.toInt()
        val endX = Math.sqrt( Math.pow(x1 - x0, 2.0) +  Math.pow(y1 - y0, 2.0)).toInt()
        val endY = Math.sqrt( Math.pow(x3 - x2, 2.0) +  Math.pow(y3 - y2, 2.0)).toInt()
        Log.d(TAGDEBUG, "sx $startX ex $endX sy $startY $endY")
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
        onBackPressed()
    }

}
