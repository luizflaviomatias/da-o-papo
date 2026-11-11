package app.daopapo.domain

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

/** Contas do dia a dia, feitas no próprio celular (funcionam sem internet). */
object Calculadoras {

    private val brasil: Locale = Locale.forLanguageTag("pt-BR")

    fun reais(valor: Double): String = NumberFormat.getCurrencyInstance(brasil).format(valor).replace(' ', ' ')

    fun porcento(valor: Double, casas: Int = 1): String =
        String.format(brasil, "%.${casas}f%%", valor)

    /**
     * Lê número do jeito que a pessoa digita: "2000", "2.000", "2.000,50", "2000,5", "R$ 1.500".
     * Devolve null se não der para entender.
     */
    fun lerNumero(texto: String): Double? {
        var t = texto.trim().replace("R$", "").replace(" ", "").replace("%", "")
        if (t.isEmpty()) return null
        t = when {
            t.contains(',') -> t.replace(".", "").replace(',', '.')
            Regex("^\\d{1,3}(\\.\\d{3})+$").matches(t) -> t.replace(".", "")
            else -> t
        }
        return t.toDoubleOrNull()?.takeIf { it >= 0 && it.isFinite() }
    }

    // ---------- Hora extra ----------
    data class HoraExtra(
        val valorHora: Double,
        val valorHoraExtra: Double,
        val horasNoMes: Double,
        val totalNoMes: Double,
        val totalNoAno: Double,
    )

    /** CLT art. 59: hora extra vale pelo menos 50% a mais. Jornada de 44h semanais = 220 horas por mês. */
    fun horaExtra(
        salario: Double,
        horasExtrasPorDia: Double,
        diasPorMes: Double,
        adicionalPct: Double = 50.0,
        horasMensais: Double = 220.0,
    ): HoraExtra {
        val valorHora = salario / horasMensais
        val valorExtra = valorHora * (1 + adicionalPct / 100)
        val horas = horasExtrasPorDia * diasPorMes
        val total = horas * valorExtra
        return HoraExtra(valorHora, valorExtra, horas, total, total * 12)
    }

    // ---------- Dívida com juros compostos ----------
    data class Divida(
        val semTeto: Double,
        val comTetoRotativo: Double,
        val jurosAoAno: Double,
        val evolucao: List<Double>,
    )

    /**
     * Juros compostos: valor × (1 + taxa)^meses.
     * No rotativo do cartão, a Lei 14.690/2023 limita juros e encargos a 100% da dívida original.
     */
    fun divida(valor: Double, jurosMesPct: Double, meses: Int): Divida {
        val i = jurosMesPct / 100
        val evolucao = (1..meses).map { valor * (1 + i).pow(it) }
        val final = evolucao.lastOrNull() ?: valor
        return Divida(
            semTeto = final,
            comTetoRotativo = min(final, valor * 2),
            jurosAoAno = ((1 + i).pow(12) - 1) * 100,
            evolucao = evolucao,
        )
    }

    // ---------- À vista x parcelado ----------
    data class AVistaParcelado(
        val totalParcelado: Double,
        val jurosEscondidos: Double,
        val taxaMensalPct: Double,
        val compensaAVista: Boolean,
    )

    /** Valor da parcela fixa (tabela Price): PMT = PV × i ÷ [1 − (1 + i)^−n]. */
    fun parcela(valor: Double, jurosMesPct: Double, parcelas: Int): Double {
        val i = jurosMesPct / 100
        if (i == 0.0) return valor / parcelas
        return valor * i / (1 - (1 + i).pow(-parcelas))
    }

    /**
     * Descobre o juro escondido no "parcelado sem juros".
     * Supõe a 1ª parcela 30 dias depois da compra.
     */
    fun aVistaOuParcelado(precoAVista: Double, valorParcela: Double, parcelas: Int): AVistaParcelado {
        val total = valorParcela * parcelas
        val juros = total - precoAVista
        val taxa = if (juros <= 0.0) 0.0 else taxaImplicita(precoAVista, valorParcela, parcelas)
        return AVistaParcelado(total, juros, taxa, juros > 0.0)
    }

    private fun taxaImplicita(pv: Double, pmt: Double, n: Int): Double {
        var baixo = 0.0
        var alto = 1.0 // 100% ao mês
        repeat(200) {
            val meio = (baixo + alto) / 2
            val p = parcela(pv, meio * 100, n)
            if (p > pmt) alto = meio else baixo = meio
            if (abs(p - pmt) < 1e-9) return meio * 100
        }
        return (baixo + alto) / 2 * 100
    }
}
