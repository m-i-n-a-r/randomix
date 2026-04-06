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
import com.google.android.material.snackbar.Snackbar
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity
import com.minar.randomix.utilities.ShakeEventListener
import java.util.Random

class CoinFragment : Fragment(), View.OnClickListener {

    private var act: MainActivity? = null
    private var shakeEnabled = false
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorListener: ShakeEventListener
    private var notFirstFlip = false
    private var lastResult = false
    private var streakCount = 0

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_coin, container, false)
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (sp.getBoolean("hide_descriptions", false))
            v.findViewById<View>(R.id.descriptionCoin).visibility = View.GONE

        v.findViewById<ImageView>(R.id.coinButtonAnimation).setOnClickListener(this)

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
        if (v.id == R.id.coinButtonAnimation) mainThrow()
    }

    private fun mainThrow() {
        if (shakeEnabled) sensorManager.unregisterListener(sensorListener)
        val coinAnimation = requireView().findViewById<ImageView>(R.id.coinButtonAnimation)
        coinAnimation.isClickable = false

        act?.vibrate()
        act?.playSound(2)

        if (notFirstFlip) {
            runResetAnimation()
            requireView().postDelayed(::flipAndRunMainAnimation, 500)
        } else {
            flipAndRunMainAnimation()
        }

        requireView().postDelayed({
            if (shakeEnabled) sensorManager.registerListener(
                sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI
            )
            coinAnimation.isClickable = true
        }, 2000)

        if (!notFirstFlip) notFirstFlip = true
    }

    private fun flipAndRunMainAnimation() {
        if (!isAdded) return
        val textViewResult = requireView().findViewById<TextView>(R.id.resultCoin)
        val coinAnimation = requireView().findViewById<ImageView>(R.id.coinButtonAnimation)
        val resultHead = getString(R.string.result_head)
        val resultTail = getString(R.string.result_tail)

        val animIn = AlphaAnimation(1f, 0f).apply { duration = 1500 }
        val animOut = AlphaAnimation(0f, 1f).apply { duration = 1000 }
        textViewResult.startAnimation(animIn)

        val n = Random().nextInt(2)
        val resId: Int
        val result: String
        val isHead: Boolean

        if (n == 1) {
            resId = R.drawable.coin_head_vector_animation
            result = resultHead
            isHead = true
        } else {
            resId = R.drawable.coin_tail_vector_animation
            result = resultTail
            isHead = false
        }

        if (notFirstFlip && isHead == lastResult) streakCount++ else streakCount = 1
        lastResult = isHead

        coinAnimation.setImageResource(resId)
        (coinAnimation.drawable as? Animatable)?.start()

        val currentStreak = streakCount
        requireView().postDelayed({
            textViewResult.text = result
            textViewResult.startAnimation(animOut)
            when (currentStreak) {
                3 -> {
                    Snackbar.make(
                        requireView(),
                        "🔥 Three in a row!",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                5 -> {
                    Snackbar.make(requireView(), "🤯 FIVE IN A ROW!", Snackbar.LENGTH_LONG)
                        .show()
                }

                10 -> {
                    Snackbar.make(
                        requireView(),
                        "TEN? Seriously? Are you mad bro?",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }

                20 -> {
                    Snackbar.make(
                        requireView(),
                        "This is literally impossible. It won't happen. No. Way. You can only" +
                                " see this part of the easter egg in the source code. And if you " +
                                "see this: star the repo!",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
            }
        }, 1500)
    }

    private fun runResetAnimation() {
        val coinAnimation = requireView().findViewById<ImageView>(R.id.coinButtonAnimation)
        val resId = if (lastResult) R.drawable.coin_head_to_start_vector_animation
        else R.drawable.coin_tail_to_start_vector_animation
        coinAnimation.setImageResource(resId)
        (coinAnimation.drawable as? Animatable)?.start()
    }
}
