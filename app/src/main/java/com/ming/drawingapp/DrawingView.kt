package com.ming.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.util.jar.Attributes

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var drawPath: CustomPath? = null
    private var canvasBitmap: Bitmap? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    private var brushSize: Float = 0f
    private var color: Int = Color.BLACK
    private var canvas: Canvas? = null
    private val pathList = ArrayList<CustomPath>()
    private val undoPathList = ArrayList<CustomPath>()

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        drawPaint = Paint()
        brushSize = 20.0F
        drawPath = CustomPath(color, brushSize)
        drawPaint!!.color = color
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG) // Paint flag that enables dithering when blitting.


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        for (path in pathList) {
            drawPaint!!.strokeWidth = path.brushSize
            drawPaint!!.color = path.color
            canvas.drawPath(path, drawPaint!!)

        }

        if (!drawPath!!.isEmpty) {

            drawPaint!!.strokeWidth = drawPath!!.brushSize
            drawPaint!!.color = drawPath!!.color
            canvas.drawPath(drawPath!!, drawPaint!!)

        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath!!.brushSize = brushSize
                drawPath!!.color = color
                drawPath!!.reset()
                drawPath!!.moveTo(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath!!.lineTo(touchX, touchY)

            }
            MotionEvent.ACTION_UP -> {
                pathList.add(drawPath!!)
                drawPath = CustomPath(color, brushSize)

            }
            else -> return false

        }
        invalidate()
        return true

    }

    fun setBrushSize(size: Float) {
        brushSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, resources.displayMetrics)
        drawPaint!!.strokeWidth = brushSize

    }

    fun setBrushColor(color: Int) {
        this.color = color
        drawPaint!!.color = color

    }

    fun undoDrawPath() {

        if (pathList.size > 0) {
            undoPathList.add(pathList.removeAt(pathList.size - 1))
            invalidate()
        }
    }


    internal inner class CustomPath(var color: Int, var brushSize: Float) : Path() {

    }
}


