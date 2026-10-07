package com.minar.randomix.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity
import com.minar.randomix.databinding.AboutRowBinding
import com.minar.randomix.databinding.FragmentAboutBinding
import com.minar.randomix.utilities.addPageInsets
import com.minar.randomix.utilities.animateChildrenCascade

/**
 * The former author preference, promoted to a page of its own: it is opened by the navbar action
 * button while the settings are on screen. No toolbar and no back arrow, the back gesture and the
 * navbar button are the way out.
 */
class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private val mainActivity get() = activity as? MainActivity

    // Easter egg stuff, why not
    private var easterEggCounter = 0

    companion object {
        const val EASTER_EGG_TAPS = 4
        const val LOGO_DELAY = 300L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageMinar.setOnClickListener { onLogoClick() }
        binding.minarig.setOnClickListener { openLink(R.string.dev_instagram) }
        binding.minartg.setOnClickListener { openLink(R.string.dev_telegram) }
        binding.minarps.setOnClickListener { openLink(R.string.dev_other_apps) }
        binding.minargit.setOnClickListener { openLink(R.string.dev_github) }
        binding.minarsite.setOnClickListener { openLink(R.string.dev_personal_site) }
        buildRows()

        // Spawn the logo with a little delay, unless the screen is already gone
        binding.imageMinar.postDelayed({
            (_binding?.imageMinar?.drawable as? Animatable)?.start()
        }, LOGO_DELAY)
        binding.aboutRows.animateChildrenCascade()
        binding.aboutScroll.addPageInsets()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // The rows live in code instead of in the layout: they are the same shape repeated, and the
    // language one only exists on Android 13+
    private fun buildRows() {
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        addRow(R.drawable.ic_info_24dp, getString(R.string.about_version), packageInfo.versionName) {
            openAppInfo()
        }
        // Per app language, backed by the locale config AGP generates from the translations
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) addRow(
            R.drawable.ic_language_24dp,
            getString(R.string.about_language_title),
            getString(R.string.about_language_summary)
        ) { openLanguageSettings() }
        addRow(
            R.drawable.ic_translate_24dp,
            getString(R.string.translate_app),
            getString(R.string.about_translate_summary)
        ) { openUrl(getString(R.string.dev_crowdin)) }
        addRow(
            R.drawable.ic_star_24dp,
            getString(R.string.about_rate_title),
            getString(R.string.about_rate_summary)
        ) { openUrl(getString(R.string.about_store_url)) }
        addRow(
            R.drawable.ic_share_black_24dp,
            getString(R.string.about_share_title),
            getString(R.string.about_share_summary)
        ) { shareApp() }
        addRow(
            R.drawable.ic_apps_email_24dp,
            getString(R.string.about_contact_title),
            getString(R.string.dev_email)
        ) { sendMail() }
    }

    private fun addRow(
        @DrawableRes icon: Int,
        title: String,
        subtitle: String?,
        onClick: () -> Unit
    ) {
        val row = AboutRowBinding.inflate(layoutInflater, binding.aboutRows, false)
        row.aboutRowIcon.setImageResource(icon)
        row.aboutRowTitle.text = title
        if (subtitle.isNullOrBlank()) row.aboutRowSubtitle.isVisible = false
        else row.aboutRowSubtitle.text = subtitle
        row.root.setOnClickListener {
            mainActivity?.vibrate()
            onClick()
        }
        binding.aboutRows.addView(row.root)
    }

    private fun onLogoClick() {
        if (easterEggCounter < EASTER_EGG_TAPS) {
            easterEggCounter++
            return
        }
        easterEggCounter = 0
        (binding.imageMinar.drawable as? Animatable)?.start()
        mainActivity?.showSnackbar(getString(R.string.easter_egg))
    }

    private fun openLink(stringRes: Int) {
        mainActivity?.vibrate()
        openUrl(getString(stringRes))
    }

    // Every row ends up firing an implicit intent, and any of them can be unhandled on a device
    // with no browser, no mail client or a stripped down settings app
    private fun startSafely(intent: Intent, onMissing: (() -> Unit)? = null) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            if (onMissing != null) onMissing()
            else mainActivity?.showSnackbar(getString(R.string.about_no_app_found))
        }
    }

    private fun openUrl(url: String) = startSafely(Intent(Intent.ACTION_VIEW, url.toUri()))

    private fun openAppInfo() = startSafely(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "package:${requireContext().packageName}".toUri()
        )
    )

    // Only reachable from the row that buildRows() adds on Android 13+
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun openLanguageSettings() = startSafely(
        Intent(
            Settings.ACTION_APP_LOCALE_SETTINGS,
            "package:${requireContext().packageName}".toUri()
        )
    ) { openAppInfo() }

    private fun shareApp() = startSafely(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                .putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.about_share_text, getString(R.string.about_store_url))
                ),
            null
        )
    )

    private fun sendMail() = startSafely(
        Intent(Intent.ACTION_SENDTO, "mailto:${getString(R.string.dev_email)}".toUri())
            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
    )
}
