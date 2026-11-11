package app.daopapo.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
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
        val conexao = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000 // servidor gratuito pode levar ~1 min para acordar
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        try {
            conexao.outputStream.use { it.write(corpo.toString().toByteArray(Charsets.UTF_8)) }
            val codigo = conexao.responseCode
            val texto = (if (codigo in 200..299) conexao.inputStream else conexao.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(texto) }.getOrNull()
            if (codigo !in 200..299) {
                throw PapoErro(json?.optString("erro")?.takeIf { it.isNotBlank() } ?: "Não consegui responder agora (erro $codigo).")
            }
            val resposta = json?.optString("resposta").orEmpty()
            if (resposta.isBlank()) throw PapoErro("A resposta veio vazia. Tente de novo.")
            val skills = json?.optJSONArray("skills")?.let { arr -> List(arr.length()) { arr.getString(it) } } ?: emptyList()
            return Resposta(resposta, skills)
        } catch (e: PapoErro) {
            throw e
        } catch (e: SocketTimeoutException) {
            throw PapoErro("A internet está lenta e a resposta demorou demais. Tente de novo.")
        } catch (e: IOException) {
            throw PapoErro("Sem conexão com o servidor. Confira a sua internet e tente de novo.")
        } finally {
            conexao.disconnect()
        }
    }

    companion object {
        /** Aceita o endereço do jeito que a pessoa colar: com ou sem "/" ou "/api/saude" no final. */
        fun enderecoBase(servidor: String): String =
            servidor.trim().trimEnd('/').replace(Regex("/api(/.*)?$"), "").trimEnd('/')
    }
}
