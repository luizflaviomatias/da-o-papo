package app.daopapo.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/** Lê as respostas em voz alta, em português do Brasil, usando a voz do próprio celular. */
class Voz(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var pronta = false
    private val _falandoId = MutableStateFlow<Long?>(null)
    /** Qual mensagem está sendo lida agora (ou null). */
    val falandoId: StateFlow<Long?> = _falandoId

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val br = Locale.forLanguageTag("pt-BR")
        val r = tts.setLanguage(br)
        pronta = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
        tts.setSpeechRate(0.95f)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (utteranceId?.startsWith("fim-") == true) _falandoId.value = null
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { _falandoId.value = null }
        })
    }

    val disponivel: Boolean get() = pronta

    fun falar(id: Long, texto: String): Boolean {
        if (!pronta) return false
        tts.stop()
        // O leitor do Android tem limite de tamanho por fala; dividimos em pedaços.
        val pedacos = texto.chunked(TextToSpeech.getMaxSpeechInputLength() - 100)
        pedacos.forEachIndexed { i, p ->
            tts.speak(p, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, if (i == pedacos.lastIndex) "fim-$id" else "parte-$id-$i")
        }
        _falandoId.value = id
        return true
    }

    fun parar() {
        tts.stop()
        _falandoId.value = null
    }

    fun desligar() = tts.shutdown()
}
