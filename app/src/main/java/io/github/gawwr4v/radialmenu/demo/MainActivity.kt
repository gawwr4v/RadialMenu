package io.github.gawwr4v.radialmenu.demo

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import io.github.gawwr4v.radialmenu.RadialMenuItem
import io.github.gawwr4v.radialmenu.RadialMenuOverlay
import io.github.gawwr4v.radialmenu.RadialMenuView
import io.github.gawwr4v.radialmenu.RadialMenuWrapper
import io.github.gawwr4v.radialmenu.toPainter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val radialMenu = findViewById<RadialMenuView>(R.id.radialMenu)
        val composeView = findViewById<ComposeView>(R.id.composeView)
        val viewDemoContainer = findViewById<View>(R.id.viewDemoContainer)
        val btnShowViewDemo = findViewById<Button>(R.id.btnShowViewDemo)
        val btnShowComposeDemo = findViewById<Button>(R.id.btnShowComposeDemo)

        setupViewDemo(radialMenu)
        setupComposeDemo(composeView)

        btnShowViewDemo.setOnClickListener {
            viewDemoContainer.visibility = View.VISIBLE
            composeView.visibility = View.GONE
        }

        btnShowComposeDemo.setOnClickListener {
            viewDemoContainer.visibility = View.GONE
            composeView.visibility = View.VISIBLE
        }
    }

    private fun createItems(): List<RadialMenuItem> {
        val icon1 = AppCompatResources.getDrawable(this, android.R.drawable.ic_menu_compass)!!
        val icon2 = AppCompatResources.getDrawable(this, android.R.drawable.ic_menu_camera)!!
        val icon3 = AppCompatResources.getDrawable(this, android.R.drawable.ic_menu_edit)!!

        return listOf(
            RadialMenuItem(id = 1, icon = icon1.toPainter(), label = "Action 1"),
            RadialMenuItem(id = 2, icon = icon2.toPainter(), label = "Action 2"),
            RadialMenuItem(id = 3, icon = icon3.toPainter(), label = "Action 3")
        )
    }

    private fun setupViewDemo(radialMenu: RadialMenuView) {
        radialMenu.setItems(createItems())

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
            val items = createItems()

            Box {
                RadialMenuWrapper(
                    items = items,
                    onItemSelected = { item ->
                        Toast.makeText(this@MainActivity, "Compose: ${item.label}", Toast.LENGTH_SHORT).show()
                    },
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