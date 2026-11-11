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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.daopapo.domain.Calculadoras
import app.daopapo.domain.Calculadoras.lerNumero
import app.daopapo.domain.Calculadoras.porcento
import app.daopapo.domain.Calculadoras.reais

/** Calculadoras que funcionam sem internet. */
@Composable
fun ContasTela(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Faça a conta aqui mesmo, sem internet. Digite os números do seu jeito: 2000, 2.000 ou 2.000,50.",
            style = MaterialTheme.typography.bodyLarge,
        )
        HoraExtraCard()
        DividaCard()
        AVistaCard()
    }
}

@Composable
private fun CartaoConta(titulo: String, explicacao: String, conteudo: @Composable () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(explicacao, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            conteudo()
        }
    }
}

@Composable
private fun Campo(rotulo: String, valor: String, onMuda: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = valor,
        onValueChange = onMuda,
        label = { Text(rotulo) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun Resultado(linhas: List<Pair<String, String>>, conclusao: String? = null, nota: String? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val cor = MaterialTheme.colorScheme.onTertiaryContainer
            linhas.forEach { (rotulo, valor) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(rotulo, color = cor, modifier = Modifier.weight(1f))
                    Text(valor, color = cor, fontWeight = FontWeight.Bold)
                }
            }
            if (conclusao != null) Text(conclusao, color = cor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
        }
    }
    if (nota != null) Text(nota, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun HoraExtraCard() {
    var salario by rememberSaveable { mutableStateOf("") }
    var horasDia by rememberSaveable { mutableStateOf("2") }
    var dias by rememberSaveable { mutableStateOf("22") }
    var adicional by rememberSaveable { mutableStateOf("50") }
    CartaoConta("Quanto vale a minha hora extra?", "Passou de 8 horas por dia, é hora extra. Ela vale pelo menos 50% a mais.") {
        Campo("Salário por mês (R$)", salario, { salario = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Campo("Horas extras por dia", horasDia, { horasDia = it }, Modifier.weight(1f))
            Campo("Dias no mês", dias, { dias = it }, Modifier.weight(1f))
        }
        Campo("Adicional (%)", adicional, { adicional = it })
        val s = lerNumero(salario)
        val h = lerNumero(horasDia)
        val d = lerNumero(dias)
        val a = lerNumero(adicional)
        if (s != null && s > 0 && h != null && d != null && a != null) {
            val r = Calculadoras.horaExtra(s, h, d, a)
            Resultado(
                listOf(
                    "Sua hora normal vale" to reais(r.valorHora),
                    "A hora extra vale" to reais(r.valorHoraExtra),
                    "Horas extras no mês" to "%.0f".format(r.horasNoMes),
                    "Você deve receber por mês" to reais(r.totalNoMes),
                    "Em um ano" to reais(r.totalNoAno),
                ),
                conclusao = "Anote o seu horário todo dia. Sem pagamento? Procure o sindicato ou a Defensoria.",
                nota = "Base: Constituição, art. 7º, XVI, e CLT, art. 59. Jornada de 44h por semana = 220 horas no mês. Sua convenção coletiva pode pagar mais.",
            )
        }
    }
}

@Composable
private fun DividaCard() {
    var valor by rememberSaveable { mutableStateOf("") }
    var juros by rememberSaveable { mutableStateOf("12") }
    var meses by rememberSaveable { mutableStateOf("6") }
    CartaoConta("Quanto a minha dívida vai crescer?", "Juros compostos: juros cobrados em cima de juros. É assim que o cartão e o cheque especial crescem.") {
        Campo("Valor da dívida hoje (R$)", valor, { valor = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Campo("Juros ao mês (%)", juros, { juros = it }, Modifier.weight(1f))
            Campo("Meses", meses, { meses = it }, Modifier.weight(1f))
        }
        val v = lerNumero(valor)
        val j = lerNumero(juros)
        val m = lerNumero(meses)?.toInt()?.coerceIn(1, 120)
        if (v != null && v > 0 && j != null && m != null) {
            val r = Calculadoras.divida(v, j, m)
            Resultado(
                listOf(
                    "Sem pagar nada, em $m meses" to reais(r.semTeto),
                    "Esses juros dão ao ano" to porcento(r.jurosAoAno),
                    "Limite da lei no rotativo do cartão" to reais(r.comTetoRotativo),
                ),
                conclusao = "Ataque primeiro a dívida de juros mais alto. Peça ao banco para parcelar e compare o CET.",
                nota = "No rotativo do cartão, a Lei 14.690/2023 limita juros e encargos a 100% da dívida original. Para outras dívidas, vale a conta sem limite.",
            )
        }
    }
}

@Composable
private fun AVistaCard() {
    var vista by rememberSaveable { mutableStateOf("") }
    var parcela by rememberSaveable { mutableStateOf("") }
    var vezes by rememberSaveable { mutableStateOf("10") }
    CartaoConta("À vista ou parcelado?", "Descubra se o \"sem juros\" tem juro escondido.") {
        Campo("Preço à vista (R$)", vista, { vista = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Campo("Valor da parcela (R$)", parcela, { parcela = it }, Modifier.weight(1f))
            Campo("Nº de parcelas", vezes, { vezes = it }, Modifier.weight(1f))
        }
        val pv = lerNumero(vista)
        val pm = lerNumero(parcela)
        val n = lerNumero(vezes)?.toInt()?.coerceIn(1, 120)
        if (pv != null && pv > 0 && pm != null && pm > 0 && n != null) {
            val r = Calculadoras.aVistaOuParcelado(pv, pm, n)
            Resultado(
                listOf(
                    "Total no parcelado" to reais(r.totalParcelado),
                    "Juros escondidos" to reais(r.jurosEscondidos.coerceAtLeast(0.0)),
                    "Taxa por mês" to porcento(r.taxaMensalPct, 2),
                ),
                conclusao = if (r.compensaAVista) {
                    "À vista sai mais barato. Mas só pague à vista se não zerar a sua reserva para emergência."
                } else {
                    "Parcelado sem juros de verdade: pode parcelar sem medo, se a parcela cabe no mês."
                },
                nota = "A conta considera a 1ª parcela 30 dias depois da compra.",
            )
        }
    }
}
