// Carrega o agente e as skills (arquivos .md) e escolhe quais usar em cada pergunta.
import { readFileSync, readdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
export const AGENT_DIR = process.env.AGENT_DIR || join(here, '..', '..', 'agent');

/** Tira o cabeçalho YAML (--- ... ---) do começo do arquivo. */
export function stripFrontmatter(md) {
  return md.replace(/^---\n[\s\S]*?\n---\n?/, '').trim();
}

/** Deixa o texto minúsculo e sem acentos, para comparar palavras. */
export function normalize(text) {
  return String(text || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '');
}

/**
 * Palavras que indicam cada skill. Escritas já sem acento.
 * Prefixos (ex.: "demit") pegam variações (demitido, demitiu...).
 */
export const SKILL_KEYWORDS = {
  'advogado-do-corre': [
    'trabalh', 'patrao', 'chefe', 'firma', 'empresa', 'carteira', 'clt', 'demit', 'demiss', 'rescis',
    'ferias', '13o', 'decimo terceiro', 'fgts', 'hora extra', 'horas extras', 'justa causa', 'aviso previo',
    'seguro-desemprego', 'seguro desemprego', 'direito', 'lei ', 'advogad', 'defensoria', 'processo', 'justica',
    'preso', 'prisao', 'policia', 'delegacia', 'aluguel', 'despejo', 'inquilin', 'locador', 'consumidor', 'procon',
    'nome sujo', 'serasa', 'negativ', 'bpc', 'loas', 'inss', 'aposentad', 'bolsa familia', 'cadunico', 'cad unico',
    'pensao', 'maria da penha', 'racismo', 'injuria', ' mei ', 'cnpj', 'escritura', 'usucapiao', 'gravida', 'gestante',
    'sindicato', 'indeniza', 'multa', 'contrato', 'golpe do pix', 'cobranca',
  ],
  'economia-do-corre': [
    'dinheiro', 'juro', 'inflacao', 'preco', ' caro', 'selic', 'divida', 'cartao', 'emprestimo', 'financiamento',
    'invest', 'poupanca', 'tesouro', 'cdb', 'imposto', 'salario minimo', 'dolar', 'pib', 'desemprego', ' renda',
    'mais-valia', 'mais valia', 'capitalismo', 'economia', 'banco', 'credito', 'consignado', 'parcel', 'a vista',
    'guardar', 'reserva', 'lucro', 'negocio', 'vender', 'cooperativa',
  ],
  'matematica-do-corre': [
    'a conta', 'faz conta', 'fazer conta', 'calcul', 'porcent', '%', 'regra de tres', 'quanto fica', 'quanto da', 'quanto vou', 'area', 'metro',
    'litro', 'kwh', 'media', 'mediana', 'equacao', 'fracao', 'matematic', 'orcamento', 'preco de venda', 'parcela',
    'juros compostos', 'probabilidade', 'aposta', 'bet',
  ],
  'comunicacao-do-corre': [
    'escrev', 'carta', 'oficio', 'reclama', 'curriculo', 'e-mail', 'email', 'mensagem', 'falar em publico',
    'apresentacao', 'fake', 'noticia falsa', 'golpe', 'clonad', 'whatsapp', 'conversa dificil', 'briga', 'discuss',
    'redacao', 'texto', 'entrevista de emprego', 'reuniao', 'abaixo-assinado', 'abaixo assinado',
  ],
  'filosofia-do-corre': [
    'filosof', 'sentido da vida', 'etica', 'moral', 'certo ou errado', 'e errado', 'sartre', 'marx', 'kant',
    'socrates', 'nietzsche', 'existencia', 'liberdade', 'felicidade', 'morte', 'deus', 'fe ', 'pensar', 'freire',
    'falacia', 'argumento', 'ubuntu',
  ],
  'politica-do-corre': [
    'politic', 'governo', 'prefeit', 'vereador', 'deputad', 'senador', 'presidente', 'eleicao', 'voto', 'votar',
    'partido', 'esquerda', 'direita', 'conselho', 'ouvidoria', 'cidadania', 'democracia', 'ditadura', 'estado',
    'congresso', 'camara', 'orcamento publico', 'lei de acesso', ' lai ',
  ],
  'geografia-do-corre': [
    'bairro', 'cidade', 'periferia', 'favela', 'comunidade', 'mapa', 'calor', 'enchente', 'alaga', 'chuva',
    'moradia', 'terreno', 'escritura', 'usucapiao', 'saneamento', 'esgoto', 'lixo', 'regiao', 'bioma', 'amazonia',
    'migra', 'nordeste', 'onibus', 'transporte', 'arvore', 'area de risco', 'deslizamento',
  ],
  'geopolitica-do-corre': [
    'guerra', 'pais', 'china', 'eua', 'estados unidos', 'russia', 'ucrania', 'israel', 'gaza', 'brics', 'mercosul',
    'onu', 'dolar', 'exporta', 'importa', 'tarifa', 'mundo', 'internacional', 'otan', 'petroleo', 'gasolina', 'gas',
  ],
  'ciencia-do-corre': [
    'saude', 'doenca', 'remedio', 'vacina', 'medic', ' sus ', 'ubs', 'posto de saude', ' dor', 'febre', 'diabetes', 'pressao',
    'dengue', ' agua', 'energia', 'conta de luz', 'quimic', 'ciencia', 'aquecimento', 'clima', 'inteligencia artificial',
    ' ia ', 'tecnologia', 'celular', 'internet', 'gravidez', 'ansiedade', 'depress',
  ],
};

/** Temas que o app mostra como botões, e a skill que cada um puxa. */
export const TOPIC_TO_SKILL = {
  trabalho: 'advogado-do-corre',
  direitos: 'advogado-do-corre',
  dinheiro: 'economia-do-corre',
  contas: 'matematica-do-corre',
  escrever: 'comunicacao-do-corre',
  pensar: 'filosofia-do-corre',
  politica: 'politica-do-corre',
  cidade: 'geografia-do-corre',
  mundo: 'geopolitica-do-corre',
  saude: 'ciencia-do-corre',
};

/** Escolhe até `max` skills para a pergunta. O tema escolhido no app vem primeiro. */
export function pickSkills(question, { topic, max = 2 } = {}) {
  const text = ` ${normalize(question).replace(/[^a-z0-9%-]+/g, ' ')} `;
  const scores = [];
  for (const [skill, words] of Object.entries(SKILL_KEYWORDS)) {
    let score = 0;
    for (const w of words) if (text.includes(w)) score += w.length > 4 ? 2 : 1;
    if (score > 0) scores.push({ skill, score });
  }
  scores.sort((a, b) => b.score - a.score);
  const chosen = [];
  const forced = topic && TOPIC_TO_SKILL[normalize(topic)];
  if (forced) chosen.push(forced);
  for (const { skill } of scores) {
    if (chosen.length >= max) break;
    if (!chosen.includes(skill)) chosen.push(skill);
  }
  return chosen;
}

/** Lê todos os arquivos do agente uma vez só. */
export function loadAgent(dir = AGENT_DIR) {
  const skillsDir = join(dir, 'skills');
  const skills = {};
  for (const file of readdirSync(skillsDir)) {
    if (!file.endsWith('.md')) continue;
    skills[file.replace(/\.md$/, '')] = stripFrontmatter(readFileSync(join(skillsDir, file), 'utf8'));
  }
  const rules = readFileSync(join(dir, 'app-rules.md'), 'utf8').trim();
  if (!skills['agente-do-corre']) throw new Error('agente-do-corre.md não encontrado em ' + skillsDir);
  return { rules, skills };
}

/**
 * Monta as instruções (system prompt) em blocos.
 * O bloco fixo (regras + agente) vem primeiro para poder ser reaproveitado em cache.
 */
export function buildSystem(agent, chosenSkills) {
  const base = `${agent.rules}\n\n---\n\n# AGENTE PRINCIPAL\n\n${agent.skills['agente-do-corre']}`;
  const extra = chosenSkills
    .filter((s) => s !== 'agente-do-corre' && agent.skills[s])
    .map((s) => `# SKILL ESPECIALIZADA: ${s}\n\n${agent.skills[s]}`)
    .join('\n\n---\n\n');
  const blocks = [{ type: 'text', text: base, cache_control: { type: 'ephemeral' } }];
  if (extra) blocks.push({ type: 'text', text: extra });
  return blocks;
}
