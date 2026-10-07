package com.minar.randomix.activities

import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Animatable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.LinearLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.edit
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.minar.randomix.R
import com.minar.randomix.databinding.ActivityMainBinding
import com.minar.randomix.databinding.NavTabBinding
import com.minar.randomix.utilities.AppRater
import com.minar.randomix.utilities.addInsetsByMargin
import com.minar.randomix.utilities.applyUserTheme
import com.minar.randomix.utilities.getThemeColor
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private lateinit var navTabs: List<NavTab>

    // The last tool used: the settings are opened on top of it, and closing them goes back there.
    // The about page is opened from the settings, so it counts as settings too
    private var selectedTabIndex = 0
    private var settingsOpen = false
    private var aboutOpen = false
    private var renderedActionMode: NavActionMode? = null

    // Read every time: the activity keeps some config changes, so this flips without a recreation
    private val isNavRail: Boolean
        get() = resources.getBoolean(R.bool.nav_rail)

    // A tool of the floating navbar, together with the icon and label of its tab
    private data class NavTab(
        val binding: NavTabBinding,
        @param:IdRes val destination: Int,
        @param:DrawableRes val icon: Int,
        @param:StringRes val label: Int,
        val pageKey: String,
    )

    // What the detached button on the right of the navbar does right now: open the settings from
    // a tool, the about page from the settings, and go back from the about page
    private enum class NavActionMode { SETTINGS, ABOUT, BACK }

    companion object {
        // Material 3 emphasized decelerate
        val NavTabInterpolator: Interpolator = PathInterpolatorCompat.create(0.05f, 0.7f, 0.1f, 1f)

        // One beat for the whole tab change: bounds, colors and label move together
        const val NAV_TAB_DURATION = 500L

        // The label is fully opaque halfway through, while the pill is still growing around it
        const val NAV_LABEL_FADE_PORTION = 0.5f

        // Each half of the action icon swap, and how far it turns on the way
        const val NAV_ACTION_SWAP_DURATION = 150L
        const val NAV_ACTION_SPIN = 90f

        const val SETTINGS_PAGE_KEY = "settings"
    }

    private val navController: NavController
        get() {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as NavHostFragment
            return navHostFragment.navController
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        // Show the introduction for the first launch
        if (!sharedPrefs.getBoolean("first", false)) {
            sharedPrefs.edit {
                // Set default accent based on the Android version
                putString("accent_color", if (Build.VERSION.SDK_INT >= 32) "monet" else "blue")
                putBoolean("first", true)
            }
            super.onCreate(savedInstanceState)
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
            return
        }

        // Set the base theme and the accent
        applyUserTheme(sharedPrefs)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge to edge, but specify the navigation bar color for android < Q
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.dark(
                    getThemeColor(com.google.android.material.R.attr.colorOutlineVariant, this)
                )
            )
        else {
            enableEdgeToEdge()
            window.isNavigationBarContrastEnforced = false
        }
        binding.floatingNavbar.addInsetsByMargin(bottom = true, left = true, right = true)

        // The floating navbar keeps its own selection, so tab, icon state and label move together
        navTabs = listOf(
            NavTab(
                binding.navTabRoulette, R.id.navigationRoulette,
                R.drawable.nav_animation_roulette, R.string.title_roulette, "roulette"
            ),
            NavTab(
                binding.navTabCoin, R.id.navigationCoin,
                R.drawable.nav_animation_coin, R.string.title_coin, "coin"
            ),
            NavTab(
                binding.navTabDice, R.id.navigationDice,
                R.drawable.nav_animation_dice, R.string.title_dice, "dice"
            ),
            NavTab(
                binding.navTabMagicBall, R.id.navigationMagicBall,
                R.drawable.nav_animation_magic_ball, R.string.title_magic_ball, "magicBall"
            ),
        )
        navTabs.forEachIndexed { index, tab ->
            tab.binding.tabIcon.setImageResource(tab.icon)
            tab.binding.tabLabel.setText(tab.label)
            tab.binding.root.contentDescription = getString(tab.label)
            tab.binding.root.setOnClickListener {
                if (index == selectedTabIndex && !settingsOpen) return@setOnClickListener
                vibrate()
                selectNavTab(index)
            }
        }

        // Read before the listener below, which saves the start destination as the last page
        val lastPage = sharedPrefs.getString("last_page", "roulette")
        val lastPageSettings = sharedPrefs.getBoolean("last_page_settings", false) ||
                lastPage == SETTINGS_PAGE_KEY

        // The navbar follows the destination, whoever changed it: a tap, a back or a recreation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val tabIndex = navTabs.indexOfFirst { it.destination == destination.id }
            aboutOpen = destination.id == R.id.aboutFragment
            settingsOpen = destination.id == R.id.navigationSettings || aboutOpen
            if (tabIndex != -1) selectedTabIndex = tabIndex
            // The about page is opaque, the background would move for nobody
            binding.blobBackground.paused = aboutOpen
            sharedPrefs.edit {
                putString("last_page", navTabs[selectedTabIndex].pageKey)
                putBoolean("last_page_settings", settingsOpen)
            }
            renderNavTabs(animate = renderedActionMode != null)
            renderNavAction()
        }

        binding.navAction.setOnClickListener {
            vibrate()
            when (currentNavActionMode()) {
                NavActionMode.SETTINGS -> openSettings()
                NavActionMode.ABOUT ->
                    navController.navigate(R.id.action_navigationSettings_to_aboutFragment)

                NavActionMode.BACK -> navController.popBackStack()
            }
        }
        applyNavbarPlacement()

        // Open the last page used, the navigation restores it by itself after a recreation
        if (savedInstanceState == null) {
            val lastTab = navTabs.indexOfFirst { it.pageKey == lastPage }
            if (lastTab > 0) navController.navigateWithOptions(navTabs[lastTab].destination, false)
            if (lastPageSettings) openSettings(animate = false)
        }

        AppRater.appLaunched(this)
    }

    private fun NavController.navigateWithOptions(@IdRes destination: Int, animate: Boolean = true) {
        // Only way to use custom animations with a navbar of its own
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.nav_graph, true)
        if (animate) options
            .setEnterAnim(R.animator.nav_enter_anim)
            .setExitAnim(R.animator.nav_exit_anim)
            .setPopEnterAnim(R.animator.nav_pop_enter_anim)
            .setPopExitAnim(R.animator.nav_pop_exit_anim)
        navigate(destination, null, options.build())
    }

    // The settings are a real step on top of the tool they were opened from, so the back gesture
    // pops them with the predictive animation instead of jumping to the tool
    private fun openSettings(animate: Boolean = true) {
        val options = NavOptions.Builder().setLaunchSingleTop(true)
        if (animate) options
            .setEnterAnim(R.animator.nav_enter_anim)
            .setExitAnim(R.animator.nav_exit_anim)
            .setPopEnterAnim(R.animator.nav_pop_enter_anim)
            .setPopExitAnim(R.animator.nav_pop_exit_anim)
        navController.navigate(R.id.navigationSettings, null, options.build())
    }

    // Navigate to the tool of a tab, the destination listener updates the navbar
    private fun selectNavTab(index: Int) {
        selectedTabIndex = index
        navController.navigateWithOptions(navTabs[index].destination)
    }

    // Bottom bar in portrait, side rail in landscape, re-applied when the window changes size
    private fun applyNavbarPlacement() {
        val rail = isNavRail
        val margin = resources.getDimensionPixelSize(R.dimen.floating_navbar_margin)
        val spacing = resources.getDimensionPixelSize(R.dimen.nav_tab_spacing)
        val tabSize = resources.getDimensionPixelSize(R.dimen.nav_tab_height)
        val tabPadding =
            if (rail) 0 else resources.getDimensionPixelSize(R.dimen.nav_tab_padding)

        binding.floatingNavbar.orientation =
            if (rail) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
        binding.floatingNavbar.gravity =
            if (rail) Gravity.CENTER_HORIZONTAL else Gravity.CENTER_VERTICAL
        binding.floatingNavbar.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            gravity = if (rail) Gravity.END or Gravity.CENTER_VERTICAL
            else Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        // The pages make room for the navbar themselves, on the side it sits on now
        ViewCompat.requestApplyInsets(binding.navHostFragment)
        binding.navTabs.orientation =
            if (rail) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL

        navTabs.forEachIndexed { index, tab ->
            tab.binding.root.updateLayoutParams<LinearLayout.LayoutParams> {
                width = if (rail) tabSize else LinearLayout.LayoutParams.WRAP_CONTENT
                height = tabSize
                marginStart = if (!rail && index > 0) spacing else 0
                topMargin = if (rail && index > 0) spacing else 0
            }
            tab.binding.root.gravity = if (rail) Gravity.CENTER else Gravity.CENTER_VERTICAL
            tab.binding.root.updatePaddingRelative(start = tabPadding, end = tabPadding)
        }

        binding.navAction.updateLayoutParams<LinearLayout.LayoutParams> {
            marginStart = if (rail) 0 else margin
            topMargin = if (rail) margin else 0
        }

        renderNavTabs(animate = false)
    }

    // Everything keyed to the edge the navbar sits on has to be redone by hand
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyNavbarPlacement()
    }

    // Bounds, label and colors on one beat, icons morph after: together they fight each other.
    // With the settings open no tool is selected, and the pill shrinks to its icons
    private fun renderNavTabs(animate: Boolean) {
        // Material 3 roles: neutral bar, tinted active destination, idle tabs transparent
        val selectedContainer =
            getThemeColor(com.google.android.material.R.attr.colorSecondaryContainer, this)
        val selectedContent =
            getThemeColor(com.google.android.material.R.attr.colorOnSecondaryContainer, this)
        val idleContent =
            getThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, this)

        navTabs.forEachIndexed { index, tab ->
            val selected = index == selectedTabIndex && !settingsOpen
            animateTabLabel(tab, selected && !isNavRail, animate)
            tab.binding.root.isSelected = selected
            val container = if (selected) selectedContainer else 0
            val content = if (selected) selectedContent else idleContent
            animateTint(tab.binding.root.backgroundTintList?.defaultColor, container, animate) {
                tab.binding.root.backgroundTintList = ColorStateList.valueOf(it)
            }
            animateTint(tab.binding.tabIcon.imageTintList?.defaultColor, content, animate) {
                tab.binding.tabIcon.imageTintList = ColorStateList.valueOf(it)
                tab.binding.tabLabel.setTextColor(it)
            }
        }
        // The icons morph once the pill has finished growing around the label
        if (animate) binding.floatingNavbar.postDelayed(::morphNavTabIcons, NAV_TAB_DURATION)
        else morphNavTabIcons()
    }

    // The label drives the width, so the pill wrapping it grows and shrinks on the same animation.
    // Only its frame is resized: the label keeps its full width, so it is never laid out again
    // into a narrower space, where a single line gets cut and the letters trickle in one by one
    private fun animateTabLabel(tab: NavTab, selected: Boolean, animate: Boolean) {
        val frame = tab.binding.tabLabelFrame
        val label = tab.binding.tabLabel
        // Gone in the layout, the width and the alpha are what hide it from here on
        frame.isVisible = true
        val maxWidth = resources.getDimensionPixelSize(R.dimen.nav_tab_label_max_width)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST)
        val unspecified = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        label.measure(widthSpec, unspecified)
        val fullWidth = label.measuredWidth
        label.updateLayoutParams { width = fullWidth }
        val params = frame.layoutParams as ViewGroup.MarginLayoutParams
        val fullMargin = resources.getDimensionPixelSize(R.dimen.nav_tab_label_margin)

        fun apply(fraction: Float) {
            params.width = (fullWidth * fraction).toInt()
            params.marginStart = (fullMargin * fraction).toInt()
            label.alpha = (fraction / NAV_LABEL_FADE_PORTION).coerceAtMost(1f)
            frame.layoutParams = params
            frame.setTag(R.id.tag_nav_label_fraction, fraction)
        }

        val to = if (selected) 1f else 0f
        val from = frame.getTag(R.id.tag_nav_label_fraction) as? Float ?: (1f - to)
        if (!animate || from == to) {
            apply(to)
            return
        }
        ValueAnimator.ofFloat(from, to).apply {
            duration = NAV_TAB_DURATION
            interpolator = NavTabInterpolator
            addUpdateListener { apply(it.animatedValue as Float) }
        }.start()
    }

    // Reads the current selection, so a tab changed mid animation still lands right
    private fun morphNavTabIcons() = navTabs.forEachIndexed { index, tab ->
        tab.binding.tabIcon.isChecked = index == selectedTabIndex && !settingsOpen
    }

    // Cross-fade the tint, so the color lands together with the bounds
    private fun animateTint(from: Int?, to: Int, animate: Boolean, apply: (Int) -> Unit) {
        if (!animate || from == null || from == to) {
            apply(to)
            return
        }
        ValueAnimator.ofArgb(from, to).apply {
            duration = NAV_TAB_DURATION
            interpolator = NavTabInterpolator
            addUpdateListener { apply(it.animatedValue as Int) }
        }.start()
    }

    private fun currentNavActionMode(): NavActionMode = when {
        aboutOpen -> NavActionMode.BACK
        settingsOpen -> NavActionMode.ABOUT
        else -> NavActionMode.SETTINGS
    }

    // The old icon spins away and the new one spins in, landing where the old one left
    private fun renderNavAction() {
        val mode = currentNavActionMode()
        if (mode == renderedActionMode) return
        val animate = renderedActionMode != null
        renderedActionMode = mode

        val icon = binding.navActionIcon
        icon.animate().cancel()
        if (!animate) {
            showNavActionIcon(mode)
            return
        }
        icon.animate()
            .scaleX(0f).scaleY(0f).alpha(0f).rotation(-NAV_ACTION_SPIN)
            .setDuration(NAV_ACTION_SWAP_DURATION)
            .setInterpolator(NavTabInterpolator)
            .withEndAction {
                showNavActionIcon(mode)
                icon.rotation = NAV_ACTION_SPIN
                icon.animate()
                    .scaleX(1f).scaleY(1f).alpha(1f).rotation(0f)
                    .setDuration(NAV_ACTION_SWAP_DURATION)
                    .setInterpolator(NavTabInterpolator)
                    .withEndAction(null)
                    .start()
            }
            .start()
    }

    private fun showNavActionIcon(mode: NavActionMode) {
        val icon = binding.navActionIcon
        when (mode) {
            NavActionMode.SETTINGS -> {
                icon.contentDescription = getString(R.string.title_settings)
                icon.setImageResource(R.drawable.animated_nav_settings)
            }

            NavActionMode.ABOUT -> {
                icon.contentDescription = getString(R.string.about_title)
                icon.setImageResource(R.drawable.animated_info)
            }

            NavActionMode.BACK -> {
                icon.contentDescription = getString(R.string.back)
                icon.setImageResource(R.drawable.animated_arrow_back)
            }
        }
        // An icon drawn on a larger canvas, to give its animation room, keeps the size of the others
        val standard = resources.getDimension(R.dimen.nav_action_icon_canvas)
        val size = resources.getDimensionPixelSize(R.dimen.nav_action_icon_size)
        val scaled = icon.drawable?.let { (size * it.intrinsicWidth / standard).roundToInt() } ?: size
        if (icon.layoutParams.width != scaled) icon.updateLayoutParams {
            width = scaled
            height = scaled
        }
        (icon.drawable as? Animatable)?.start()
    }

    // Show a snackbar above the floating navbar
    fun showSnackbar(content: String) {
        val snackbar = Snackbar.make(binding.root, content, Snackbar.LENGTH_LONG)
        snackbar.isGestureInsetBottomIgnored = true
        snackbar.anchorView = binding.floatingNavbar
        snackbar.show()
    }

    // Vibrate using a standard vibration pattern
    // or use system Haptic feedback if vibration is disabled
    fun vibrate() {
        if (!sharedPrefs.getBoolean("vibration", true)) return

        // Deprecated for no reason
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                this.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Use system Haptic feedback if available, or a short vibration
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    vib.areEffectsSupported(VibrationEffect.EFFECT_CLICK)[0] == Vibrator.VIBRATION_EFFECT_SUPPORT_YES ->
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                vib.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))

            else -> @Suppress("DEPRECATION") vib.vibrate(30)
        }
    }

    fun playSound(fragmentNumber: Int) {
        if (!sharedPrefs.getBoolean("sound", false)) return
        val resId = when (fragmentNumber) {
            1 -> R.raw.roulette_sound
            2 -> R.raw.coin_sound
            3 -> R.raw.magicball_sound
            4 -> R.raw.dice_sound
            else -> return
        }
        val mp = MediaPlayer.create(this, resId) ?: return
        mp.start()
        mp.setOnCompletionListener { it.release() }
    }

    fun shakeAllowed(): Boolean = sharedPrefs.getBoolean("shake", false)
}
