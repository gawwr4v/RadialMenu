package io.github.gawwr4v.radialmenu.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import io.github.gawwr4v.radialmenu.RadialMenuItem
import io.github.gawwr4v.radialmenu.RadialMenuOverlay
import io.github.gawwr4v.radialmenu.RadialMenuView
import io.github.gawwr4v.radialmenu.RadialMenuWrapper

class MainActivity : AppCompatActivity() {

    private var composeItemCount by mutableIntStateOf(4)
    private var composeEdgeHugEnabled by mutableStateOf(false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val radialMenu = findViewById<RadialMenuView>(R.id.radialMenu)
        val composeView = findViewById<ComposeView>(R.id.composeView)
        val viewDemoContainer = findViewById<View>(R.id.viewDemoContainer)
        val btnShowViewDemo = findViewById<android.widget.Button>(R.id.btnShowViewDemo)
        val btnShowComposeDemo = findViewById<android.widget.Button>(R.id.btnShowComposeDemo)
        val seekItemCount = findViewById<SeekBar>(R.id.seekItemCount)
        val tvItemCount = findViewById<TextView>(R.id.tvItemCount)
        val switchEdgeHug = findViewById<SwitchCompat>(R.id.switchEdgeHug)
        val controlsPanel = findViewById<LinearLayout>(R.id.controlsPanel)

        setupViewDemo(radialMenu, 4)
        setupComposeDemo(composeView)

        // Make controls panel draggable
        var dragStartX = 0f
        var dragStartY = 0f
        var panelStartX = 0f
        var panelStartY = 0f

        controlsPanel.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragStartX = event.rawX
                    dragStartY = event.rawY
                    panelStartX = view.translationX
                    panelStartY = view.translationY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - dragStartX
                    val dy = event.rawY - dragStartY
                    val parent = view.parent as View
                    val maxX = (parent.width - view.width) / 2f
                    val maxY = (parent.height - view.height) / 2f
                    view.translationX = (panelStartX + dx).coerceIn(-maxX, maxX)
                    view.translationY = (panelStartY + dy).coerceIn(-maxY, maxY)
                    true
                }
                else -> false
            }
        }

        btnShowViewDemo.setOnClickListener {
            viewDemoContainer.visibility = View.VISIBLE
            composeView.visibility = View.GONE
        }

        btnShowComposeDemo.setOnClickListener {
            viewDemoContainer.visibility = View.GONE
            composeView.visibility = View.VISIBLE
        }

        seekItemCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val count = progress + 2
                tvItemCount.text = "Items: $count"
                setupViewDemo(radialMenu, count)
                composeItemCount = count
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        switchEdgeHug.setOnCheckedChangeListener { _, isChecked ->
            radialMenu.enableEdgeHugLayout = isChecked
            composeEdgeHugEnabled = isChecked
        }
    }

    private fun createItems(count: Int): List<RadialMenuItem> {
        val iconResources = listOf(
            android.R.drawable.ic_menu_compass,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_edit,
            android.R.drawable.ic_menu_search,
            android.R.drawable.ic_menu_share,
            android.R.drawable.ic_menu_send,
            android.R.drawable.ic_menu_info_details,
            android.R.drawable.ic_menu_preferences
        )
        return (0 until count.coerceIn(2, 8)).map { i ->
            RadialMenuItem(
                context = this,
                id = i + 1,
                iconRes = iconResources[i],
                label = "Action ${i + 1}"
            )
        }
    }

    private fun setupViewDemo(radialMenu: RadialMenuView, itemCount: Int) {
        radialMenu.setItems(createItems(itemCount))
        radialMenu.onItemSelected = { item ->
            Toast.makeText(this, "Selected: ${item.label}", Toast.LENGTH_SHORT).show()
        }
        radialMenu.onTap = {
            Toast.makeText(this, "Tapped", Toast.LENGTH_SHORT).show()
        }
        radialMenu.onDoubleTap = {
            Toast.makeText(this, "Double tapped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupComposeDemo(composeView: ComposeView) {
        composeView.setContent {
            val items = createItems(composeItemCount)
            val edgeHugEnabled = composeEdgeHugEnabled

            Box {
                RadialMenuWrapper(
                    items = items,
                    onItemSelected = { item ->
                        Toast.makeText(this@MainActivity, "Compose: ${item.label}", Toast.LENGTH_SHORT).show()
                    },
                    enableEdgeHugLayout = edgeHugEnabled,
                    onTap = {
                        Toast.makeText(this@MainActivity, "Compose Tapped", Toast.LENGTH_SHORT).show()
                    },
                    onDoubleTap = {
                        Toast.makeText(this@MainActivity, "Compose Double tapped", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Long press anywhere (Compose Demo)",
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
                RadialMenuOverlay(items = items)
            }
        }
    }
}
