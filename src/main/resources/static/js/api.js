import { auth } from './state.js';
import { $, el, limpar } from './dom.js';

export const API = "/notificacao";

export function mostrarMensagem(tipo, titulo, detalhes) {
  const caixa = $("mensagens");
  limpar(caixa);
  caixa.setAttribute("class", tipo);
  caixa.appendChild(el("strong", null, titulo));
  if (detalhes) {
    caixa.appendChild(el("p", null, detalhes));
  }
}

export function mostrarProblema(problema) {
  const caixa = $("mensagens");
  limpar(caixa);
  caixa.setAttribute("class", "erro");
  const titulo = (problema && problema.title) ? problema.title : "Erro na requisição";
  caixa.appendChild(el("strong", null, titulo));
  if (problema && problema.detail) {
    caixa.appendChild(el("p", null, problema.detail));
  }
  if (problema && Array.isArray(problema.errors) && problema.errors.length > 0) {
    const lista = el("ul");
    for (const erro of problema.errors) {
      const campo = erro.campo || "—";
      const mensagem = erro.mensagem || "";
      lista.appendChild(el("li", null, `${campo}: ${mensagem}`));
    }
    caixa.appendChild(lista);
  }
}

export function mostrarErroRede(erro) {
  mostrarMensagem("erro", "Falha de comunicação",
    "Não foi possível falar com a API. Verifique se o servidor está no ar. (" + erro + ")");
}

export async function enviar(caminho, opcoes = {}) {
  try {
    const headers = {
      ...opcoes.headers,
      "Authorization": `Basic ${auth.getCredenciais()}`
    };

    const resposta = await fetch(caminho, { ...opcoes, headers });
    
    if (resposta.status === 401) {
       mostrarMensagem("erro", "Não autorizado", "As credenciais são inválidas ou expiraram. Faça login novamente.");
       return { ok: false, status: 401 };
    }

    if (resposta.status === 204) {
      return { ok: true, dados: null };
    }

    let corpo = null;
    try {
      corpo = await resposta.json();
    } catch (interpretao) {
      corpo = null;
    }
    
    if (!resposta.ok) {
      if (corpo) {
        mostrarProblema(corpo);
      } else {
        mostrarMensagem("erro", `Erro ${resposta.status}`,
          "A resposta veio sem corpo problem+json.");
      }
      return { ok: false, status: resposta.status, problema: corpo };
    }
    return { ok: true, dados: corpo };
  } catch (erroDeRede) {
    mostrarErroRede(erroDeRede);
    return { ok: false };
  }
}
