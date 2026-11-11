@file:OptIn(ExperimentalMaterial3Api::class)

package app.daopapo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.daopapo.BuildConfig
import app.daopapo.data.Preferencias
import app.daopapo.ui.theme.Tema

@Composable
fun AjustesTela(prefs: Preferencias, modifier: Modifier = Modifier) {
    val ajustes by prefs.ajustes.collectAsState()
    var servidor by rememberSaveable { mutableStateOf(ajustes.servidor) }
    var salvo by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Secao("Aparência") {
            Text("Escolha as cores do app:")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Tema.SISTEMA to "Igual ao celular", Tema.CLARO to "Claro", Tema.ESCURO to "Escuro").forEach { (t, rotulo) ->
                    FilterChip(
                        selected = ajustes.tema == t,
                        onClick = { prefs.atualizar { it.copy(tema = t) } },
                        label = { Text(rotulo) },
                    )
                }
            }
        }

        Secao("Respostas") {
            Chave(
                "Respostas curtas",
                "Até 5 linhas, direto ao ponto. Você sempre pode tocar em \"Explica mais\".",
                ajustes.modoCurto,
            ) { v -> prefs.atualizar { it.copy(modoCurto = v) } }
            Chave(
                "Ler a resposta em voz alta",
                "O celular lê cada resposta assim que ela chega.",
                ajustes.lerEmVozAlta,
            ) { v -> prefs.atualizar { it.copy(lerEmVozAlta = v) } }
        }

        Secao("Servidor da IA") {
            Text(
                "É o endereço do computador que conversa com a inteligência artificial. Já vem preenchido. Deixe em branco para voltar ao endereço padrão.",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = servidor,
                onValueChange = { servidor = it; salvo = false },
                label = { Text("Endereço (ex.: https://...)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    prefs.atualizar { it.copy(servidor = servidor.trim().ifBlank { BuildConfig.DEFAULT_SERVER_URL }) }
                    servidor = prefs.ajustes.value.servidor
                    salvo = true
                }) { Text("Salvar") }
                if (salvo) Text("Salvo ✓", color = MaterialTheme.colorScheme.tertiary)
            }
        }

        Secao("Sobre o Dá o Papo") {
            Text("Um amigo que entende das coisas, no seu celular. Respostas simples sobre trabalho, dinheiro, direitos, saúde, cidade, política e mais, com exemplo e o próximo passo.")
            Text("Privacidade: o app não pede nome, CPF nem senha. A sua dúvida vai para o servidor só para criar a resposta. A conversa fica no seu celular e some quando você toca em \"Nova conversa\" ou fecha o app.")
            Text("Não substitui advogado, médico nem contador. Em caso sério, procure a Defensoria, a UBS ou o Procon (veja a aba Onde ir).")
            Text("Versão ${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = { prefs.atualizar { it.copy(aceitouAviso = false) } }) { Text("Ver o aviso de novo") }
        }
    }
}

@Composable
private fun Secao(titulo: String, conteudo: @Composable () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            conteudo()
        }
    }
}

@Composable
private fun Chave(titulo: String, descricao: String, ligado: Boolean, aoMudar: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(descricao, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = ligado, onCheckedChange = aoMudar)
    }
}
