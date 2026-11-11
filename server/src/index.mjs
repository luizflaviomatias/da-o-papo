// Liga o servidor HTTP do Dá o Papo.
// Rotas:
//   GET  /api/saude      -> mostra se o servidor está de pé
//   POST /api/perguntar  -> { pergunta, modo: "curto" | "completo", tema?, historico? }
import { createServer } from 'node:http';
import { answer, createRateLimiter, HttpError } from './app.mjs';
import { loadAgent } from './skills.mjs';

const PORT = Number(process.env.PORT || 8787);
const VERSAO = '0.2.0'; // aumente quando mudar o servidor, para conferir o que está no ar
const ALLOWED_ORIGIN = process.env.ALLOWED_ORIGIN || '*';
const agent = loadAgent();
const allow = createRateLimiter();

function send(res, status, data) {
  res.writeHead(status, {
    'content-type': 'application/json; charset=utf-8',
    'access-control-allow-origin': ALLOWED_ORIGIN,
    'access-control-allow-headers': 'content-type',
    'access-control-allow-methods': 'GET,POST,OPTIONS',
  });
  res.end(JSON.stringify(data));
}

async function readJson(req, limit = 64 * 1024) {
  let size = 0;
  const chunks = [];
  for await (const chunk of req) {
    size += chunk.length;
    if (size > limit) throw new HttpError(413, 'Mensagem grande demais.');
    chunks.push(chunk);
  }
  try {
    return JSON.parse(Buffer.concat(chunks).toString('utf8') || '{}');
  } catch {
    throw new HttpError(400, 'Formato inválido.');
  }
}

const server = createServer(async (req, res) => {
  try {
    if (req.method === 'OPTIONS') return send(res, 204, {});
    const url = new URL(req.url, 'http://localhost');
    if (req.method === 'GET' && url.pathname === '/api/saude') {
      return send(res, 200, { ok: true, versao: VERSAO, skills: Object.keys(agent.skills).length, iaConfigurada: Boolean(process.env.ANTHROPIC_API_KEY) });
    }
    if (req.method === 'POST' && url.pathname === '/api/perguntar') {
      const ip = (req.headers['x-forwarded-for'] || req.socket.remoteAddress || '').toString().split(',')[0].trim();
      if (!allow(ip)) throw new HttpError(429, 'Calma! Muitas perguntas seguidas. Espere 1 minuto.');
      const body = await readJson(req);
      const result = await answer(body, { agent });
      // Não guardamos a pergunta nem a resposta: só um registro mínimo para saber se está funcionando.
      console.log(new Date().toISOString(), 'ok', result.modo, result.skills.join('+') || 'agente');
      return send(res, 200, result);
    }
    send(res, 404, { erro: 'Rota não encontrada.' });
  } catch (err) {
    const status = err instanceof HttpError ? err.status : 500;
    if (status === 500) console.error(err);
    send(res, status, { erro: err instanceof HttpError ? err.message : 'Erro inesperado. Tente de novo.', ...(err.detalhe ? { detalhe: err.detalhe } : {}) });
  }
});

server.listen(PORT, () => console.log(`Dá o Papo servidor ouvindo na porta ${PORT}`));
