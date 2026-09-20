package com.example.videodownloader

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.videodownloader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askNotifPermission()

        val navHost = supportFragmentManager
            .findFragmentById(R.id.contentArea) as NavHostFragment
        val navController = navHost.navController

        val items = listOf(
            binding.navbarHost.navHome to "Home",
            binding.navbarHost.navDownloads to "Downloads",
            binding.navbarHost.navProfile to "Profile",
            binding.navbarHost.navApps to "More Apps"
        )

        items.forEach { (item, label) ->
            item.setOnClickListener {
                animateNavSelection(item, label, items.map { it.first })
                when (label) {
                    "Home" -> navController.navigate(R.id.homeFragment)
                    "Downloads" -> navController.navigate(R.id.downloadsFragment)
                    "Profile" -> navController.navigate(R.id.profileFragment)
                    "More Apps" -> { /* TODO */ }
                }
            }
        }
    }

    private fun askNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100
                )
            }
        }
    }

    private fun animateNavSelection(
        selected: FrameLayout, label: String, allItems: List<FrameLayout>
    ) {
        allItems.forEach { item ->
            val isSelected = item == selected
            item.background = if (isSelected)
                ContextCompat.getDrawable(this, R.drawable.navbar_active_pill) else null

            val container = item.getChildAt(0) as? LinearLayout
            if (container != null) {
                val icon = container.getChildAt(0) as ImageView
                val text = container.getChildAt(1) as? TextView

                if (isSelected && text == null) {
                    val tv = TextView(this).apply {
                        this.text = label
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.nav_active_text))
                        textSize = 14f
                        setTypeface(null, Typeface.BOLD)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { marginStart = 14 }
                    }
                    container.addView(tv)
                    icon.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.nav_active_text))
                } else if (!isSelected && text != null) {
                    container.removeView(text)
                    icon.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.nav_icon_inactive))
                }
            } else {
                wrapInactiveItem(item, label, isSelected)
            }
        }
    }

    private fun wrapInactiveItem(item: FrameLayout, label: String, isSelected: Boolean) {
        if (item.childCount == 1 && item.getChildAt(0) is ImageView) {
            val icon = item.getChildAt(0) as ImageView
            item.removeView(icon)
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
                addView(icon)
                if (isSelected) {
                    addView(TextView(this@MainActivity).apply {
                        text = label
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.nav_active_text))
                        textSize = 14f
                        setTypeface(null, Typeface.BOLD)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { marginStart = 14 }
                    })
                }
            }
            item.addView(container)
        }
    }
}