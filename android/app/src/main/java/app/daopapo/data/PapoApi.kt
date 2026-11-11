package app.daopapo.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.URL

data class Mensagem(val papel: String, val texto: String) // papel: "user" ou "assistant"

data class Resposta(val texto: String, val skills: List<String>)

class PapoErro(message: String) : Exception(message)

/** Fala com o servidor do Dá o Papo. Se não houver servidor configurado, usa o modo demonstração. */
class PapoApi {

    suspend fun perguntar(
        servidor: String,
        pergunta: String,
        completo: Boolean,
        tema: String?,
        historico: List<Mensagem>,
    ): Resposta {
        if (servidor.isBlank()) return Demonstracao.responder(pergunta, completo)
        return withContext(Dispatchers.IO) { enviar(servidor, pergunta, completo, tema, historico) }
    }

    private fun enviar(
        servidor: String,
        pergunta: String,
        completo: Boolean,
        tema: String?,
        historico: List<Mensagem>,
    ): Resposta {
        val corpo = JSONObject()
            .put("pergunta", pergunta)
            .put("modo", if (completo) "completo" else "curto")
            .put("historico", JSONArray().apply {
                historico.takeLast(8).forEach { put(JSONObject().put("role", it.papel).put("content", it.texto)) }
            })
        if (tema != null) corpo.put("tema", tema)
        val url = URL(enderecoBase(servidor) + "/api/perguntar")

        // O servidor gratuito dorme. Enquanto acorda, ele responde "carregando" (503).
        // Nesse caso o app espera e tenta de novo, por até ~1 minuto e meio.
        val limite = System.currentTimeMillis() + 100_000
        while (true) {
            try {
                return tentar(url, corpo.toString())
            } catch (e: Acordando) {
                if (System.currentTimeMillis() + ESPERA_MS > limite) {
                    throw PapoErro("O servidor está demorando para acordar. Espere 1 minuto e tente de novo.")
                }
                Thread.sleep(ESPERA_MS)
            }
        }
    }

    private class Acordando : Exception()

    private fun tentar(url: URL, corpo: String): Resposta {
        val conexao = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }
        try {
            conexao.outputStream.use { it.write(corpo.toByteArray(Charsets.UTF_8)) }
            val codigo = conexao.responseCode
            val texto = (if (codigo in 200..299) conexao.inputStream else conexao.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(texto) }.getOrNull()
            val erroDoServidor = json?.optString("erro").orEmpty()
            if (codigo !in 200..299) {
                // Página de "carregando" do Render (não é o nosso JSON): ainda está acordando.
                if (codigo in setOf(502, 503, 504) && erroDoServidor.isBlank()) throw Acordando()
                throw PapoErro(erroDoServidor.ifBlank { "Não consegui responder agora (erro $codigo)." })
            }
            val resposta = json?.optString("resposta").orEmpty()
            if (resposta.isBlank()) throw PapoErro("A resposta veio vazia. Tente de novo.")
            val skills = json?.optJSONArray("skills")?.let { arr -> List(arr.length()) { arr.getString(it) } } ?: emptyList()
            return Resposta(resposta, skills)
        } catch (e: PapoErro) {
            throw e
        } catch (e: Acordando) {
            throw e
        } catch (e: UnknownHostException) {
            throw PapoErro("Sem internet agora. Confira o Wi-Fi ou os dados móveis e tente de novo.")
        } catch (e: SocketTimeoutException) {
            throw PapoErro("A internet está lenta e a resposta demorou demais. Tente de novo.")
        } catch (e: IOException) {
            // Conexão caiu no meio do caminho (comum enquanto o servidor acorda): tenta de novo.
            throw Acordando()
        } finally {
            conexao.disconnect()
        }
    }

    /** Dá um "toque" no servidor para ele ir acordando enquanto a pessoa digita. */
    suspend fun acordar(servidor: String) {
        if (servidor.isBlank()) return
        withContext(Dispatchers.IO) {
            runCatching {
                val c = (URL(enderecoBase(servidor) + "/api/saude").openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10_000
                    readTimeout = 60_000
                }
                try { c.responseCode } finally { c.disconnect() }
            }
        }
    }

    companion object {
        private const val ESPERA_MS = 4_000L

        /** Aceita o endereço do jeito que a pessoa colar: com ou sem "/" ou "/api/saude" no final. */
        fun enderecoBase(servidor: String): String =
            servidor.trim().trimEnd('/').replace(Regex("/api(/.*)?$"), "").trimEnd('/')
    }
}
