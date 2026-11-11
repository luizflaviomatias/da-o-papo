// Lógica do servidor do Dá o Papo, separada do "ligar o servidor" para poder testar.
import { loadAgent, pickSkills, buildSystem } from './skills.mjs';

export const DEFAULTS = {
  model: process.env.MODEL || 'claude-haiku-5-5',
  apiUrl: process.env.ANTHROPIC_API_URL || 'https://api.anthropic.com/v1/messages',
  maxQuestion: 2000,
  maxHistory: 8,
  rateLimitPerMinute: Number(process.env.RATE_LIMIT_PER_MINUTE || 12),
  webSearch: process.env.ENABLE_WEB_SEARCH === 'true',
};

export class HttpError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

/** Confere e limpa o que o app mandou. */
export function validateRequest(body, { maxQuestion = DEFAULTS.maxQuestion, maxHistory = DEFAULTS.maxHistory } = {}) {
  if (!body || typeof body !== 'object') throw new HttpError(400, 'Pedido vazio.');
  const pergunta = typeof body.pergunta === 'string' ? body.pergunta.trim() : '';
  if (!pergunta) throw new HttpError(400, 'Escreva ou fale a sua dúvida.');
  if (pergunta.length > maxQuestion) throw new HttpError(400, `A dúvida passou de ${maxQuestion} letras. Tente resumir.`);
  const modo = body.modo === 'completo' ? 'completo' : 'curto';
  const tema = typeof body.tema === 'string' ? body.tema.slice(0, 30) : undefined;
  const historico = Array.isArray(body.historico)
    ? body.historico
        .filter((m) => m && (m.role === 'user' || m.role === 'assistant') && typeof m.content === 'string' && m.content.trim())
        .slice(-maxHistory)
        .map((m) => ({ role: m.role, content: m.content.slice(0, 4000) }))
    : [];
  // A API exige que a conversa comece com o usuário e alterne os papéis.
  while (historico.length && historico[0].role !== 'user') historico.shift();
  const alternado = [];
  for (const m of historico) {
    if (alternado.length && alternado[alternado.length - 1].role === m.role) alternado[alternado.length - 1] = m;
    else alternado.push(m);
  }
  if (alternado.length && alternado[alternado.length - 1].role === 'user') alternado.pop();
  return { pergunta, modo, tema, historico: alternado };
}

/** Monta o pedido que vai para a IA. */
export function buildPayload(agent, { pergunta, modo, tema, historico }, opts = {}) {
  const skills = pickSkills(pergunta, { topic: tema });
  const marcador = modo === 'completo' ? '[MODO COMPLETO]' : '[MODO CURTO]';
  const payload = {
    model: opts.model || DEFAULTS.model,
    max_tokens: modo === 'completo' ? 1500 : 450,
    system: buildSystem(agent, skills),
    messages: [...historico, { role: 'user', content: `${marcador}\n${pergunta}` }],
  };
  if (opts.webSearch ?? DEFAULTS.webSearch) {
    payload.tools = [{ type: 'web_search_20250305', name: 'web_search', max_uses: 2 }];
  }
  return { payload, skills };
}

/** Junta os pedaços de texto da resposta da IA. */
export function extractText(apiResponse) {
  const parts = (apiResponse?.content || []).filter((b) => b.type === 'text').map((b) => b.text);
  return parts.join('').trim();
}

/** Limita quantas perguntas cada aparelho/IP faz por minuto. */
export function createRateLimiter(perMinute = DEFAULTS.rateLimitPerMinute, now = () => Date.now()) {
  const hits = new Map();
  return (key) => {
    const t = now();
    const recent = (hits.get(key) || []).filter((x) => t - x < 60_000);
    if (recent.length >= perMinute) {
      hits.set(key, recent);
      return false;
    }
    recent.push(t);
    hits.set(key, recent);
    if (hits.size > 10_000) hits.clear();
    return true;
  };
}

/** Responde uma pergunta: escolhe skills, chama a IA e devolve o texto. */
export async function answer(body, { agent = loadAgent(), fetchImpl = fetch, apiKey = process.env.ANTHROPIC_API_KEY, ...opts } = {}) {
  const req = validateRequest(body);
  if (!apiKey) throw new HttpError(503, 'O servidor está sem a chave da IA configurada.');
  const { payload, skills } = buildPayload(agent, req, opts);
  const res = await fetchImpl(opts.apiUrl || DEFAULTS.apiUrl, {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      'x-api-key': apiKey,
      'anthropic-version': '2023-06-01',
    },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const detail = await res.text().catch(() => '');
    console.error('Erro da IA', res.status, detail.slice(0, 500));
    if (res.status === 429 || res.status === 529) throw new HttpError(503, 'Muita gente perguntando agora. Tente de novo em 1 minuto.');
    throw new HttpError(502, 'Não consegui responder agora. Tente de novo daqui a pouco.');
  }
  const data = await res.json();
  const resposta = extractText(data);
  if (!resposta) throw new HttpError(502, 'A resposta veio vazia. Tente perguntar de outro jeito.');
  return { resposta, modo: req.modo, skills };
}
