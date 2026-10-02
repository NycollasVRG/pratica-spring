export function el(nome, atributos, ...filhos) {
  const no = document.createElement(nome);
  if (atributos) {
    for (const [chave, valor] of Object.entries(atributos)) {
      no.setAttribute(chave, valor);
    }
  }
  for (const filho of filhos) {
    if (filho === null || filho === undefined) continue;
    if (filho instanceof Node) {
      no.appendChild(filho);
    } else {
      no.appendChild(document.createTextNode(String(filho)));
    }
  }
  return no;
}

export function $(id) {
  return document.getElementById(id);
}

export function limpar(no) {
  no.replaceChildren();
}

export function definir(id, valor) {
  $(id).value = valor === null || valor === undefined ? "" : String(valor);
}

export function valorCampo(id) {
  const texto = $(id).value.trim();
  return texto === "" ? null : texto;
}

export function dataCampo(id) {
  const valor = $(id).value;
  return valor === "" ? null : valor;
}

export function inteiroCampo(id) {
  const valor = $(id).value;
  return valor === "" ? null : Number.parseInt(valor, 10);
}

export function enumCampo(id) {
  return valorCampo(id);
}

export function formatarData(iso) {
  if (!iso) return "—";
  const partes = String(iso).split("-");
  if (partes.length !== 3) return String(iso);
  return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

export function popular(id, opcoes, rotuloVazio) {
  const select = $(id);
  if (!select) return;
  limpar(select);
  if (rotuloVazio !== undefined) {
    select.appendChild(el("option", { value: "" }, rotuloVazio));
  }
  for (const opcao of opcoes) {
    const valor = typeof opcao === "string" ? opcao : opcao.valor;
    const rotulo = typeof opcao === "string"
      ? opcao.replaceAll("_", " ")
      : opcao.rotulo;
    select.appendChild(el("option", { value: valor }, rotulo));
  }
}
