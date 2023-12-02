package org.tensorflow.lite.examples.digitclassifier

import android.graphics.Bitmap
import java.nio.ByteBuffer

//data class ExecutionResult (
//    val bitmapResize:Bitmap,
////    val outputBufferString:String,
//    val outputBuffer:ByteBuffer,
////    val maskOnlyBitmap:Bitmap,
////    val inputBufferString:String
//)

data class Result(
    val float: Float,
    val string: String
)

data class ExecutionResult (
    val bitmapResize:Bitmap,
//    val outputBufferString:String,
    val outputBuffer:ByteBuffer,
    val multiplePredictResult:MutableList<Any>
//    val multiplePredictResult:MutableList<Double>
//    val maskOnlyBitmap:Bitmap,
//    val inputBufferString:String
)

data class ExecutionResultClassify (
    val bitmapResize:Bitmap,
//    val outputBufferString:String,
    val outputBuffer:ByteBuffer,
//    val maskOnlyBitmap:Bitmap,
//    val inputBufferString:String
)


