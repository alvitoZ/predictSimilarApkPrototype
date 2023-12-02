package org.tensorflow.lite.examples.digitclassifier

import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import android.util.Log
import androidx.core.graphics.ColorUtils
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
import org.tensorflow.lite.support.image.ops.Rot90Op
import  org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.nio.FloatBuffer
import java.nio.charset.StandardCharsets
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random


class DigitClassifier2(val context: Context) {
  private var interpreter: Interpreter? = null
  private lateinit var imageDataType:DataType;

  var isInitialized = false
    private set

  /** Executor to run inference task in the background */
  private val executorService: ExecutorService = Executors.newCachedThreadPool()

  fun initialize(): Task<Void> {
    val task = TaskCompletionSource<Void>()
    executorService.execute {
      try {
        initializeInterpreter()
        task.setResult(null)
      } catch (e: IOException) {
        task.setException(e)
      }
    }
    return task.task
  }

  @Throws(IOException::class)
  private fun initializeInterpreter() {
    // Load the TF Lite model
    val assetManager = context.assets
    val model = loadModelFile(assetManager)

    // Initialize TF Lite Interpreter with NNAPI enabled
    val options = Interpreter.Options()
    options.setUseNNAPI(true)
    val interpreter = Interpreter(model, options)

    // Finish interpreter initialization
    this.interpreter = interpreter
    isInitialized = true
    imageDataType = interpreter.getInputTensor(0).dataType();
    Log.d(TAG, "Initialized TFLite interpreter.")
  }

  @Throws(IOException::class)
  private fun loadModelFile(assetManager: AssetManager): ByteBuffer {
    val fileDescriptor = assetManager.openFd(MODEL_FILE)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
  }

  fun loadImageWithTf(bitmap: Bitmap):TensorImage{

    var image = TensorImage(imageDataType)
//    image.load(bitmap)

    var cropSize:Int = min(bitmap.width, bitmap.height)

    var imageProcessor = ImageProcessor.Builder().add(ResizeWithCropOrPadOp(cropSize, cropSize))
      .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeMethod.NEAREST_NEIGHBOR))
      .build()

    return imageProcessor.process(image)
  }



  private fun classify(bitmap: Bitmap): ExecutionResultClassify {
    if (!isInitialized) {
      throw IllegalStateException("TF Lite Interpreter is not initialized yet.")
    }

    var startTime: Long
    var elapsedTime: Long

    // Preprocessing: resize the input
    startTime = System.nanoTime()

//    val resizedImage = scaleBitmapAndKeepRatio(bitmap,224,224)

    val resizedImage = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
    val inputBuffer = convertBitmapToByteBuffer(resizedImage)
//    val inputBuffer = loadImageWithTf(resizedImage)

    val outputBuffer = ByteBuffer.allocateDirect(OUTPUT_SIZE * FLOAT_TYPE_SIZE)
    outputBuffer.order(ByteOrder.nativeOrder())
    outputBuffer.rewind()
    elapsedTime = (System.nanoTime() - startTime) / 1000000
    Log.d(TAG, "Preprocessing time = " + elapsedTime + "ms")

    startTime = System.nanoTime()

    interpreter?.run(inputBuffer, outputBuffer)

    elapsedTime = (System.nanoTime() - startTime) / 1000000
    Log.d(TAG, "Inference time = " + elapsedTime + "ms")


//    inputBuffer.rewind()
//    outputBuffer.rewind()


    return ExecutionResultClassify(
      resizedImage,
//      Coba(outputBuffer),
      outputBuffer
//      Coba(inputBuffer)
    )
  }



  fun ReturnBitmap(bitmap: Bitmap):Bitmap{

    return bitmap

  }

  private fun CosineSim(vector1:MutableList<Float>, vector2:MutableList<Float>):Float{
    var sum = 0.0f
    var suma1 = 0.0f
    var suma2 = 0.0f

    var zipped: List<Pair<Float, Float>> = vector1 zip vector2

    for ((i,j) in zipped){
      suma1 += i * j
      suma2 += j * j
      sum += i * j
    }
    var cosine_sim = sum / ((sqrt(suma1)) * (sqrt(suma2)))

    return cosine_sim

  }

  data class Result(
    val similarity: Float,
    val gambar: String
  )

  fun VectorToCosineSim():MutableList<Any>{
    var list = mutableListOf<Any>()

    for (i in getPredictImage()){
      list.add(Result(
        CosineSim(Coba(classify(loadImageFile(context.assets, "predict/original-image.jpg")).outputBuffer),Coba(classify(loadImageFile(context.assets, "predict/$i")).outputBuffer)),
        i
      ))
    }

//    list.add(CosineSim(Coba(classify(loadImageFile(context.assets, "predict/kitan2.jpg")).outputBuffer),Coba(classify(loadImageFile(context.assets, "predict/kitan2.jpg")).outputBuffer)))

    return list
  }


  fun Coba(byteBuffer: ByteBuffer):MutableList<Float>{
     var list = mutableListOf<Float>()

     byteBuffer.rewind()



//    1279
//    var int = byteBuffer.remaining() - 1
    var int = 1279

    var floatBuffer: FloatBuffer = byteBuffer.asFloatBuffer()


    var floatArray = FloatArray(floatBuffer.remaining())
    floatBuffer.get(floatArray)


    for (i in floatArray){
      list.add(i)
    }



//    for (i in 0..int){
//      list.add(byteBuffer.asFloatBuffer().get(i))
//    }

//    var bufer = byteBuffer.remaining()
//    var bufer = byteBuffer.asFloatBuffer()

//    return "list:${list.toList()} \n limit: ${byteBuffer.limit()} \n, capacity: ${byteBuffer.capacity()} \n posiiton: ${byteBuffer.position()} "

    return list

//    return "${CosineSim(list, list)}"
  }

  fun getPredictImage():MutableList<String>{
    var coba = context.assets
    var list = mutableListOf<String>()

    for (i in 0..14){
      coba.list("predict")?.get(i)?.let { list.add(it) }
    }
    return list
  }

