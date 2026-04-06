package com.minar.randomix.fragments

import android.content.Context
import android.graphics.drawable.Animatable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity
import com.minar.randomix.utilities.ShakeEventListener
import java.util.Random

class MagicBallFragment : Fragment(), View.OnClickListener {

    private var act: MainActivity? = null
    private var shakeEnabled = false
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorListener: ShakeEventListener
    private val bottomSheet = MagicBallBottomSheet(this)
    private var customAnswers: Array<String>? = null

    override fun onResume() {
        super.onResume()
        if (shakeEnabled) sensorManager.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        if (shakeEnabled) sensorManager.unregisterListener(sensorListener)
        super.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_magic_ball, container, false)
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (sp.getBoolean("hide_descriptions", false))
            v.findViewById<View>(R.id.descriptionMagicBall).visibility = View.GONE

        v.findViewById<ImageView>(R.id.magicBallButtonAnimation).setOnClickListener(this)
        v.findViewById<ImageView>(R.id.magicBallRecentButton).setOnClickListener(this)

        act = activity as? MainActivity
        act?.let { a ->
            shakeEnabled = a.shakeAllowed()
            if (shakeEnabled) {
                sensorManager = a.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                sensorListener = ShakeEventListener().also { it.setOnShakeListener(::mainThrow) }
            }
        }
        return v
    }

    override fun onClick(v: View) {
        val view = requireView()
        val recentAnimation = view.findViewById<ImageView>(R.id.magicBallRecentButton)
        when (v.id) {
            R.id.magicBallRecentButton -> {
                (recentAnimation.drawable as? Animatable)?.start()
                (activity as? MainActivity)?.vibrate()
                if (!bottomSheet.isAdded)
                    bottomSheet.show(childFragmentManager, "magic_ball_bottom_sheet")
            }
            R.id.magicBallButtonAnimation -> mainThrow()
        }
    }

    fun setCustomAnswers(answers: Array<String>?) {
        customAnswers = answers
    }

    private fun mainThrow() {
        val magicBallAnimation = requireView().findViewById<ImageView>(R.id.magicBallButtonAnimation)
        if (shakeEnabled) sensorManager.unregisterListener(sensorListener)
        magicBallAnimation.isClickable = false
        (magicBallAnimation.drawable as? Animatable)?.start()

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val customAnswersEnabled = sp.getBoolean("custom_answers_active", false)
        if (customAnswersEnabled) {
            val raw = sp.getString("custom_answers", "") ?: ""
            if (raw.isNotEmpty()) customAnswers = raw.split(";").filter { it.isNotEmpty() }.toTypedArray()
        }

        act?.vibrate()
        act?.playSound(3)

        val answers: Array<String>
        val n: Int
        val ran = Random()

        if (customAnswers != null && customAnswers!!.size >= 2) {
            answers = customAnswers!!
            n = ran.nextInt(answers.size)
        } else {
            answers = buildMagicAnswers()
            n = if (sp.getBoolean("rude_answers", true)) ran.nextInt(36) else ran.nextInt(30)
        }

        val textViewResult = requireView().findViewById<TextView>(R.id.resultMagicBall)
        val animIn = AlphaAnimation(1f, 0f).apply { duration = 1700 }
        val animOut = AlphaAnimation(0f, 1f).apply { duration = 1000 }
        textViewResult.startAnimation(animIn)

        requireView().postDelayed({
            if (shakeEnabled) sensorManager.registerListener(
                sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI
            )
            textViewResult.text = answers[n]
            textViewResult.isSelected = true
            textViewResult.startAnimation(animOut)
            magicBallAnimation.isClickable = true
        }, 1700)
    }

    private fun buildMagicAnswers(): Array<String> = arrayOf(
        getString(R.string.magic_answer_1), getString(R.string.magic_answer_2),
        getString(R.string.magic_answer_3), getString(R.string.magic_answer_4),
        getString(R.string.magic_answer_5), getString(R.string.magic_answer_6),
        getString(R.string.magic_answer_7), getString(R.string.magic_answer_8),
        getString(R.string.magic_answer_9), getString(R.string.magic_answer_10),
        getString(R.string.magic_answer_11), getString(R.string.magic_answer_12),
        getString(R.string.magic_answer_13), getString(R.string.magic_answer_14),
        getString(R.string.magic_answer_15), getString(R.string.magic_answer_16),
        getString(R.string.magic_answer_17), getString(R.string.magic_answer_18),
        getString(R.string.magic_answer_19), getString(R.string.magic_answer_20),
        getString(R.string.magic_answer_21), getString(R.string.magic_answer_22),
        getString(R.string.magic_answer_23), getString(R.string.magic_answer_24),
        getString(R.string.magic_answer_25), getString(R.string.magic_answer_26),
        getString(R.string.magic_answer_27), getString(R.string.magic_answer_28),
        getString(R.string.magic_answer_29), getString(R.string.magic_answer_30),
        getString(R.string.magic_answer_rude_1), getString(R.string.magic_answer_rude_2),
        getString(R.string.magic_answer_rude_3), getString(R.string.magic_answer_rude_4),
        getString(R.string.magic_answer_rude_5), getString(R.string.magic_answer_rude_6)
    )
}
