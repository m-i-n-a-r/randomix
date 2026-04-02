package com.minar.randomix.utilities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.color.MaterialColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * A large, softly morphing blob shape drawn behind all app content.
 *
 * The blob continuously interpolates between randomly generated organic shapes
 * using smooth cubic Bézier curves (Catmull-Rom conversion). Its center slowly
 * drifts around a small area of the screen so it never feels static.
 *
 * Color: theme's colorPrimary at a very low alpha (~9 %) so it acts purely
 * as a subtle atmospheric accent, never interfering with readability.
 */
class BlobBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        /** Number of anchor points that define the blob outline. */
        private const val N = 8
        /** Duration of one morph cycle in milliseconds. */
        private const val MORPH_MS = 9_000L
        /** Blob fill alpha (0–255). 22 ≈ 8.6 %. */
        private const val BLOB_ALPHA = 22
        /**
         * Catmull-Rom tension factor.
         * Lower = rounder curves; higher = tighter curves.
         */
        private const val TENSION = 0.38f
        /** Blob radius as a fraction of min(width, height). */
        private const val RADIUS_FACTOR = 0.54f
        /** How much the center drifts (fraction of width / height). */
        private const val DRIFT = 0.12f
    }

    // Paint & path

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val blobPath = Path()

    // Shape state

    /** Per-anchor radius multipliers for the "from" shape (0.55 – 1.0). */
    private var fromRadii  = randomRadii()
    /** Per-anchor radius multipliers for the "to" (target) shape. */
    private var toRadii    = randomRadii()
    /** Currently interpolated radii, drawn each frame. */
    private var curRadii   = fromRadii.copyOf()

    // Center position expressed as fractions of width / height (0–1).
    private var fromCx = 0.50f;  private var fromCy = 0.48f
    private var toCx   = 0.50f;  private var toCy   = 0.52f
    private var curCx  = 0.50f;  private var curCy  = 0.50f

    // Animator

    private val morphAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration       = MORPH_MS
        interpolator   = AccelerateDecelerateInterpolator()
        repeatCount    = ValueAnimator.INFINITE
        repeatMode     = ValueAnimator.RESTART

        // At the start of every new cycle: current shape becomes the "from",
        // and a fresh random shape becomes the new "to".
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator) {
                fromRadii = curRadii.copyOf()
                fromCx    = toCx;   fromCy = toCy
                toRadii   = randomRadii()
                toCx      = 0.50f + (Math.random().toFloat() - 0.5f) * DRIFT * 2f
                toCy      = 0.50f + (Math.random().toFloat() - 0.5f) * DRIFT * 2f
            }
        })

        addUpdateListener { anim ->
            val t = anim.animatedFraction
            for (i in 0 until N) curRadii[i] = lerp(fromRadii[i], toRadii[i], t)
            curCx = lerp(fromCx, toCx, t)
            curCy = lerp(fromCy, toCy, t)
            invalidate()
        }
    }

    // Lifecycle

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        morphAnimator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        morphAnimator.cancel()
    }

    // Drawing

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val color = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimaryFixed,
            0
        )
        paint.color = color
        paint.alpha = BLOB_ALPHA

        val w          = width.toFloat()
        val h          = height.toFloat()
        val baseRadius = minOf(w, h) * RADIUS_FACTOR
        val cx         = w * curCx
        val cy         = h * curCy

        buildBlobPath(cx, cy, baseRadius)
        canvas.drawPath(blobPath, paint)
    }

    // ── Path construction ─────────────────────────────────────────────────────

    /**
     * Builds a smooth closed Bézier path through [N] anchor points arranged
     * on a polar grid. Radii come from [curRadii]; control handles use a
     * Catmull-Rom → cubic-Bézier conversion with [TENSION].
     */
    private fun buildBlobPath(cx: Float, cy: Float, baseRadius: Float) {
        blobPath.reset()

        // 1. Compute anchor positions.
        val px = FloatArray(N)
        val py = FloatArray(N)
        for (i in 0 until N) {
            val angle = 2.0 * Math.PI * i / N - Math.PI / 2.0
            val r     = baseRadius * curRadii[i]
            px[i]     = (cx + r * cos(angle)).toFloat()
            py[i]     = (cy + r * sin(angle)).toFloat()
        }

        // 2. Trace the path with cubic Bézier segments (Catmull-Rom handles).
        blobPath.moveTo(px[0], py[0])
        for (i in 0 until N) {
            val i0 = (i - 1 + N) % N
            val i2 = (i + 1)     % N
            val i3 = (i + 2)     % N

            // Control point leaving i1 toward i2
            val cp1x = px[i] + (px[i2] - px[i0]) * TENSION
            val cp1y = py[i] + (py[i2] - py[i0]) * TENSION
            // Control point arriving at i2 from i1
            val cp2x = px[i2] - (px[i3] - px[i]) * TENSION
            val cp2y = py[i2] - (py[i3] - py[i]) * TENSION

            blobPath.cubicTo(cp1x, cp1y, cp2x, cp2y, px[i2], py[i2])
        }
        blobPath.close()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns [N] random radius multipliers in [0.55, 1.0]. */
    private fun randomRadii() = FloatArray(N) { 0.55f + Math.random().toFloat() * 0.45f }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
}
