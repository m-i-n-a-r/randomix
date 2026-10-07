package com.minar.randomix.utilities

import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.preference.PreferenceManager
import com.google.android.material.color.MaterialColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// A few soft shapes drifting behind the pages. Every blob is a circle bent by two or three slow
// waves running around it, so it stays round and organic whatever the moment, and its center
// follows a closed curve, so it never has to turn back. Time is the only input: no target to
// reach, no restart, nothing to jump. The randomness is in the phases, picked once per launch
class BlobBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // A wave bending the contour: how many bumps around it, how deep, how fast it travels
    private class Wave(val lobes: Int, val depth: Float, val speed: Float, val phase: Float)

    private class Blob(
        val colorAttr: Int,
        val alpha: Float,
        // Radius as a fraction of the shorter side of the view
        val size: Float,
        // Center of the orbit, its reach on each axis and the speed on each axis (radians/s)
        val originX: Float,
        val originY: Float,
        val reachX: Float,
        val reachY: Float,
        val speedX: Float,
        val speedY: Float,
        val phaseX: Float,
        val phaseY: Float,
        val waves: List<Wave>,
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        var color = Color.TRANSPARENT
        val path = Path()
    }

    private val random = Random(System.nanoTime())
    private fun phase() = random.nextFloat() * 2f * PI.toFloat()

    // Gentle waves: the deepest one bends the contour by a fifth at most, never into a star
    private fun waves() = listOf(
        Wave(2, 0.11f + random.nextFloat() * 0.06f, 0.18f + random.nextFloat() * 0.10f, phase()),
        Wave(3, 0.06f + random.nextFloat() * 0.04f, -(0.24f + random.nextFloat() * 0.12f), phase()),
        Wave(5, 0.015f + random.nextFloat() * 0.015f, 0.35f + random.nextFloat() * 0.15f, phase()),
    )

    // The containers of the theme, not the accents: they are already tuned to sit on the
    // background, in light, dark and amoled alike
    // One to four of them, picked at every launch: the first is always there, the others join in
    // order, so even a single blob is the large primary one
    private val blobs = listOf(
        Blob(
            com.google.android.material.R.attr.colorPrimaryContainer, 0.55f, 0.50f,
            0.25f, 0.20f, 0.30f, 0.18f, 0.11f, 0.08f, phase(), phase(), waves()
        ),
        Blob(
            com.google.android.material.R.attr.colorTertiaryContainer, 0.32f, 0.42f,
            0.75f, 0.70f, 0.25f, 0.22f, 0.09f, 0.13f, phase(), phase(), waves()
        ),
        Blob(
            com.google.android.material.R.attr.colorSecondaryContainer, 0.40f, 0.34f,
            0.55f, 0.45f, 0.35f, 0.30f, 0.07f, 0.10f, phase(), phase(), waves()
        ),
        Blob(
            com.google.android.material.R.attr.colorPrimaryContainer, 0.30f, 0.30f,
            0.40f, 0.80f, 0.30f, 0.15f, 0.12f, 0.06f, phase(), phase(), waves()
        ),
    ).take(random.nextInt(1, 5))

    companion object {
        // Points sampled on each contour, joined by a smooth spline
        private const val CONTOUR_POINTS = 24

        // How much of the radius the edge takes to fade, where there's no blur to soften it
        private const val EDGE_SOFTNESS = 0.35f
        private const val BLUR_RADIUS_DP = 36f
    }

    private val xs = FloatArray(CONTOUR_POINTS)
    private val ys = FloatArray(CONTOUR_POINTS)

    // Seconds of animation, so a pause resumes exactly where the blobs were left
    private var time = random.nextFloat() * 1000f
    private val ticker = TimeAnimator().apply {
        setTimeListener { _, _, deltaMs ->
            time += deltaMs / 1000f
            invalidate()
        }
    }

    private val sharedPrefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private var enabled = sharedPrefs.getBoolean("blob", true)

    // Set while an opaque page covers the whole background: no point in drawing under it
    var paused = false
        set(value) {
            field = value
            updateTicker()
        }

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (key != "blob") return@OnSharedPreferenceChangeListener
        enabled = sp.getBoolean("blob", true)
        updateTicker()
        invalidate()
    }

    init {
        // The blur softens the whole layer at once, on the GPU
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = BLUR_RADIUS_DP * resources.displayMetrics.density
            setRenderEffect(RenderEffect.createBlurEffect(blur, blur, Shader.TileMode.DECAL))
        }
    }

    // Runs only when it can be seen and moved: shown, enabled, uncovered, and with the system
    // animations on. Otherwise the blobs stay still where they are
    private fun updateTicker() {
        val animatorsOn = Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                ValueAnimator.areAnimatorsEnabled()
        val run = enabled && !paused && isAttachedToWindow &&
                windowVisibility == VISIBLE && animatorsOn
        if (run && !ticker.isStarted) ticker.start()
        else if (!run && ticker.isStarted) ticker.end()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener)
        // The theme can't change without a new activity, and a new view with it
        blobs.forEach { it.color = MaterialColors.getColor(this, it.colorAttr, Color.TRANSPARENT) }
        updateTicker()
    }

    override fun onDetachedFromWindow() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        ticker.end()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        updateTicker()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!enabled || width == 0 || height == 0) return
        val w = width.toFloat()
        val h = height.toFloat()
        val shortSide = min(w, h)

        blobs.forEach { blob ->
            val cx = w * (blob.originX + blob.reachX * sin(blob.speedX * time + blob.phaseX))
            val cy = h * (blob.originY + blob.reachY * sin(blob.speedY * time + blob.phaseY))
            val radius = shortSide * blob.size
            buildContour(blob, cx, cy, radius)
            blob.paint.shader = edgeShader(blob, cx, cy, radius)
            canvas.drawPath(blob.path, blob.paint)
        }
    }

    // Solid at the core, fading on the outer part: the blur finishes the job where it exists
    private fun edgeShader(blob: Blob, cx: Float, cy: Float, radius: Float): Shader {
        val core = ColorUtils.setAlphaComponent(blob.color, (255 * blob.alpha).toInt())
        val rim = ColorUtils.setAlphaComponent(blob.color, (255 * blob.alpha * 0.45f).toInt())
        return RadialGradient(
            cx, cy, radius * (1f + EDGE_SOFTNESS),
            intArrayOf(core, core, rim),
            floatArrayOf(0f, 1f - EDGE_SOFTNESS, 1f),
            Shader.TileMode.CLAMP
        )
    }

    private fun buildContour(blob: Blob, cx: Float, cy: Float, radius: Float) {
        for (i in 0 until CONTOUR_POINTS) {
            val angle = 2f * PI.toFloat() * i / CONTOUR_POINTS
            var bend = 1f
            blob.waves.forEach { bend += it.depth * sin(it.lobes * angle + it.speed * time + it.phase) }
            xs[i] = cx + radius * bend * cos(angle)
            ys[i] = cy + radius * bend * sin(angle)
        }
        // Catmull-Rom through the samples, as cubic segments
        val path = blob.path
        path.reset()
        path.moveTo(xs[0], ys[0])
        for (i in 0 until CONTOUR_POINTS) {
            val p0 = (i - 1 + CONTOUR_POINTS) % CONTOUR_POINTS
            val p2 = (i + 1) % CONTOUR_POINTS
            val p3 = (i + 2) % CONTOUR_POINTS
            path.cubicTo(
                xs[i] + (xs[p2] - xs[p0]) / 6f, ys[i] + (ys[p2] - ys[p0]) / 6f,
                xs[p2] - (xs[p3] - xs[i]) / 6f, ys[p2] - (ys[p3] - ys[i]) / 6f,
                xs[p2], ys[p2]
            )
        }
        path.close()
    }
}
