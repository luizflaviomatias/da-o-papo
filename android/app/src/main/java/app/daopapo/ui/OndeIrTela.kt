package app.daopapo.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.daopapo.R
import app.daopapo.domain.Servico
import app.daopapo.domain.Servicos

@Composable
fun OndeIrTela(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    fun abrir(intent: Intent) {
        try {
            contexto.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(contexto, "Não deu para abrir neste celular.", Toast.LENGTH_SHORT).show()
        }
    }
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Telefones de ajuda que valem no Brasil todo. Toque em Ligar: o número já aparece no seu discador. A maioria é de graça.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Servicos.grupos.forEach { grupo ->
            item {
                Text(
                    grupo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            items(Servicos.lista.filter { it.grupo == grupo }, key = { it.nome }) { s ->
                ServicoCard(
                    s,
                    ligar = { abrir(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${s.telefone}"))) },
                    abrirSite = { abrir(Intent(Intent.ACTION_VIEW, Uri.parse(s.site))) },
                )
            }
        }
    }
}

@Composable
private fun ServicoCard(s: Servico, ligar: () -> Unit, abrirSite: () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(s.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(s.paraQue, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (s.telefone != null) {
                    FilledTonalButton(onClick = ligar) {
                        Icon(painterResource(R.drawable.ic_phone), null, Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Ligar ${formatarTelefone(s.telefone)}")
                    }
                }
                if (s.site != null) {
                    OutlinedButton(onClick = abrirSite) { Text("Abrir site") }
                }
            }
        }
    }
}

private fun formatarTelefone(t: String) =
    if (t.startsWith("0800") && t.length == 11) "0800 ${t.substring(4, 7)} ${t.substring(7)}" else t
