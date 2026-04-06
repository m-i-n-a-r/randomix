package com.minar.randomix.utilities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.google.android.material.color.MaterialColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class BlobBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Random number of blobs per app launch (1–4). The rest of the config is fixed.
    private val blobCount = Random.nextInt(1, 5)

    // 12 anchor points = 30° steps.
    private val n = 12

    // Catmull-Rom tension in safe range for closed organic curves.
    // Above ~0.30 with steep radii differences the spline self-intersects.
    private val tension = 0.26f

    private data class BlobConfig(
        // Theme color attribute for the blob fill.
        val colorAttr: Int,
        // Opacity 0–255. Keep low (15–55) so blobs stay atmospheric.
        val alpha: Int,
        // Base radius as a fraction of min(screenWidth, screenHeight).
        val radiusFactor: Float,
        // Duration of one shape morph cycle in ms.
        val morphMs: Long,
        // Duration of one position drift cycle in ms. Much longer than morphMs
        // so movement is slow and shape change dominates visually.
        val driftMs: Long,
        // Max center displacement from screen midpoint as a fraction of screen size.
        // Values above 0.5 allow the center to leave the screen — blob bleeds off the edge.
        val drift: Float,
        // Initial center as fractions of width/height. Can exceed [0,1] to start off-screen.
        val initCx: Float,
        val initCy: Float
    )

    private val configs = listOf(
        BlobConfig(
            colorAttr    = com.google.android.material.R.attr.colorPrimaryFixed,
            alpha        = 50,
            radiusFactor = 0.52f,
            morphMs      = 6_500L,
            driftMs      = 17_000L,
            drift        = 0.55f,
            initCx       = 0.08f,
            initCy       = 0.05f
        ),
        BlobConfig(
            colorAttr    = com.google.android.material.R.attr.colorSecondary,
            alpha        = 35,
            radiusFactor = 0.43f,
            morphMs      = 7_800L,
            driftMs      = 20_000L,
            drift        = 0.50f,
            initCx       = 0.55f,
            initCy       = 0.82f
        ),
        BlobConfig(
            colorAttr    = com.google.android.material.R.attr.colorTertiary,
            alpha        = 25,
            radiusFactor = 0.33f,
            morphMs      = 5_200L,
            driftMs      = 14_000L,
            drift        = 0.45f,
            initCx       = 0.95f,
            initCy       = 0.52f
        ),
        BlobConfig(
            colorAttr    = com.google.android.material.R.attr.colorPrimaryFixed,
            alpha        = 18,
            radiusFactor = 0.38f,
            morphMs      = 9_100L,
            driftMs      = 23_000L,
            drift        = 0.40f,
            initCx       = 0.30f,
            initCy       = 0.60f
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

    // Morph animators: handle shape transitions. AccelerateDecelerate makes each shape
    // linger at start/end so the eye can register it before transitioning.
    // Blob 0 is the master that drives screen redraws; all others just update state.
    private val morphAnimators: List<ValueAnimator> = states.mapIndexed { index, state ->
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration     = state.cfg.morphMs
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount  = ValueAnimator.INFINITE
            repeatMode   = ValueAnimator.RESTART

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    state.fromRadii = state.curRadii.copyOf()
                    state.toRadii   = randomRadii()
                }
            })

            addUpdateListener {
                val t = animatedFraction
                for (i in 0 until n) state.curRadii[i] = lerp(state.fromRadii[i], state.toRadii[i], t)
                if (index == 0) invalidate()
            }
        }
    }

    // Drift animators: handle center position only, completely separate from morphing.
    // LinearInterpolator = constant velocity, no acceleration spikes mid-journey.
    private val driftAnimators: List<ValueAnimator> = states.map { state ->
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration     = state.cfg.driftMs
            interpolator = LinearInterpolator()
            repeatCount  = ValueAnimator.INFINITE
            repeatMode   = ValueAnimator.RESTART

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    state.fromCx = state.toCx
                    state.fromCy = state.toCy
                    val d = state.cfg.drift
                    state.toCx = 0.50f + (Random.nextFloat() - 0.5f) * d * 2f
                    state.toCy = 0.50f + (Random.nextFloat() - 0.5f) * d * 2f
                }
            })

            // Only update state — invalidate is driven exclusively by morphAnimator[0].
            addUpdateListener {
                val t = animatedFraction
                state.curCx = lerp(state.fromCx, state.toCx, t)
                state.curCy = lerp(state.fromCy, state.toCy, t)
            }
        }
    }

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (key != "blob") return@OnSharedPreferenceChangeListener
        if (sp.getBoolean("blob", true)) {
            morphAnimators.forEach { if (!it.isRunning) it.start() }
            driftAnimators.forEach { if (!it.isRunning) it.start() }
        } else {
            morphAnimators.forEach { it.cancel() }
            driftAnimators.forEach { it.cancel() }
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val sp = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        sp.registerOnSharedPreferenceChangeListener(prefListener)
        if (!sp.getBoolean("blob", true)) return
        morphAnimators.forEach { it.start() }
        driftAnimators.forEach { it.start() }
    }

    override fun onDetachedFromWindow() {
        val sp = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        sp.unregisterOnSharedPreferenceChangeListener(prefListener)
        morphAnimators.forEach { it.cancel() }
        driftAnimators.forEach { it.cancel() }
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return
        if (morphAnimators.none { it.isRunning }) return
        val w = width.toFloat()
        val h = height.toFloat()

        states.forEachIndexed { index, state ->
            val color = MaterialColors.getColor(this, state.cfg.colorAttr, 0)
            paints[index].color = color
            paints[index].alpha = state.cfg.alpha
            // cx/cy in pixels. Canvas clips automatically if the blob extends off-screen.
            val radius = minOf(w, h) * state.cfg.radiusFactor
            buildBlobPath(paths[index], w * state.curCx, h * state.curCy, radius, state.curRadii)
            canvas.drawPath(paths[index], paints[index])
        }
    }

    private fun buildBlobPath(path: Path, cx: Float, cy: Float, baseRadius: Float, radii: FloatArray) {
        path.reset()
        val px = FloatArray(n)
        val py = FloatArray(n)
        // Anchor points evenly distributed around a circle, each scaled by its radii multiplier.
        for (i in 0 until n) {
            val angle = 2.0 * Math.PI * i / n - Math.PI / 2.0
            val r = baseRadius * radii[i]
            px[i] = (cx + r * cos(angle)).toFloat()
            py[i] = (cy + r * sin(angle)).toFloat()
        }
        // Cubic Bézier segments via Catmull-Rom handle derivation.
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

    // Wide variance [0.25, 1.0] makes morphing clearly visible.
    // Safe with tension=0.26 — no self-intersections.
    private fun randomRadii() = FloatArray(n) { 0.25f + Random.nextFloat() * 0.75f }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
}