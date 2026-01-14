package com.example.argh

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    private lateinit var taskCompletedBtn: Button
    private lateinit var timeText: TextView
    private lateinit var arcView: TimerArcView
    private lateinit var focusBtn: Button
    private lateinit var resetBtn: Button

    private var totalMillis: Long = 25 * 60 * 1000L
    private var timeLeftMillis: Long = totalMillis

    private var timer: CountDownTimer? = null
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Views
        taskCompletedBtn = findViewById(R.id.btnTaskCompleted)
        timeText = findViewById(R.id.txtTime)
        arcView = findViewById(R.id.arcView)
        focusBtn = findViewById(R.id.btnFocus)
        resetBtn = findViewById(R.id.btnReset)

        updateCompletedCount()
        updateTimeUI()

        taskCompletedBtn.setOnClickListener {
            startActivity(Intent(this, TodoActivity::class.java))
        }

        focusBtn.setOnClickListener {
            if (isRunning) pauseTimer() else startTimer()
        }

        resetBtn.setOnClickListener {
            resetTimer()
        }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_timer -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_todo -> {
                    startActivity(Intent(this, TodoActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateCompletedCount() // ✅ sync after returning from To-Do
    }

    private fun updateCompletedCount() {
        val count = TaskStorage.completedCount(this)
        taskCompletedBtn.text = "Task Completed: $count"
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(timeLeftMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMillis = millisUntilFinished
                updateTimeUI()
            }

            override fun onFinish() {
                isRunning = false
                focusBtn.text = "Focus"
                timeLeftMillis = 0
                updateTimeUI()
            }
        }.start()

        isRunning = true
        focusBtn.text = "Pause"
    }

    private fun pauseTimer() {
        timer?.cancel()
        timer = null
        isRunning = false
        focusBtn.text = "Focus"
    }

    private fun resetTimer() {
        pauseTimer()
        timeLeftMillis = totalMillis
        updateTimeUI()
    }

    private fun updateTimeUI() {
        val totalSeconds = timeLeftMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        timeText.text = String.format("%02d:%02d", minutes, seconds)

        val progress = if (totalMillis > 0) timeLeftMillis.toFloat() / totalMillis.toFloat() else 0f
        arcView.setProgress(progress)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}
