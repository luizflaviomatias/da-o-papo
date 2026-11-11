package app.daopapo.domain

/** Telefones e lugares de ajuda que valem para o Brasil todo. */
data class Servico(
    val nome: String,
    val paraQue: String,
    val telefone: String? = null,
    val site: String? = null,
    val grupo: String,
)

object Servicos {
    val lista = listOf(
        // Emergência
        Servico("SAMU", "Emergência de saúde: dor no peito, desmaio, falta de ar, AVC.", "192", grupo = "Emergência"),
        Servico("Polícia Militar", "Crime acontecendo ou risco de vida.", "190", grupo = "Emergência"),
        Servico("Bombeiros", "Incêndio, resgate, acidente.", "193", grupo = "Emergência"),
        Servico("Defesa Civil", "Enchente, deslizamento, área de risco. Para receber alertas por SMS, mande o seu CEP para 40199.", "199", grupo = "Emergência"),

        // Apoio e proteção
        Servico("CVV", "Conversa e apoio emocional, 24 horas, de graça e em sigilo.", "188", "https://cvv.org.br", "Apoio e proteção"),
        Servico("Ligue 180", "Violência contra a mulher: orientação e denúncia.", "180", grupo = "Apoio e proteção"),
        Servico("Disque 100", "Direitos humanos: violência contra criança, idoso, pessoa com deficiência, racismo.", "100", grupo = "Apoio e proteção"),
        Servico("Intoxicação (CIATox)", "Engoliu produto de limpeza, remédio, veneno.", "08007226001", grupo = "Apoio e proteção"),

        // Direitos e dinheiro
        Servico("Defensoria Pública", "Advogado de graça para quem não pode pagar. Procure a Defensoria do seu estado.", site = "https://www.google.com/search?q=Defensoria+P%C3%BAblica+do+meu+estado", grupo = "Direitos e dinheiro"),
        Servico("Consumidor.gov.br", "Reclamar de empresa (internet, banco, loja) pela internet.", site = "https://www.consumidor.gov.br", grupo = "Direitos e dinheiro"),
        Servico("Alô Trabalho", "Dúvidas sobre carteira de trabalho, seguro-desemprego, abono.", "158", grupo = "Direitos e dinheiro"),
        Servico("INSS", "Aposentadoria, auxílio-doença, BPC. Também pelo app Meu INSS.", "135", "https://meu.inss.gov.br", "Direitos e dinheiro"),
        Servico("Bolsa Família e Cadastro Único", "Central do Ministério do Desenvolvimento Social. O cadastro é feito no CRAS da sua cidade.", "121", grupo = "Direitos e dinheiro"),
        Servico("Anatel", "Reclamar de operadora de celular, internet ou TV.", "1331", grupo = "Direitos e dinheiro"),

        // Saúde e cidade
        Servico("Disque Saúde (SUS)", "Informações e Ouvidoria do SUS.", "136", grupo = "Saúde e cidade"),
        Servico("Prefeitura", "Buraco na rua, iluminação, lixo, posto de saúde. Em muitas cidades o número é 156.", "156", grupo = "Saúde e cidade"),
    )

    val grupos: List<String> get() = lista.map { it.grupo }.distinct()
}
