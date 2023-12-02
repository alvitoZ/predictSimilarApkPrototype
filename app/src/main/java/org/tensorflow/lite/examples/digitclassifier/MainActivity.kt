package org.tensorflow.lite.examples.digitclassifier

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.tfe_dc_activity_main.*

fun main() {
//  digitClassifier2
//    .initialize()
//    .addOnFailureListener { e -> Log.e("AKTIF", "Error to setting up digit classifier.", e) }
  println(2)
}

class MainActivity : AppCompatActivity() {

//  private var drawView: DrawView? = null
  private var imageView: ImageView? = null
  private var actionButton:Button? = null
  private var clearButton: Button? = null
  private var predictedTextView: TextView? = null
//  private var digitClassifier = DigitClassifier(this)
  private var digitClassifier2 = DigitClassifier2(this)

  @SuppressLint("ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tfe_dc_activity_main)
    // Setup view instances
//    drawView = findViewById(R.id.draw_view)
//    drawView?.setStrokeWidth(70.0f)
//    drawView?.setColor(Color.WHITE)
//    drawView?.setBackgroundColor(Color.BLACK)
    clearButton = findViewById(R.id.clear_button)
    actionButton = findViewById(R.id.action_button)
    imageView = findViewById(R.id.result_imageview)
    predictedTextView = findViewById(R.id.predicted_text)

    // Setup clear drawing button
    clearButton?.setOnClickListener {
//      loopenamribukali()
//      drawView?.clearCanvas()
      imageView?.setImageBitmap(createEmptyBitmap(224,224))
      predictedTextView?.text = getString(R.string.tfe_dc_prediction_text_placeholder)
    }

    // Setup classification trigger so that it classify after every stroke drew

    actionButton?.setOnClickListener {
      classifyDrawing()
    }


//    drawView?.setOnTouchListener { _, event ->
//      // As we have interrupted DrawView's touch event,
//      // we first need to pass touch events through to the instance for the drawing to show up
//      drawView?.onTouchEvent(event)
//
//      // Then if user finished a touch event, run classification
//      if (event.action == MotionEvent.ACTION_UP) {
//        classifyDrawing()
//      }
//
//      true
//    }

    // Setup digit classifier
//    digitClassifier
//      .initialize()
//      .addOnFailureListener { e -> Log.e(TAG, "Error to setting up digit classifier.", e) }

    digitClassifier2
      .initialize()
      .addOnFailureListener { e -> Log.e(TAG, "Error to setting up digit classifier.", e) }
  }

  override fun onDestroy() {
    digitClassifier2.close()
    super.onDestroy()
  }

//  override fun onDestroy() {
//    digitClassifier.close()
//    super.onDestroy()
//  }

  private fun classifyDrawing() {
      digitClassifier2
        .classifyAsync(this.assets,"original-image.jpg")
//        .addOnSuccessListener { resultText -> Log.d("KITAN", "$resultText") }
        .addOnSuccessListener { resultText ->
          imageView?.setImageBitmap(resultText.bitmapResize)
          predictedTextView?.text = "output: ${resultText.multiplePredictResult}"
          Consoled("${resultText.multiplePredictResult.toList()}")
//          Log.i(TAGOUTPUT, "output: ${resultText.outputBufferString}")
//          Log.i(TAGINPUT, "input: ${resultText.inputBufferString}")

        }
        .addOnFailureListener { e ->
          predictedTextView?.text = getString(
            R.string.tfe_dc_classification_error_message,
            e.localizedMessage
          )
          Log.e(TAG, "Error classifying drawing.", e)
    }
  }

  fun Consoled(str: String){
    Log.i("MIAW", "${str}")
  }

  fun loopenamribukali():Unit{
    for (i in 0..6000){
      Log.i("ENAM", "$i")

    }
  }

  fun createEmptyBitmap(imageWidth: Int, imageHeigth: Int, color: Int = 0): Bitmap {
    val ret = Bitmap.createBitmap(imageWidth, imageHeigth, Bitmap.Config.RGB_565)
    if (color != 0) {
      ret.eraseColor(color)
    }
    return ret
  }

//  private fun classifyDrawing() {
//    val bitmap = drawView?.getBitmap()
//
//    if ((bitmap != null) && (digitClassifier.isInitialized)) {
//      digitClassifier
//        .classifyAsync(bitmap)
////        .addOnSuccessListener { resultText -> predictedTextView?.text = resultText }
//        .addOnSuccessListener { resultText -> Log.i("KITAN", "$resultText") }
//        .addOnFailureListener { e ->
//          predictedTextView?.text = getString(
//            R.string.tfe_dc_classification_error_message,
//            e.localizedMessage
//          )
//          Log.e(TAG, "Error classifying drawing.", e)
//        }
//    }
//  }



  companion object {
    private const val TAG = "MainActivity"
    private const val TAGINPUT = "KITAN"
    private const val TAGOUTPUT = "IKUYO"
  }
}
