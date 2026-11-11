package app.daopapo.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Transforma o texto da IA (markdown simples) em texto bonito na tela:
 * **negrito**, *negrito do WhatsApp*, listas com "-" e títulos com "#".
 */
object Texto {
    private val inline = Regex("""\*\*(.+?)\*\*|\*([^*\s][^*]*?)\*|`([^`]+)`""")

    fun formatar(markdown: String): AnnotatedString = buildAnnotatedString {
        val linhas = markdown.trim().lines()
        linhas.forEachIndexed { idx, bruta ->
            var linha = bruta.trimEnd()
            var negritoLinha = false
            when {
                linha.matches(Regex("^#{1,6}\\s+.*")) -> {
                    linha = linha.replace(Regex("^#{1,6}\\s+"), ""); negritoLinha = true
                }
                linha.matches(Regex("^\\s*[-*•]\\s+.*")) -> linha = "• " + linha.replace(Regex("^\\s*[-*•]\\s+"), "")
                linha.matches(Regex("^\\s*>\\s?.*")) -> linha = linha.replace(Regex("^\\s*>\\s?"), "")
                linha.matches(Regex("^\\s*-{3,}\\s*$")) -> linha = ""
            }
            if (negritoLinha) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { inlineSemMarcas(linha) }
            else inlineComNegrito(linha)
            if (idx < linhas.lastIndex) append('\n')
        }
    }

    private fun AnnotatedString.Builder.inlineComNegrito(linha: String) {
        var pos = 0
        for (m in inline.findAll(linha)) {
            append(linha.substring(pos, m.range.first))
            val conteudo = m.groupValues[1].ifEmpty { m.groupValues[2] }.ifEmpty { m.groupValues[3] }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(conteudo) }
            pos = m.range.last + 1
        }
        append(linha.substring(pos))
    }

    private fun AnnotatedString.Builder.inlineSemMarcas(linha: String) = append(paraFala(linha))

    /** Texto limpo para ler em voz alta ou copiar. */
    fun paraFala(markdown: String): String = markdown
        .replace(Regex("^#{1,6}\\s+", RegexOption.MULTILINE), "")
        .replace(Regex("^\\s*[-*•]\\s+", RegexOption.MULTILINE), "")
        .replace(Regex("^\\s*>\\s?", RegexOption.MULTILINE), "")
        .replace("**", "")
        .replace(Regex("(?<!\\w)\\*(?!\\s)|(?<!\\s)\\*(?!\\w)"), "")
        .replace("`", "")
        .trim()
}
