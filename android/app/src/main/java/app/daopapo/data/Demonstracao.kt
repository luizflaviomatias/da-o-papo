package app.daopapo.data

import kotlinx.coroutines.delay
import java.text.Normalizer

/**
 * Modo demonstração: funciona sem servidor e sem internet.
 * Responde alguns exemplos prontos, escritos seguindo as skills do agente.
 */
object Demonstracao {

    private fun limpar(t: String) =
        Normalizer.normalize(t.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

    private data class Exemplo(val palavras: List<String>, val curto: String, val completo: String)

    private val exemplos = listOf(
        Exemplo(
            listOf("hora extra", "horas extras", "10 horas", "jornada"),
            curto = """
                Papo reto: passou de 8 horas por dia é **hora extra**, com no mínimo 50% a mais.
                Base: Constituição, art. 7º, XIII, e CLT, art. 59.
                Com salário de R$ 2.000, 2 horas extras por dia dão **uns R$ 600 por mês**.
                Anote o horário todo dia e procure o sindicato ou a Defensoria.
                Toque em *Explica mais* se quiser detalhes.
            """.trimIndent(),
            completo = """
                **Papo reto:** o limite é 8 horas por dia e 44 por semana. O que passar disso é hora extra, paga com pelo menos 50% a mais (CLT, art. 59).

                **Na prática:**
                - Salário de R$ 2.000 ÷ 220 horas = R$ 9,09 a hora.
                - Hora extra = R$ 9,09 + 50% = R$ 13,64.
                - 2 horas por dia × 22 dias = 44 horas = **cerca de R$ 600 por mês**.

                **Olhar crítico:** hora não paga é dinheiro que sai do seu bolso e fica no caixa da empresa. Saber fazer a conta muda a conversa.

                **Próximos passos:**
                - Anote todo dia o horário de entrada e saída.
                - Veja se existe acordo de banco de horas por escrito.
                - Procure o sindicato ou a Defensoria. Você pode cobrar os últimos 5 anos, até 2 anos depois de sair.
            """.trimIndent(),
        ),
        Exemplo(
            listOf("rescisao", "mandou embora", "demitid", "demissao", "acerto"),
            curto = """
                Papo reto: a firma tem **10 dias** depois do fim do contrato para pagar tudo.
                Base: CLT, art. 477, § 6º.
                Se atrasar, ela te deve **mais um salário de multa**.
                Guarde as mensagens e procure o sindicato ou a Defensoria.
                Toque em *Explica mais* se quiser detalhes.
            """.trimIndent(),
            completo = """
                **Papo reto:** na demissão sem justa causa, a empresa tem 10 dias para pagar o acerto e entregar os documentos (CLT, art. 477, § 6º). Atrasou? Multa de um salário para você (§ 8º).

                **O que entra no acerto:** saldo de salário, aviso prévio, férias com 1/3, 13º proporcional, saque do FGTS com multa de 40% e direito ao seguro-desemprego.

                **Na prática:** saiu dia 1º? O dinheiro tem que cair até dia 11.

                **Próximos passos:**
                - Confira cada valor no recibo (ele deve vir separado item por item).
                - Use a aba **Contas** para conferir valores.
                - Prazo para entrar na Justiça: 2 anos depois de sair.
            """.trimIndent(),
        ),
        Exemplo(
            listOf("cartao", "rotativo", "juros", "divida"),
            curto = """
                Papo reto: o rotativo do cartão tem um dos juros mais altos do país.
                Pela Lei 14.690/2023, os juros do rotativo não podem passar do valor da dívida original.
                R$ 500 a 12% ao mês viram **quase R$ 987 em 6 meses**.
                Ligue no banco e peça para parcelar a fatura, comparando o CET.
                Use a aba *Contas* para fazer a sua conta.
            """.trimIndent(),
            completo = """
                **Papo reto:** pagar só o mínimo joga o resto no rotativo, e os juros crescem sobre juros.

                **Na prática (12% ao mês):**
                - Mês 1: R$ 560
                - Mês 3: cerca de R$ 702
                - Mês 6: cerca de R$ 987

                **A lei ajuda:** a Lei 14.690/2023 limita juros e encargos do rotativo a 100% da dívida. R$ 500 não podem virar mais de R$ 1.000 só de rotativo.

                **Próximos passos:**
                - Peça ao banco para parcelar a fatura e compare o CET (custo total).
                - Se as dívidas engoliram o básico, existe a Lei do Superendividamento: procure o Procon ou a Defensoria.
            """.trimIndent(),
        ),
        Exemplo(
            listOf("golpe", "codigo", "clonad", "whatsapp", "pix"),
            curto = """
                Papo reto: **nunca passe o código de 6 números** que chega por SMS. É assim que clonam o WhatsApp.
                Ative a verificação em duas etapas: Configurações, Conta.
                Pediram Pix com número novo? Ligue para o número antigo antes.
                Caiu no golpe do Pix: avise o banco na hora e peça o MED.
                O Dá o Papo nunca pede senha, código nem Pix.
            """.trimIndent(),
            completo = """
                **Papo reto:** golpista vive de pressa e de medo. Desconfie de tudo que pede urgência.

                **Golpes comuns:**
                - Código de verificação do WhatsApp: nunca passe.
                - "Filho com número novo" pedindo Pix: ligue para o número antigo.
                - Falsa central do banco: desligue e ligue você para o número do cartão.
                - Link de entrega, multa ou prêmio: não clique.

                **Caiu no golpe:**
                - Avise o banco na hora e peça o MED (devolução do Pix).
                - Faça boletim de ocorrência.
                - Avise seus contatos.
            """.trimIndent(),
        ),
    )

    private val semServidor = """
        Este é o **modo demonstração**: o servidor da IA ainda não foi ligado no app.
        Experimente perguntar sobre **hora extra**, **rescisão**, **juros do cartão** ou **golpe no WhatsApp**.
        As abas **Contas** e **Onde ir** já funcionam de verdade, mesmo sem internet.
        Para ligar a IA, vá em **Ajustes** e coloque o endereço do servidor.
    """.trimIndent()

    suspend fun responder(pergunta: String, completo: Boolean): Resposta {
        delay(600) // dá a sensação de "pensando"
        val q = limpar(pergunta)
        val ex = exemplos.firstOrNull { e -> e.palavras.any { q.contains(it) } }
        val texto = when {
            ex == null -> semServidor
            completo -> ex.completo
            else -> ex.curto
        }
        return Resposta(texto, listOf("demonstracao"))
    }
}
