import { test } from 'node:test';
import assert from 'node:assert/strict';
import { pickSkills, loadAgent, buildSystem, normalize, stripFrontmatter } from '../src/skills.mjs';
import { validateRequest, buildPayload, extractText, createRateLimiter, answer, HttpError } from '../src/app.mjs';

const agent = loadAgent();

test('carrega o agente e as 9 skills especializadas', () => {
  const names = Object.keys(agent.skills).sort();
  assert.equal(names.length, 10);
  for (const n of ['agente-do-corre', 'advogado-do-corre', 'economia-do-corre', 'matematica-do-corre', 'comunicacao-do-corre',
    'filosofia-do-corre', 'politica-do-corre', 'geografia-do-corre', 'geopolitica-do-corre', 'ciencia-do-corre']) {
    assert.ok(names.includes(n), n);
    assert.ok(!agent.skills[n].startsWith('---'), 'frontmatter removido de ' + n);
  }
  assert.match(agent.rules, /Dá o Papo/);
});

test('normalize tira acentos', () => {
  assert.equal(normalize('Patrão DEMITIU, é?'), 'patrao demitiu, e?');
  assert.equal(stripFrontmatter('---\nname: x\n---\n# Oi'), '# Oi');
});

test('escolhe skills certas para perguntas comuns', () => {
  assert.equal(pickSkills('O patrão me demitiu e não pagou a rescisão')[0], 'advogado-do-corre');
  assert.ok(pickSkills('Quanto fica a dívida do cartão com juros?').includes('economia-do-corre'));
  assert.equal(pickSkills('Me ajuda a escrever um currículo')[0], 'comunicacao-do-corre');
  assert.equal(pickSkills('Por que meu bairro alaga quando tem chuva?')[0], 'geografia-do-corre');
  assert.equal(pickSkills('A vacina da dengue é segura?')[0], 'ciencia-do-corre');
  assert.ok(pickSkills('Por que a guerra na Ucrânia aumenta o preço do pão?').includes('geopolitica-do-corre'));
  assert.equal(pickSkills('Como funciona a eleição de vereador?')[0], 'politica-do-corre');
  assert.equal(pickSkills('É errado mentir para proteger um amigo? O que Kant diria?')[0], 'filosofia-do-corre');
});

test('não confunde palavras parecidas', () => {
  assert.ok(!pickSkills('Quanto de imposto eu pago?').includes('ciencia-do-corre')); // "imposto" não é "posto"
  assert.ok(!pickSkills('Quero aprender a meia-noite').includes('advogado-do-corre')); // "meia" não é "MEI"
  assert.deepEqual(pickSkills('oi'), []);
});

test('tema escolhido no app vem primeiro e limita a 2', () => {
  const s = pickSkills('juros do cartão e direito do consumidor e conta', { topic: 'saude' });
  assert.equal(s[0], 'ciencia-do-corre');
  assert.equal(s.length, 2);
});

test('system prompt tem regras + agente em cache e skill extra', () => {
  const blocks = buildSystem(agent, ['economia-do-corre']);
  assert.equal(blocks.length, 2);
  assert.deepEqual(blocks[0].cache_control, { type: 'ephemeral' });
  assert.match(blocks[0].text, /Regras do app Dá o Papo/);
  assert.match(blocks[0].text, /Voz da Quebrada/);
  assert.match(blocks[1].text, /Economia do Corre/);
  assert.equal(buildSystem(agent, []).length, 1);
});

test('valida pedido e arruma histórico', () => {
  assert.throws(() => validateRequest({}), HttpError);
  assert.throws(() => validateRequest({ pergunta: 'x'.repeat(3000) }), /Tente resumir/);
  const r = validateRequest({
    pergunta: '  e agora?  ',
    modo: 'qualquer',
    historico: [
      { role: 'assistant', content: 'oi' },
      { role: 'user', content: 'a' },
      { role: 'user', content: 'b' },
      { role: 'assistant', content: 'c' },
      { role: 'user', content: 'pendente' },
      { role: 'hacker', content: 'x' },
    ],
  });
  assert.equal(r.pergunta, 'e agora?');
  assert.equal(r.modo, 'curto');
  assert.deepEqual(r.historico, [{ role: 'user', content: 'b' }, { role: 'assistant', content: 'c' }]);
});

