package app.daopapo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadorasTest {

    @Test fun `le numero do jeito brasileiro`() {
        assertEquals(2000.0, Calculadoras.lerNumero("2000")!!, 0.0)
        assertEquals(2000.0, Calculadoras.lerNumero("2.000")!!, 0.0)
        assertEquals(2000.5, Calculadoras.lerNumero("2.000,50")!!, 0.0)
        assertEquals(1500.0, Calculadoras.lerNumero("R$ 1.500")!!, 0.0)
        assertEquals(12.5, Calculadoras.lerNumero("12,5%")!!, 0.0)
        assertEquals(2.5, Calculadoras.lerNumero("2.5")!!, 0.0)
        assertNull(Calculadoras.lerNumero("abc"))
        assertNull(Calculadoras.lerNumero(""))
    }

    @Test fun `hora extra com salario de 2 mil`() {
        val r = Calculadoras.horaExtra(salario = 2000.0, horasExtrasPorDia = 2.0, diasPorMes = 22.0)
        assertEquals(9.0909, r.valorHora, 0.001)
        assertEquals(13.636, r.valorHoraExtra, 0.001)
        assertEquals(44.0, r.horasNoMes, 0.0)
        assertEquals(600.0, r.totalNoMes, 0.01)
        assertEquals(7200.0, r.totalNoAno, 0.1)
    }

    @Test fun `divida do cartao cresce e respeita o teto do rotativo`() {
        val seis = Calculadoras.divida(500.0, 12.0, 6)
        assertEquals(986.91, seis.semTeto, 0.01)
        assertEquals(986.91, seis.comTetoRotativo, 0.01)
        assertEquals(289.6, seis.jurosAoAno, 0.1)
        val doze = Calculadoras.divida(500.0, 12.0, 12)
        assertTrue(doze.semTeto > 1900)
        assertEquals(1000.0, doze.comTetoRotativo, 0.0)
    }

    @Test fun `parcela pela tabela price`() {
        assertEquals(301.39, Calculadoras.parcela(3000.0, 3.0, 12), 0.01)
        assertEquals(250.0, Calculadoras.parcela(3000.0, 0.0, 12), 0.0)
    }

    @Test fun `descobre o juro escondido no parcelado`() {
        val r = Calculadoras.aVistaOuParcelado(3000.0, 301.39, 12)
        assertEquals(3616.68, r.totalParcelado, 0.01)
        assertEquals(3.0, r.taxaMensalPct, 0.01)
        assertTrue(r.compensaAVista)

        val semJuros = Calculadoras.aVistaOuParcelado(2000.0, 200.0, 10)
        assertEquals(0.0, semJuros.jurosEscondidos, 0.0)
        assertFalse(semJuros.compensaAVista)
    }

    @Test fun `formata em reais`() {
        assertEquals("R$ 2.000,00", Calculadoras.reais(2000.0))
    }
}
