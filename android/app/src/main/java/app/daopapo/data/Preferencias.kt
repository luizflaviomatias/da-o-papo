package app.daopapo.data

import android.content.Context
import app.daopapo.BuildConfig
import app.daopapo.ui.theme.Tema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Ajustes(
    val tema: Tema = Tema.SISTEMA,
    val modoCurto: Boolean = true,
    val lerEmVozAlta: Boolean = false,
    val servidor: String = BuildConfig.DEFAULT_SERVER_URL,
    val aceitouAviso: Boolean = false,
)

/** Guarda os ajustes no próprio celular. Nada disso vai para a internet. */
class Preferencias(context: Context) {
    private val prefs = context.getSharedPreferences("dao_papo", Context.MODE_PRIVATE)
    private val _ajustes = MutableStateFlow(ler())
    val ajustes: StateFlow<Ajustes> = _ajustes.asStateFlow()

    private fun ler() = Ajustes(
        tema = runCatching { Tema.valueOf(prefs.getString("tema", Tema.SISTEMA.name)!!) }.getOrDefault(Tema.SISTEMA),
        modoCurto = prefs.getBoolean("modo_curto", true),
        lerEmVozAlta = prefs.getBoolean("ler_voz", false),
        servidor = prefs.getString("servidor", null) ?: BuildConfig.DEFAULT_SERVER_URL,
        aceitouAviso = prefs.getBoolean("aceitou_aviso", false),
    )

    fun atualizar(mudanca: (Ajustes) -> Ajustes) {
        val novo = mudanca(_ajustes.value)
        prefs.edit()
            .putString("tema", novo.tema.name)
            .putBoolean("modo_curto", novo.modoCurto)
            .putBoolean("ler_voz", novo.lerEmVozAlta)
            .putString("servidor", novo.servidor.trim())
            .putBoolean("aceitou_aviso", novo.aceitouAviso)
            .apply()
        _ajustes.value = novo
    }

    companion object {
        @Volatile private var instancia: Preferencias? = null
        fun de(context: Context): Preferencias =
            instancia ?: synchronized(this) { instancia ?: Preferencias(context.applicationContext).also { instancia = it } }
    }
}
