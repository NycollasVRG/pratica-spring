import { state, auth } from './state.js';
import { API, enviar, mostrarMensagem } from './api.js';
import { 
  $, el, limpar, definir, valorCampo, dataCampo, 
  inteiroCampo, enumCampo, formatarData, popular 
} from './dom.js';

const UFS = [
  "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS",
  "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC",
  "SP", "SE", "TO"
];

const TIPOS = ["NEGATIVA", "INDIVIDUAL", "SURTO", "TRACOMA"];
const SEXOS = ["MASCULINO", "FEMININO", "IGNORADO"];
const GESTANTES = [
  "PRIMEIRO_TRIMESTRE", "SEGUNDO_TRIMESTRE", "TERCEIRO_TRIMESTRE",
  "IDADE_GESTACIONAL_IGNORADA", "NAO", "NAO_SE_APLICA", "IGNORADO"
];
const RACAS = ["BRANCA", "PRETA", "AMARELA", "PARDA", "INDIGENA"];
const ZONAS = ["URBANA", "RURAL", "PERIURBANA", "IGNORADO"];
const SIM_NAO = ["SIM", "NAO", "INDETERMINADO"];
const CRITERIOS = ["LABORATORIAL", "CLINICO_EPIDEMIOLOGICO"];
const EVOLUCOES = ["CURA", "OBITO_AGRAVO", "OBITO_OUTRAS_CAUSAS", "IGNORADO"];

const ORDENACOES = [
  { valor: "dataNotificacao", rotulo: "Data da notificação" },
  { valor: "numeroNotificacao", rotulo: "Número" },
  { valor: "ufNotificacao", rotulo: "UF da notificação" },
  { valor: "municipioNotificacao", rotulo: "Município da notificação" },
  { valor: "paciente.nomePaciente", rotulo: "Nome do paciente" },
  { valor: "paciente.dataNascimento", rotulo: "Data de nascimento" }
];

// ---------------------------------------------------------------- Auth UI
function configurarAuthUI() {
  const containerAuth = $("auth-container");
  if (!containerAuth) return;
  limpar(containerAuth);
  
  if (auth.isLogado()) {
    const btnLogout = el("button", { type: "button" }, "Sair (Logout)");
    btnLogout.addEventListener("click", () => {
      auth.logout();
      configurarAuthUI();
      carregarLista();
    });
    containerAuth.appendChild(btnLogout);
  } else {
    const btnLogin = el("button", { type: "button" }, "Login");
    btnLogin.addEventListener("click", () => {
      const usuario = prompt("Usuário:");
      const senha = prompt("Senha:");
      if (usuario && senha) {
        auth.setCredenciais(usuario, senha);
        configurarAuthUI();
        carregarLista();
      }
    });
    containerAuth.appendChild(btnLogin);
  }
}

// ---------------------------------------------------------------- listagem
function parametrosLista() {
  const params = new URLSearchParams();
  const filtroUf = valorCampo("filtroUf");
  const municipio = valorCampo("filtroMunicipio");
  const tipo = valorCampo("filtroTipo");
  const sexo = valorCampo("filtroSexo");
  const dataInicio = dataCampo("filtroDataInicio");
  const dataFim = dataCampo("filtroDataFim");

  if (filtroUf) params.set("uf", filtroUf);
  if (municipio) params.set("municipio", municipio);
  if (tipo) params.set("tipo", tipo);
  if (sexo) params.set("sexo", sexo);
  if (dataInicio) params.set("dataInicio", dataInicio);
  if (dataFim) params.set("dataFim", dataFim);
  
  // O Spring espera o booleano (ou falha ao vincular no record caso omitido)
  params.set("duplicadas", $("filtroDuplicadas").checked ? "true" : "false");

  // Ajustado para o formato padrão do Spring Data Pageable
  params.set("page", String(state.pagina));
  params.set("size", String(state.tamanho));
  
  const ordenarPor = valorCampo("ordenarPor") || "dataNotificacao";
  const direcao = valorCampo("direcao") || "desc";
  params.set("sort", `${ordenarPor},${direcao}`);
  
  return params;
}

