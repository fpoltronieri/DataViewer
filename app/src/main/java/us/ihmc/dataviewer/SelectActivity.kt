package us.ihmc.dataviewer

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.theartofdev.edmodo.cropper.CropImageView


class SelectActivity : AppCompatActivity() {

    val TAGDEBUG: String = "DEBUG"
    lateinit var cropView : CropImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        //get the intent and the uri
        val uriImage = intent.extras.get("uri") as Uri?
        Log.d(TAGDEBUG, "Uri retrieved " + uriImage)
        cropView = findViewById(R.id.cropImageView) as CropImageView
        cropView.setImageUriAsync(uriImage)

        //define the behaviour of the select button
        val select_button  = findViewById(R.id.select_area_button) as Button
        select_button.setOnClickListener(
                {
                    _ -> getCurrentSelection()
                }
        )

    }


    /**
     * get the Current selection of the Crop
     */
    private fun getCurrentSelection()  {
        val points = cropView.cropPoints
        Log.d(TAGDEBUG, "Selected points: " )
        var pointString = ""
        for (p in points) {
            Log.d(TAGDEBUG, "" + p)
            pointString = pointString + p + " "
        }
        Toast.makeText(this, "Points: " + pointString, Toast.LENGTH_SHORT).show()
    }

}
