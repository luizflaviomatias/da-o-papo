# Dá o Papo

**Ficou na dúvida? Dá o Papo.** Um amigo que entende das coisas, no seu celular.

App de Android que responde dúvidas sobre trabalho, dívida, direitos, saúde, bairro, política e mais, em **linguagem simples**, com **exemplo** e o **próximo passo**. Pensado para a periferia e para quem tem pouca escolaridade, celular simples ou internet fraca.

🌐 **Página e documentação:** https://luizflaviomatias.github.io/da-o-papo/
📱 **Baixar o APK de teste:** https://github.com/luizflaviomatias/da-o-papo/releases/download/ultima-versao/da-o-papo.apk

## O que tem no app (MVP)

- **Perguntar** por texto ou voz, com resposta curta (até 5 linhas) ou completa, botão **Ouvir**, **Explica mais** e **Mandar** (compartilhar).
- **Contas** sem internet: hora extra, dívida com juros (com o limite da Lei 14.690/2023 no rotativo) e "à vista ou parcelado?".
- **Onde ir**: telefones de ajuda do Brasil todo (SAMU, CVV, Ligue 180, Disque 100, INSS, Anatel...).
- **Ajustes**: tema claro/escuro/igual ao celular, respostas curtas, leitura em voz alta, endereço do servidor.
- **Modo demonstração** quando não há servidor configurado.

Cores: **azul royal / ciano** (confiança) e **verde vivo** ("pode ir", solução prática).

## Como funciona

```
App Android  →  Servidor do Dá o Papo  →  IA (Claude)
                 (escolhe as skills)
```

O jeito de responder vem do agente **"Voz da Quebrada: Agente do Corre"** (método de Paulo Freire) e de 9 skills especializadas em `agent/skills/`: direito, economia, matemática, comunicação, filosofia, política, geografia, geopolítica e ciência. As regras de linguagem simples do app ficam em `agent/app-rules.md`.

## Rodar

**Servidor no ar (mais fácil):** [![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/luizflaviomatias/da-o-papo)
O Render lê o `render.yaml`, pede a `ANTHROPIC_API_KEY` e sobe o servidor no plano gratuito. Depois, coloque o endereço (`https://....onrender.com`) em **Ajustes → Servidor da IA** no app.

**Servidor no computador** (Node 20+, sem dependências):

```bash
cd server
export ANTHROPIC_API_KEY="sua-chave"
npm start   # porta 8787
npm test
```

**App Android** (Android Studio ou Gradle 8.11+ com JDK 17):

```bash
cd android
gradle testDebugUnitTest assembleDebug
```

Para já sair com o servidor configurado: `gradle assembleDebug -Pdaopapo.serverUrl=https://seu-servidor`.

A cada envio para a branch `main`, o GitHub Actions roda os testes e publica o APK em *Releases → ultima-versao*.

Mais detalhes (telas, API, configuração, próximas etapas) na [documentação](https://luizflaviomatias.github.io/da-o-papo/#documentacao).

## Aviso

O Dá o Papo orienta e encaminha, mas **não substitui** advogado, médico ou contador. O app **nunca** pede senha, código, cartão ou Pix.

Licença MIT.