async function carregarLista() {
  const resultado = await enviar(`${API}?${parametrosLista()}`, {
    method: "GET"
  });
  if (!resultado.ok) {
     if(resultado.status === 401) {
         renderizarTabela([]);
         $("info-pagina").textContent = "Faça login para ver as notificações.";
     }
     return;
  }

  const envelope = resultado.dados || {};
  const conteudo = Array.isArray(envelope.conteudo) ? envelope.conteudo : [];

  // página além do intervalo (filtros encolheram o resultado) → volta para 0
  if (conteudo.length === 0 && envelope.totalElementos > 0 && state.pagina > 0) {
    state.pagina = 0;
    await carregarLista();
    return;
  }

  state.totalPaginas = envelope.totalPaginas || 0;
  renderizarTabela(conteudo);
  renderizarPaginacao(envelope);
}

function renderizarTabela(itens) {
  const corpo = $("corpo-tabela");
  limpar(corpo);

  if (itens.length === 0) {
    corpo.appendChild(
      el("tr", null,
        el("td", { colspan: "8" }, "Nenhuma notificação encontrada."))
    );
    return;
  }

  for (const item of itens) {
    corpo.appendChild(montarLinha(item));
  }
}

function montarLinha(item) {
  const paciente = item.paciente || {};
  return el("tr", null,
    celula(item.numeroNotificacao),
    celula(item.agravoDoenca),
    celula(formatarData(item.dataNotificacao)),
    celula(item.ufNotificacao),
    celula(item.municipioNotificacao),
    celula(item.tipoNotificacao),
    celula(paciente.nomePaciente),
    celulaAcoes(item)
  );
}

function celula(valor) {
  return el("td", null, valor === null || valor === undefined || valor === "" ? "—" : String(valor));
}

function celulaAcoes(item) {
  const botaoEditar = el("button", { type: "button", class: "acao" }, "Editar");
  botaoEditar.addEventListener("click", () => preencherForm(item));

  const botaoExcluir = el("button", { type: "button", class: "acao perigo" }, "Excluir");
  botaoExcluir.addEventListener("click", () => excluir(item.numeroNotificacao));

  return el("td", { class: "acoes" }, botaoEditar, botaoExcluir);
}

function renderizarPaginacao(envelope) {
  const total = envelope.totalPaginas || 0;
  const totalElementos = envelope.totalElementos || 0;
  const pagina = envelope.pagina !== undefined ? envelope.pagina : state.pagina;

  $("info-pagina").textContent =
    `Página ${total === 0 ? 0 : pagina + 1} de ${total} — ${totalElementos} registro(s)`;

  $("btn-anterior").disabled = pagina <= 0;
  $("btn-proximo").disabled = pagina >= total - 1;
}

// ---------------------------------------------------------------- CRUD

