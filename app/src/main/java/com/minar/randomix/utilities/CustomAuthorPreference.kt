package com.minar.randomix.utilities

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity
import androidx.core.net.toUri

class CustomAuthorPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs), View.OnClickListener {

    private var easterEgg = 0

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val v = holder.itemView

        val logo = v.findViewById<ImageView>(R.id.imageMinar)
        val l1 = v.findViewById<ImageView>(R.id.minarig)
        val l2 = v.findViewById<ImageView>(R.id.minartt)
        val l3 = v.findViewById<ImageView>(R.id.minarps)
        val l4 = v.findViewById<ImageView>(R.id.minargit)
        val l5 = v.findViewById<ImageView>(R.id.minarsite)
        val translate = v.findViewById<Button>(R.id.translateButton)

        listOf(logo, l1, l2, l3, l4, l5, translate).forEach { it.setOnClickListener(this) }

        v.postDelayed({ (logo.drawable as? Animatable)?.start() }, 200)
    }

    override fun onClick(v: View) {
        val act = context as? MainActivity
        when (v.id) {
            R.id.imageMinar -> {
                if (easterEgg == 3) {
                    Toast.makeText(context, context.getString(R.string.easter_egg), Toast.LENGTH_SHORT).show()
                    easterEgg = 0
                } else easterEgg++
            }
            R.id.translateButton -> openUrl(act, R.string.dev_crowdin)
            R.id.minarig -> openUrl(act, R.string.dev_instagram)
            R.id.minartt -> openUrl(act, R.string.dev_telegram)
            R.id.minarps -> openUrl(act, R.string.dev_other_apps)
            R.id.minargit -> openUrl(act, R.string.dev_github)
            R.id.minarsite -> openUrl(act, R.string.dev_personal_site)
        }
    }

    private fun openUrl(act: MainActivity?, urlResId: Int) {
        act?.vibrate()
        val uri = context.getString(urlResId).toUri()
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