//  fun Normalize(pixels: ByteArray):FloatArray{
//    return pixels.map { it / 255.0f }.toFloatArray()
//  }


//  fun classifyAsync(assetmanager: AssetManager,filePath: String): Task<String> {
//    val task = TaskCompletionSource<String>()
//    executorService.execute {
//      val result = classify(loadImageFile(assetmanager,filePath))
////      val result = classify(BitmapFactory.decodeFile(filePath))
//      task.setResult(result)
//    }
//    return task.task
//  }


  fun classifyAsync(assetmanager: AssetManager,filePath: String): Task<ExecutionResult> {
    val task = TaskCompletionSource<ExecutionResult>()
    executorService.execute {
      val result = classify(loadImageFile(assetmanager,filePath))
      val result2 = VectorToCosineSim()
//      val result = classify(BitmapFactory.decodeFile(filePath))
      task.setResult(ExecutionResult(
        result.bitmapResize,
        result.outputBuffer,
        result2
      ))
    }
    return task.task
  }

  fun close() {
    executorService.execute {
      interpreter?.close()
      Log.d(TAG, "Closed TFLite interpreter.")
    }
  }

  private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    val inputBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE * FLOAT_TYPE_SIZE)
    inputBuffer.order(ByteOrder.nativeOrder())
    inputBuffer.rewind()

    val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    for (pixelValue in pixels) {
      val r = (pixelValue shr 16 and 0xFF)
      val g = (pixelValue shr 8 and 0xFF)
      val b = (pixelValue and 0xFF)

      // Convert RGB to grayscale and normalize pixel value to [0..1]
      val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
      inputBuffer.putFloat(normalizedPixelValue)
    }

    return inputBuffer
  }

  fun scaleBitmapAndKeepRatio(
    targetBmp: Bitmap,
    reqHeightInPixels: Int,
    reqWidthInPixels: Int
  ): Bitmap {
    if (targetBmp.height == reqHeightInPixels && targetBmp.width == reqWidthInPixels) {
      return targetBmp
    }
    val matrix = Matrix()
    matrix.setRectToRect(
      RectF(
        0f, 0f,
        targetBmp.width.toFloat(),
        targetBmp.width.toFloat()
      ),
      RectF(
        0f, 0f,
        reqWidthInPixels.toFloat(),
        reqHeightInPixels.toFloat()
      ),
      Matrix.ScaleToFit.FILL
    )
    return Bitmap.createBitmap(
      targetBmp, 0, 0,
      targetBmp.width,
      targetBmp.width, matrix, true
    )
  }



  fun loadImageFile(assetmanager: AssetManager, image: String): Bitmap {
    val inputstream: InputStream
    try {
      inputstream = assetmanager.open(image)
    }catch (e: IOException) {
      throw IOException("gagal buka asset $image $e")
    }

    return BitmapFactory.decodeStream(inputstream)
  }

  private fun getOutputString(output: FloatArray): String {
    val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
    return "Prediction Result: %d\nConfidence: %2f".format(maxIndex, output[maxIndex])
  }

  //  private fun classify(bitmap: Bitmap): String {
//    if (!isInitialized) {
//      throw IllegalStateException("TF Lite Interpreter is not initialized yet.")
//    }
//
//    var startTime: Long
//    var elapsedTime: Long
//
//    // Preprocessing: resize the input
//    startTime = System.nanoTime()
//
//    val resizedImage = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
//    val inputBuffer = convertBitmapToByteBuffer(resizedImage)
////    val inputBuffer = loadImageWithTf(resizedImage)
//
//    val outputBuffer = ByteBuffer.allocateDirect(OUTPUT_SIZE * FLOAT_TYPE_SIZE)
//    outputBuffer.order(ByteOrder.nativeOrder())
//    outputBuffer.rewind()
//    elapsedTime = (System.nanoTime() - startTime) / 1000000
//    Log.d(TAG, "Preprocessing time = " + elapsedTime + "ms")
//
//    startTime = System.nanoTime()
//
//    interpreter?.run(inputBuffer, outputBuffer)
//    elapsedTime = (System.nanoTime() - startTime) / 1000000
//    Log.d(TAG, "Inference time = " + elapsedTime + "ms")
//
//    outputBuffer.rewind()
//    return Coba(outputBuffer)
//  }

  companion object {
    private const val TAG = "DigitClassifier2"

    private const val MODEL_FILE = "model4.tflite"

    private const val BATCH_SIZE = 1
    private const val INPUT_SIZE = 224
    private const val PIXEL_SIZE = 3
    private const val OUTPUT_SIZE = 1280
    private const val FLOAT_TYPE_SIZE = 4
  }
}