async function salvar(evento) {
  evento.preventDefault();

  const payload = montarPayload();
  const emEdicao = state.modo === "editar";
  const caminho = emEdicao
    ? `${API}/${encodeURIComponent(state.numeroOriginal)}`
    : API;

  const resultado = await enviar(caminho, {
    method: emEdicao ? "PUT" : "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  if (resultado.ok) {
    mostrarMensagem("sucesso",
      emEdicao ? "Notificação atualizada." : "Notificação criada.",
      `Número: ${payload.numeroNotificacao}`);
    limparForm();
    await carregarLista();
  }
}

async function excluir(numero) {
  const confirmado = window.confirm(`Excluir a notificação ${numero}?`);
  if (!confirmado) return;

  const resultado = await enviar(`${API}/${encodeURIComponent(numero)}`, {
    method: "DELETE"
  });

  if (resultado.ok) {
    mostrarMensagem("sucesso", "Notificação excluída.", `Número: ${numero}`);
    await carregarLista();
  }
}

function montarPayload() {
  const investigacao = montarInvestigacao();

  return {
    numeroNotificacao: valorCampo("numeroNotificacao"),
    tipoNotificacao: enumCampo("tipoNotificacao"),
    agravoDoenca: valorCampo("agravoDoenca"),
    dataNotificacao: dataCampo("dataNotificacao"),
    ufNotificacao: valorCampo("ufNotificacao"),
    municipioNotificacao: valorCampo("municipioNotificacao"),
    unidadeSaudeNotificadora: valorCampo("unidadeSaudeNotificadora"),
    dataPrimeirosSintomas: dataCampo("dataPrimeirosSintomas"),
    paciente: {
      nomePaciente: valorCampo("nomePaciente"),
      dataNascimento: dataCampo("dataNascimento"),
      idade: inteiroCampo("idade"),
      sexo: enumCampo("sexo"),
      gestante: enumCampo("gestante"),
      racaCor: enumCampo("racaCor"),
      escolaridade: inteiroCampo("escolaridade"),
      numeroCartaoSus: valorCampo("numeroCartaoSus"),
      nomeMae: valorCampo("nomeMae"),
      telefone: valorCampo("telefone"),
      endereco: {
        ufResidencia: valorCampo("ufResidencia"),
        municipioResidencia: valorCampo("municipioResidencia"),
        distritoResidencia: valorCampo("distritoResidencia"),
        bairroResidencia: valorCampo("bairroResidencia"),
        logradouroResidencia: valorCampo("logradouroResidencia"),
        numeroResidencia: valorCampo("numeroResidencia"),
        complementoResidencia: valorCampo("complementoResidencia"),
        geoCampo1: valorCampo("geoCampo1"),
        geoCampo2: valorCampo("geoCampo2"),
        pontoReferenciaResidencia: valorCampo("pontoReferenciaResidencia"),
        cepResidencia: valorCampo("cepResidencia"),
        zonaResidencia: enumCampo("zonaResidencia"),
        paisResidencia: valorCampo("paisResidencia")
      }
    },
    investigacao: investigacao
  };
}

function montarInvestigacao() {
  const dataInvestigacao = dataCampo("invest-data");
  if (!dataInvestigacao) return null;

  return {
    dataInvestigacao: dataInvestigacao,
    classificacaoFinal: inteiroCampo("invest-classificacao"),
    criterioConfirmacaoDescarte: enumCampo("invest-criterio"),
    autoctoneMunicipioResidencia: enumCampo("invest-autoctone"),
    ufLocalInfeccao: valorCampo("invest-uf-local"),
    paisLocalInfeccao: valorCampo("invest-pais-local"),
    municipioLocalInfeccao: valorCampo("invest-municipio-local"),
    distritoLocalInfeccao: valorCampo("invest-distrito-local"),
    bairroLocalInfeccao: valorCampo("invest-bairro-local"),
    doencaRelacionadaTrabalho: enumCampo("invest-trabalho"),
    evolucaoCaso: enumCampo("invest-evolucao"),
    dataObito: dataCampo("invest-obito"),
    dataEncerramento: dataCampo("invest-encerramento")
  };
}

// ---------------------------------------------------------------- edição

function preencherForm(item) {
  definir("numeroNotificacao", item.numeroNotificacao);
  definir("tipoNotificacao", item.tipoNotificacao);
  definir("agravoDoenca", item.agravoDoenca);
  definir("dataNotificacao", item.dataNotificacao);
  definir("ufNotificacao", item.ufNotificacao);
  definir("municipioNotificacao", item.municipioNotificacao);
  definir("unidadeSaudeNotificadora", item.unidadeSaudeNotificadora);
  definir("dataPrimeirosSintomas", item.dataPrimeirosSintomas);

  const paciente = item.paciente || {};
  definir("nomePaciente", paciente.nomePaciente);
  definir("dataNascimento", paciente.dataNascimento);
  definir("idade", paciente.idade);
  definir("sexo", paciente.sexo);
  definir("gestante", paciente.gestante);
  definir("racaCor", paciente.racaCor);
  definir("escolaridade", paciente.escolaridade);
  definir("numeroCartaoSus", paciente.numeroCartaoSus);
  definir("nomeMae", paciente.nomeMae);
  definir("telefone", paciente.telefone);

  const endereco = paciente.endereco || {};
  definir("ufResidencia", endereco.ufResidencia);
  definir("municipioResidencia", endereco.municipioResidencia);
  definir("distritoResidencia", endereco.distritoResidencia);
  definir("bairroResidencia", endereco.bairroResidencia);
  definir("logradouroResidencia", endereco.logradouroResidencia);
  definir("numeroResidencia", endereco.numeroResidencia);
  definir("complementoResidencia", endereco.complementoResidencia);
  definir("geoCampo1", endereco.geoCampo1);
  definir("geoCampo2", endereco.geoCampo2);
  definir("pontoReferenciaResidencia", endereco.pontoReferenciaResidencia);
  definir("cepResidencia", endereco.cepResidencia);
  definir("zonaResidencia", endereco.zonaResidencia);
  definir("paisResidencia", endereco.paisResidencia);

  const investigacao = item.investigacao || null;
  if (investigacao) {
    definir("invest-data", investigacao.dataInvestigacao);
    definir("invest-classificacao", investigacao.classificacaoFinal);
    definir("invest-criterio", investigacao.criterioConfirmacaoDescarte);
    definir("invest-autoctone", investigacao.autoctoneMunicipioResidencia);
    definir("invest-uf-local", investigacao.ufLocalInfeccao);
    definir("invest-pais-local", investigacao.paisLocalInfeccao);
    definir("invest-municipio-local", investigacao.municipioLocalInfeccao);
    definir("invest-distrito-local", investigacao.distritoLocalInfeccao);
    definir("invest-bairro-local", investigacao.bairroLocalInfeccao);
    definir("invest-trabalho", investigacao.doencaRelacionadaTrabalho);
    definir("invest-evolucao", investigacao.evolucaoCaso);
    definir("invest-obito", investigacao.dataObito);
    definir("invest-encerramento", investigacao.dataEncerramento);
  } else {
    limparInvestigacao();
  }

  state.modo = "editar";
  state.numeroOriginal = item.numeroNotificacao;
  $("titulo-form").textContent = `Editar notificação nº ${item.numeroNotificacao}`;
  limpar($("mensagens"));
  $("formulario").scrollIntoView({ behavior: "smooth", block: "start" });
}

function limparInvestigacao() {
  definir("invest-data", null);
  definir("invest-classificacao", null);
  definir("invest-criterio", null);
  definir("invest-autoctone", null);
  definir("invest-uf-local", null);
  definir("invest-pais-local", null);
  definir("invest-municipio-local", null);
  definir("invest-distrito-local", null);
  definir("invest-bairro-local", null);
  definir("invest-trabalho", null);
  definir("invest-evolucao", null);
  definir("invest-obito", null);
  definir("invest-encerramento", null);
}

function limparForm() {
  $("form-notificacao").reset();
  limparInvestigacao();
  state.modo = "criar";
  state.numeroOriginal = null;
  $("titulo-form").textContent = "Nova notificação";
}

// ---------------------------------------------------------------- inicialização

function popularSelects() {
  popular("filtroUf", UFS, "Todas");
  popular("filtroTipo", TIPOS, "Todos");
  popular("filtroSexo", SEXOS, "Todos");

  popular("ufNotificacao", UFS, "Selecione…");
  popular("tipoNotificacao", TIPOS, "Selecione…");
  popular("sexo", SEXOS, "Selecione…");
  popular("gestante", GESTANTES, "—");
  popular("racaCor", RACAS, "—");
  popular("ufResidencia", UFS, "—");
  popular("zonaResidencia", ZONAS, "—");

  popular("invest-criterio", CRITERIOS, "—");
  popular("invest-autoctone", SIM_NAO, "—");
  popular("invest-uf-local", UFS, "—");
  popular("invest-trabalho", SIM_NAO, "—");
  popular("invest-evolucao", EVOLUCOES, "—");

  popular("ordenarPor", ORDENACOES);
  popular("direcao", [
    { valor: "desc", rotulo: "Decrescente" },
    { valor: "asc", rotulo: "Crescente" }
  ]);
  $("ordenarPor").value = "dataNotificacao";
  $("direcao").value = "desc";
}

function vincularEventos() {
  $("form-notificacao").addEventListener("submit", salvar);

  $("btn-nova").addEventListener("click", () => {
    limparForm();
    limpar($("mensagens"));
    $("numeroNotificacao").focus();
  });

  $("btn-limpar-form").addEventListener("click", () => {
    limparForm();
    limpar($("mensagens"));
  });

  $("form-filtros").addEventListener("submit", (evento) => {
    evento.preventDefault();
    state.pagina = 0;
    carregarLista();
  });

  $("btn-limpar-filtros").addEventListener("click", () => {
    $("form-filtros").reset();
    $("ordenarPor").value = "dataNotificacao";
    $("direcao").value = "desc";
    state.pagina = 0;
    carregarLista();
  });

  $("btn-anterior").addEventListener("click", () => {
    if (state.pagina > 0) {
      state.pagina -= 1;
      carregarLista();
    }
  });

  $("btn-proximo").addEventListener("click", () => {
    if (state.pagina < state.totalPaginas - 1) {
      state.pagina += 1;
      carregarLista();
    }
  });

  $("tamanhoPagina").addEventListener("change", () => {
    state.tamanho = Number.parseInt($("tamanhoPagina").value, 10);
    state.pagina = 0;
    carregarLista();
  });
}

// Initialization flow
popularSelects();
vincularEventos();
configurarAuthUI();
carregarLista();
