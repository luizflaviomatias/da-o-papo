package app.daopapo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.daopapo.data.Mensagem
import app.daopapo.data.PapoApi
import app.daopapo.data.PapoErro
import app.daopapo.data.Preferencias
import app.daopapo.data.Voz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Balao(
    val id: Long,
    val doUsuario: Boolean,
    val texto: String,
    val pergunta: String? = null, // para respostas: a pergunta que gerou (usado no "Explica mais")
    val completo: Boolean = false,
    val erro: Boolean = false,
)

data class TelaChat(
    val baloes: List<Balao> = emptyList(),
    val pensando: Boolean = false,
    val tema: String? = null,
)

/** Temas que aparecem como botões no começo da conversa. */
data class TemaRapido(val chave: String, val rotulo: String, val exemplo: String)

val TEMAS = listOf(
    TemaRapido("trabalho", "Trabalho", "Trabalho 10 horas por dia e não ganho hora extra. É normal?"),
    TemaRapido("dinheiro", "Dívida e dinheiro", "Paguei só o mínimo do cartão. Quanto vou ficar devendo?"),
    TemaRapido("direitos", "Meus direitos", "Fui mandado embora. Em quantos dias a firma tem que pagar?"),
    TemaRapido("escrever", "Escrever pra mim", "Me ajuda a escrever uma reclamação para a operadora de internet?"),
    TemaRapido("saude", "Saúde e SUS", "Onde eu pego remédio de pressão de graça?"),
    TemaRapido("cidade", "Bairro e cidade", "Tem um buraco na minha rua há meses. Como cobro a prefeitura?"),
    TemaRapido("politica", "Política", "O que faz um vereador?"),
    TemaRapido("mundo", "O mundo", "Por que o dólar alto deixa o pão mais caro?"),
    TemaRapido("pensar", "Filosofia", "Pra que serve filosofia se eu tenho boleto pra pagar?"),
    TemaRapido("contas", "Fazer conta", "Vale mais pagar à vista com 10% de desconto ou em 10x sem juros?"),
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Preferencias.de(app)
    private val api = PapoApi()
    val voz = Voz(app)

    private val _tela = MutableStateFlow(TelaChat())
    val tela: StateFlow<TelaChat> = _tela.asStateFlow()
    private var proximoId = 1L

    init {
        // Acorda o servidor assim que o app abre, para a primeira resposta vir mais rápido.
        viewModelScope.launch { api.acordar(prefs.ajustes.value.servidor) }
    }

    fun escolherTema(tema: TemaRapido?) = _tela.update { it.copy(tema = tema?.chave) }

    fun perguntar(texto: String) {
        val pergunta = texto.trim()
        if (pergunta.isEmpty() || _tela.value.pensando) return
        adicionar(Balao(proximoId++, doUsuario = true, texto = pergunta))
        responder(pergunta, completo = !prefs.ajustes.value.modoCurto)
    }

    fun explicarMais(balao: Balao) {
        val pergunta = balao.pergunta ?: return
        if (_tela.value.pensando) return
        adicionar(Balao(proximoId++, doUsuario = true, texto = "Explica mais"))
        responder(pergunta, completo = true)
    }

    fun tentarDeNovo(balao: Balao) {
        val pergunta = balao.pergunta ?: return
        _tela.update { t -> t.copy(baloes = t.baloes.filterNot { it.id == balao.id }) }
        responder(pergunta, completo = balao.completo)
    }

    fun limpar() {
        voz.parar()
        _tela.value = TelaChat()
    }

    fun ouvir(balao: Balao) {
        if (voz.falandoId.value == balao.id) voz.parar()
        else voz.falar(balao.id, Texto.paraFala(balao.texto))
    }

    private fun responder(pergunta: String, completo: Boolean) {
        val ajustes = prefs.ajustes.value
        // Histórico: só as mensagens de verdade (sem erros e sem o "Explica mais").
        // A pergunta atual vai separada, então tiramos do fim qualquer fala do usuário.
        val historico = _tela.value.baloes
            .filter { !it.erro && it.texto != "Explica mais" }
            .map { Mensagem(if (it.doUsuario) "user" else "assistant", it.texto) }
            .dropLastWhile { it.papel == "user" }
        _tela.update { it.copy(pensando = true) }
        viewModelScope.launch {
            val balao = try {
                val r = api.perguntar(ajustes.servidor, pergunta, completo, _tela.value.tema, historico)
                Balao(proximoId++, doUsuario = false, texto = r.texto, pergunta = pergunta, completo = completo)
            } catch (e: PapoErro) {
                Balao(proximoId++, false, e.message ?: "Deu erro.", pergunta, completo, erro = true)
            } catch (e: Exception) {
                Balao(proximoId++, false, "Deu um erro inesperado. Tente de novo.", pergunta, completo, erro = true)
            }
            adicionar(balao)
            _tela.update { it.copy(pensando = false) }
            if (!balao.erro && ajustes.lerEmVozAlta) voz.falar(balao.id, Texto.paraFala(balao.texto))
        }
    }

    private fun adicionar(b: Balao) = _tela.update { it.copy(baloes = it.baloes + b) }

    override fun onCleared() {
        voz.desligar()
    }
}
