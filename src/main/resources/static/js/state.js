export const state = {
  pagina: 0,
  tamanho: 10,
  totalPaginas: 0,
  modo: "criar",          // "criar" | "editar"
  numeroOriginal: null    // usado na URL do PUT
};

// Funções de manipulação de Autenticação na Sessão
export const auth = {
  getCredenciais() {
    return sessionStorage.getItem('auth') || btoa("admin:admin123"); // Default fallback
  },
  setCredenciais(usuario, senha) {
    sessionStorage.setItem('auth', btoa(`${usuario}:${senha}`));
  },
  logout() {
    sessionStorage.removeItem('auth');
  },
  isLogado() {
    return !!sessionStorage.getItem('auth');
  }
};
