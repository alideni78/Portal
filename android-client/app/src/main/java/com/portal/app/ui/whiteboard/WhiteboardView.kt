package com.portal.app.ui.whiteboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.portal.app.R
import com.portal.app.model.DrawPoint
import com.portal.app.model.Stroke
import kotlinx.coroutines.*

/**
 * Custom view for collaborative whiteboard
 * Handles drawing, rendering, and auto-cleanup of strokes
 */
class WhiteboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TAG = "WhiteboardView"
    
    // Drawing properties
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 4f
    }
    
    private val backgroundPaint = Paint().apply {
        color = context.getColor(R.color.whiteboard_bg)
    }
    
    // Stroke storage
    private val strokes = mutableListOf<Stroke>()
    private val currentPath = Path()
    private val currentPoints = mutableListOf<DrawPoint>()
    
    // Callbacks
    var onStrokeDrawn: ((List<DrawPoint>) -> Unit)? = null
    
    // Cleanup coroutine
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var cleanupJob: Job? = null
    
    // Constants
    private val STROKE_LIFETIME_MS = 30_000L // 30 seconds
    private val CLEANUP_INTERVAL_MS = 1_000L // 1 second
    
    init {
        startCleanupTimer()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Draw all stored strokes
        synchronized(strokes) {
            strokes.forEach { stroke ->
                drawStroke(canvas, stroke)
            }
        }
        
        // Draw current stroke being drawn
        if (currentPoints.isNotEmpty()) {
            paint.color = Color.BLACK
            paint.strokeWidth = 4f
            canvas.drawPath(currentPath, paint)
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.reset()
                currentPath.moveTo(x, y)
                currentPoints.clear()
                currentPoints.add(normalizePoint(x, y))
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
                currentPoints.add(normalizePoint(x, y))
                invalidate()
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                currentPath.lineTo(x, y)
                currentPoints.add(normalizePoint(x, y))
                
                // Notify listener about completed stroke
                if (currentPoints.size > 1) {
                    onStrokeDrawn?.invoke(currentPoints.toList())
                    
                    // Add to local strokes
                    addStroke(
                        Stroke(
                            points = currentPoints.toList(),
                            color = "#000000",
                            strokeWidth = 4f,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                
                currentPath.reset()
                currentPoints.clear()
                invalidate()
                return true
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    /**
     * Add a stroke from network or local drawing
     */
    fun addStroke(stroke: Stroke) {
        synchronized(strokes) {
            strokes.add(stroke)
        }
        invalidate()
    }
    
    /**
     * Clear all strokes
     */
    fun clear() {
        synchronized(strokes) {
            strokes.clear()
        }
        currentPath.reset()
        currentPoints.clear()
        invalidate()
    }
    
    /**
     * Normalize point coordinates to 0.0-1.0 range
     */
    private fun normalizePoint(x: Float, y: Float): DrawPoint {
        val normalizedX = if (width > 0) x / width.toFloat() else 0f
        val normalizedY = if (height > 0) y / height.toFloat() else 0f
        return DrawPoint(
            x = normalizedX.coerceIn(0f, 1f),
            y = normalizedY.coerceIn(0f, 1f)
        )
    }
    
    /**
     * Denormalize point coordinates from 0.0-1.0 to actual pixels
     */
    private fun denormalizePoint(point: DrawPoint): PointF {
        return PointF(
            point.x * width.toFloat(),
            point.y * height.toFloat()
        )
    }
    
    /**
     * Draw a single stroke on canvas
     */
    private fun drawStroke(canvas: Canvas, stroke: Stroke) {
        if (stroke.points.size < 2) return
        
        val path = Path()
        val firstPoint = denormalizePoint(stroke.points[0])
        path.moveTo(firstPoint.x, firstPoint.y)
        
        for (i in 1 until stroke.points.size) {
            val point = denormalizePoint(stroke.points[i])
            path.lineTo(point.x, point.y)
        }
        
        paint.color = parseColor(stroke.color)
        paint.strokeWidth = stroke.strokeWidth
        canvas.drawPath(path, paint)
    }
    
    /**
     * Parse color string to Color int
     */
    private fun parseColor(colorString: String): Int {
        return try {
            Color.parseColor(colorString)
        } catch (e: Exception) {
            Color.BLACK
        }
    }
    
    /**
     * Start automatic cleanup timer
     */
    private fun startCleanupTimer() {
        cleanupJob = scope.launch {
            while (isActive) {
                delay(CLEANUP_INTERVAL_MS)
                cleanupOldStrokes()
            }
        }
    }
    
    /**
     * Remove strokes older than STROKE_LIFETIME_MS
     */
    private fun cleanupOldStrokes() {
        val currentTime = System.currentTimeMillis()
        var removed = false
        
        synchronized(strokes) {
            val iterator = strokes.iterator()
            while (iterator.hasNext()) {
                val stroke = iterator.next()
                if (currentTime - stroke.timestamp > STROKE_LIFETIME_MS) {
                    iterator.remove()
                    removed = true
                }
            }
        }
        
        if (removed) {
            invalidate()
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cleanupJob?.cancel()
        scope.cancel()
    }
}