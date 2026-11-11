@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package app.daopapo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.daopapo.R
import app.daopapo.data.Preferencias
import app.daopapo.ui.theme.Marca

enum class Aba(val rotulo: String, val icone: Int, val titulo: String) {
    PERGUNTAR("Perguntar", R.drawable.ic_chat, "Dá o Papo"),
    CONTAS("Contas", R.drawable.ic_calculate, "Fazer conta"),
    ONDE_IR("Onde ir", R.drawable.ic_place, "Onde ir"),
    AJUSTES("Ajustes", R.drawable.ic_settings, "Ajustes"),
}

@Composable
fun AppDaOPapo(prefs: Preferencias) {
    val ajustes by prefs.ajustes.collectAsState()
    var aba by rememberSaveable { mutableStateOf(Aba.PERGUNTAR) }
    val chat: ChatViewModel = viewModel()
    val tela by chat.tela.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (aba == Aba.PERGUNTAR) {
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)) { append("Dá o ") }
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.ExtraBold)) { append("Papo") }
                            },
                            style = MaterialTheme.typography.titleLarge,
                        )
                    } else {
                        Text(aba.titulo, style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    if (aba == Aba.PERGUNTAR && tela.baloes.isNotEmpty()) {
                        TextButton(onClick = { chat.limpar() }) { Text("Nova conversa") }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Aba.entries.forEach { a ->
                    NavigationBarItem(
                        selected = aba == a,
                        onClick = { aba = a },
                        icon = { Icon(painterResource(a.icone), contentDescription = null) },
                        label = { Text(a.rotulo) },
                    )
                }
            }
        },
    ) { espaco ->
        val conteudo = Modifier.padding(espaco).consumeWindowInsets(espaco)
        when (aba) {
            Aba.PERGUNTAR -> ChatTela(chat, ajustes, irParaAjustes = { aba = Aba.AJUSTES }, modifier = conteudo.imePadding())
            Aba.CONTAS -> ContasTela(conteudo.imePadding())
            Aba.ONDE_IR -> OndeIrTela(conteudo)
            Aba.AJUSTES -> AjustesTela(prefs, conteudo.imePadding())
        }
    }

    if (!ajustes.aceitouAviso) {
        AvisoInicial(aoAceitar = { prefs.atualizar { it.copy(aceitouAviso = true) } })
    }
}

@Composable
private fun AvisoInicial(aoAceitar: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Antes de começar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("💬 Eu explico as coisas de um jeito simples, com exemplo e o que fazer depois.")
                Text("⚖️ Não sou advogado nem médico. Em caso sério, eu te mostro onde ir.")
                Text("🔒 Eu nunca peço senha, código, cartão ou Pix. Se alguém pedir em meu nome, é golpe.")
                Text("🤔 Posso errar. Na dúvida, confira com quem entende.")
            }
        },
        confirmButton = {
            Button(
                onClick = aoAceitar,
                colors = ButtonDefaults.buttonColors(containerColor = Marca.Verde, contentColor = Color(0xFF00391A)),
            ) { Text("Entendi, bora!") }
        },
    )
}
