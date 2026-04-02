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

class BlobBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // How many blobs to render. Adding a BlobConfig entry for each is enough.
    private val blobCount = 2

    // Number of anchor points on the blob outline. More = smoother silhouette, heavier math.
    private val n = 10

    // Catmull-Rom tension: lower = rounder/fatter curves, higher = tighter/spikier.
    private val tension = 0.32f

    private data class BlobConfig(
        // Theme color attribute used for the blob fill (e.g. colorPrimaryFixed).
        val colorAttr: Int,
        // Opacity 0-255. Keep low (15-30) so the blob stays atmospheric and doesn't obscure UI.
        val alpha: Int,
        // Blob base radius as a fraction of min(screenWidth, screenHeight).
        // 0.5 = half the short side. Values above ~0.6 make the blob visually huge.
        val radiusFactor: Float,
        // Duration in ms of one full morph cycle (from→to shape).
        val morphMs: Long,
        // How far the blob center can wander from the screen midpoint, expressed as a fraction
        // of the screen dimensions. Values above 0.5 allow the CENTER to leave the screen
        // entirely, so the blob partially or fully bleeds off the edges — intentional.
        val drift: Float,
        // Starting center position as fractions of screen width (cx) and height (cy).
        // 0.0 = left/top edge, 1.0 = right/bottom edge. Can exceed that range to start off-screen.
        val initCx: Float,
        val initCy: Float
    )

    private val configs = listOf(
        BlobConfig(
            colorAttr    = com.google.android.material.R.attr.colorPrimaryFixed,
            alpha        = 20,
            radiusFactor = 0.52f,
            morphMs      = 6_500L,
            // Large drift: the center roams well past the edges, so the blob frequently
            // bleeds off two sides simultaneously.
            drift        = 0.65f,
            initCx       = 0.08f,
            initCy       = 0.05f
        ),
        BlobConfig(
            colorAttr    = com.google.android.material.R.attr.colorSecondary,
            alpha        = 25,
            radiusFactor = 0.43f,
            morphMs      = 7_800L,
            drift        = 0.60f,
            initCx       = 0.55f,
            initCy       = 0.82f
        )
    ).take(blobCount)

    private inner class BlobState(val cfg: BlobConfig) {
        var fromRadii = randomRadii()
        var toRadii   = randomRadii()
        var curRadii  = fromRadii.copyOf()
        var fromCx = cfg.initCx; var fromCy = cfg.initCy
        var toCx   = cfg.initCx; var toCy   = cfg.initCy
        var curCx  = cfg.initCx; var curCy  = cfg.initCy
    }

    private val states = configs.map { BlobState(it) }

    private val paints = configs.map {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    }

    private val paths = List(blobCount) { Path() }

    private val animators: List<ValueAnimator> = states.mapIndexed { index, state ->
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration     = state.cfg.morphMs
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount  = ValueAnimator.INFINITE
            repeatMode   = ValueAnimator.RESTART

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    state.fromRadii = state.curRadii.copyOf()
                    state.fromCx    = state.toCx
                    state.fromCy    = state.toCy
                    state.toRadii   = randomRadii()
                    // Target center: midpoint ± drift. Because drift can exceed 0.5, the target
                    // cx/cy can be negative or above 1.0, placing the center outside the screen.
                    val d = state.cfg.drift
                    state.toCx = 0.50f + (Math.random().toFloat() - 0.5f) * d * 2f
                    state.toCy = 0.50f + (Math.random().toFloat() - 0.5f) * d * 2f
                }
            })

            addUpdateListener { anim ->
                val t = anim.animatedFraction
                for (i in 0 until n) state.curRadii[i] = lerp(state.fromRadii[i], state.toRadii[i], t)
                state.curCx = lerp(state.fromCx, state.toCx, t)
                state.curCy = lerp(state.fromCy, state.toCy, t)
                // Only blob 0 triggers the redraw; blob 1 piggybacks on the same frame.
                if (index == 0) invalidate()
            }
        }
    }

    init {
        if (blobCount > 1) {
            animators.drop(1).forEach { anim ->
                anim.addUpdateListener { invalidate() }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animators.forEach { it.start() }
    }

    override fun onDetachedFromWindow() {
        animators.forEach { it.cancel() }
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return
        val w = width.toFloat()
        val h = height.toFloat()

        states.forEachIndexed { index, state ->
            val color = MaterialColors.getColor(this, state.cfg.colorAttr, 0)
            paints[index].color = color
            paints[index].alpha = state.cfg.alpha
            // Convert fractional center to pixels. Values outside [0,w] or [0,h] are fine:
            // Canvas clips to the View bounds automatically, so off-screen parts are just not drawn.
            val radius = minOf(w, h) * state.cfg.radiusFactor
            buildBlobPath(paths[index], w * state.curCx, h * state.curCy, radius, state.curRadii)
            canvas.drawPath(paths[index], paints[index])
        }
    }

    private fun buildBlobPath(path: Path, cx: Float, cy: Float, baseRadius: Float, radii: FloatArray) {
        path.reset()
        val px = FloatArray(n)
        val py = FloatArray(n)
        // Place anchor points evenly around a circle, each at a random radius multiplier.
        for (i in 0 until n) {
            val angle = 2.0 * Math.PI * i / n - Math.PI / 2.0
            val r = baseRadius * radii[i]
            px[i] = (cx + r * cos(angle)).toFloat()
            py[i] = (cy + r * sin(angle)).toFloat()
        }
        // Draw smooth cubic Bézier segments using Catmull-Rom handle derivation.
        path.moveTo(px[0], py[0])
        for (i in 0 until n) {
            val i0 = (i - 1 + n) % n
            val i2 = (i + 1) % n
            val i3 = (i + 2) % n
            val cp1x = px[i]  + (px[i2] - px[i0]) * tension
            val cp1y = py[i]  + (py[i2] - py[i0]) * tension
            val cp2x = px[i2] - (px[i3] - px[i])  * tension
            val cp2y = py[i2] - (py[i3] - py[i])  * tension
            path.cubicTo(cp1x, cp1y, cp2x, cp2y, px[i2], py[i2])
        }
        path.close()
    }

    // Radius multipliers in [0.60, 1.0]: the blob never collapses to a point but can bulge freely.
    private fun randomRadii() = FloatArray(n) { 0.60f + Math.random().toFloat() * 0.40f }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
}
