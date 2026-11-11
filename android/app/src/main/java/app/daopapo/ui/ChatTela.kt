@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package app.daopapo.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.daopapo.R
import app.daopapo.data.Ajustes
import app.daopapo.ui.theme.Marca
import kotlinx.coroutines.delay

@Composable
fun ChatTela(vm: ChatViewModel, ajustes: Ajustes, irParaAjustes: () -> Unit, modifier: Modifier = Modifier) {
    val tela by vm.tela.collectAsState()
    val falando by vm.voz.falandoId.collectAsState()
    var texto by rememberSaveable { mutableStateOf("") }
    val lista = rememberLazyListState()
    val contexto = LocalContext.current

    LaunchedEffect(tela.baloes.size, tela.pensando) {
        val total = (if (tela.baloes.isEmpty()) 1 else 0) + tela.baloes.size + (if (tela.pensando) 1 else 0)
        if (total > 1) lista.animateScrollToItem(total - 1)
    }

    val ouvirFala = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) {
            val falado = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!falado.isNullOrBlank()) texto = if (texto.isBlank()) falado else "$texto $falado"
        }
    }

    fun enviar() {
        if (texto.isNotBlank()) {
            vm.perguntar(texto)
            texto = ""
        }
    }

    Column(modifier.fillMaxSize()) {
        LazyColumn(
            state = lista,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (tela.baloes.isEmpty()) {
                item {
                    BoasVindas(
                        modoDemonstracao = ajustes.servidor.isBlank(),
                        irParaAjustes = irParaAjustes,
                        aoEscolher = { t -> vm.escolherTema(t); texto = t.exemplo },
                    )
                }
            }
            items(tela.baloes, key = { it.id }) { b ->
                BalaoView(
                    b = b,
                    falando = falando == b.id,
                    ouvir = { if (!vm.voz.disponivel) Toast.makeText(contexto, "A voz do celular não está disponível.", Toast.LENGTH_SHORT).show() else vm.ouvir(b) },
                    explicarMais = { vm.explicarMais(b) },
                    tentarDeNovo = { vm.tentarDeNovo(b) },
                    compartilhar = {
                        val envio = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, Texto.paraFala(b.texto) + "\n\n(Resposta do app Dá o Papo)")
                        }
                        contexto.startActivity(Intent.createChooser(envio, "Compartilhar resposta"))
                    },
                )
            }
            if (tela.pensando) item { Pensando() }
        }

        val temaAtual = TEMAS.firstOrNull { it.chave == tela.tema }
        if (temaAtual != null) {
            InputChip(
                selected = true,
                onClick = { vm.escolherTema(null) },
                label = { Text("Assunto: ${temaAtual.rotulo}  ✕") },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surfaceContainer) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escreva sua dúvida…") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { enviar() }),
                )
                FilledTonalIconButton(
                    onClick = {
                        val pedido = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale a sua dúvida")
                        }
                        try {
                            ouvirFala.launch(pedido)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(contexto, "Este celular não tem reconhecimento de voz.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.size(52.dp).semantics { contentDescription = "Falar a dúvida" },
                ) { Icon(painterResource(R.drawable.ic_mic), contentDescription = null) }
                FilledIconButton(
                    onClick = { enviar() },
                    enabled = texto.isNotBlank() && !tela.pensando,
                    modifier = Modifier.size(52.dp).semantics { contentDescription = "Enviar" },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Marca.Verde, contentColor = Color(0xFF00391A)),
                ) { Icon(painterResource(R.drawable.ic_send), contentDescription = null) }
            }
        }
    }
}

@Composable
private fun BoasVindas(modoDemonstracao: Boolean, irParaAjustes: () -> Unit, aoEscolher: (TemaRapido) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .then(Modifier),
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Marca.Royal, Marca.Ciano)))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("E aí! Qual é a sua dúvida?", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(
                        "Pergunte do seu jeito, escrevendo ou falando no microfone. Eu respondo simples, com exemplo e o próximo passo.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                }
            }
        }

        if (modoDemonstracao) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Modo demonstração", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        "A IA ainda não está ligada. Dá para testar alguns exemplos e usar Contas e Onde ir.",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    TextButton(onClick = irParaAjustes) { Text("Ligar a IA nos Ajustes") }
                }
            }
        }

        Text("Escolha um assunto ou escreva abaixo:", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TEMAS.forEach { t ->
                AssistChip(
                    onClick = { aoEscolher(t) },
                    label = { Text(t.rotulo) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer, labelColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    border = null,
                )
            }
        }
        Text(
            "🔒 O Dá o Papo nunca pede senha, código, cartão ou Pix.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BalaoView(
    b: Balao,
    falando: Boolean,
    ouvir: () -> Unit,
    explicarMais: () -> Unit,
    tentarDeNovo: () -> Unit,
    compartilhar: () -> Unit,
) {
    val cores = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (b.doUsuario) Alignment.End else Alignment.Start) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (b.doUsuario) 20.dp else 4.dp,
                bottomEnd = if (b.doUsuario) 4.dp else 20.dp,
            ),
            color = when {
                b.doUsuario -> cores.primary
                b.erro -> cores.errorContainer
                else -> cores.surfaceContainerHigh
            },
            contentColor = when {
                b.doUsuario -> cores.onPrimary
                b.erro -> cores.onErrorContainer
                else -> cores.onSurface
            },
            modifier = Modifier.widthIn(max = 520.dp),
        ) {
            Text(
                if (b.doUsuario) androidx.compose.ui.text.AnnotatedString(b.texto) else Texto.formatar(b.texto),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (!b.doUsuario) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                if (b.erro) {
                    TextButton(onClick = tentarDeNovo) { Text("Tentar de novo") }
                } else {
                    TextButton(onClick = ouvir) {
                        Icon(painterResource(if (falando) R.drawable.ic_stop else R.drawable.ic_volume), null, Modifier.size(20.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(if (falando) "Parar" else "Ouvir")
                    }
                    if (!b.completo) {
                        TextButton(onClick = explicarMais) { Text("Explica mais", color = cores.tertiary) }
                    }
                    TextButton(onClick = compartilhar) {
                        Icon(painterResource(R.drawable.ic_share), null, Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Mandar")
                    }
                }
            }
        }
    }
}

@Composable
private fun Pensando() {
    var demorando by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(8_000)
        demorando = true
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(start = 6.dp)) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.tertiary)
        Text(
            if (demorando) "O servidor está acordando. Pode levar até 1 minuto na primeira pergunta…" else "Pensando na melhor resposta…",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(4.dp))
}
