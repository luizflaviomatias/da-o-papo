package app.daopapo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.daopapo.data.Preferencias
import app.daopapo.ui.AppDaOPapo
import app.daopapo.ui.theme.DaOPapoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = Preferencias.de(this)
        setContent {
            val ajustes by prefs.ajustes.collectAsState()
            DaOPapoTheme(tema = ajustes.tema) {
                AppDaOPapo(prefs)
            }
        }
    }
}