test('payload marca o modo e ajusta tamanho', () => {
  const curto = buildPayload(agent, validateRequest({ pergunta: 'hora extra' })).payload;
  assert.match(curto.messages.at(-1).content, /^\[MODO CURTO\]/);
  assert.equal(curto.max_tokens, 1200);
  assert.deepEqual(curto.thinking, { type: 'disabled' });
  assert.equal(curto.tools, undefined);
  const completo = buildPayload(agent, validateRequest({ pergunta: 'hora extra', modo: 'completo' }), { webSearch: true }).payload;
  assert.match(completo.messages.at(-1).content, /^\[MODO COMPLETO\]/);
  assert.equal(completo.max_tokens, 3000);
  assert.equal(completo.tools[0].type, 'web_search_20250305');
});

test('extrai texto mesmo com blocos de busca no meio', () => {
  assert.equal(extractText({ content: [{ type: 'text', text: 'Papo ' }, { type: 'server_tool_use' }, { type: 'text', text: 'reto.' }] }), 'Papo reto.');
});

test('limite de perguntas por minuto', () => {
  let t = 0;
  const allow = createRateLimiter(2, () => t);
  assert.ok(allow('a'));
  assert.ok(allow('a'));
  assert.ok(!allow('a'));
  assert.ok(allow('b'));
  t = 61_000;
  assert.ok(allow('a'));
});

test('answer chama a IA e devolve resposta (com fetch falso)', async () => {
  let sent;
  const fetchImpl = async (url, init) => {
    sent = { url, init: { ...init, body: JSON.parse(init.body) } };
    return { ok: true, json: async () => ({ content: [{ type: 'text', text: 'Papo reto: passou de 8h é hora extra.' }] }) };
  };
  const r = await answer({ pergunta: 'Trabalho 10 horas por dia, tenho hora extra?' }, { agent, fetchImpl, apiKey: 'teste' });
  assert.equal(r.resposta, 'Papo reto: passou de 8h é hora extra.');
  assert.equal(r.skills[0], 'advogado-do-corre');
  assert.equal(sent.init.headers['x-api-key'], 'teste');
  assert.equal(sent.init.body.model, 'claude-haiku-5-5');
  assert.equal(r.cortada, false);
});

test('avisa quando a resposta foi cortada e falha quando só vem raciocínio', async () => {
  const cortada = async () => ({ ok: true, json: async () => ({ stop_reason: 'max_tokens', content: [{ type: 'text', text: 'Papo reto: a firma tem 10 dias' }] }) });
  const r = await answer({ pergunta: 'rescisão' }, { agent, fetchImpl: cortada, apiKey: 'k' });
  assert.equal(r.cortada, true);
  const soPensou = async () => ({ ok: true, json: async () => ({ stop_reason: 'max_tokens', content: [{ type: 'thinking', thinking: '...' }] }) });
  await assert.rejects(answer({ pergunta: 'cartão' }, { agent, fetchImpl: soPensou, apiKey: 'k' }), /vazia/);
});

test('answer sem chave e com erro da IA dá mensagens simples', async () => {
  await assert.rejects(answer({ pergunta: 'oi' }, { agent, apiKey: '' }), (e) => e.status === 503);
  const fetchImpl = async () => ({ ok: false, status: 529, text: async () => 'overloaded' });
  await assert.rejects(answer({ pergunta: 'oi' }, { agent, fetchImpl, apiKey: 'k' }), /Tente de novo em 1 minuto/);
});
