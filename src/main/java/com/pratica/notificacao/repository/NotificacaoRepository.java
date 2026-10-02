package com.pratica.notificacao.repository;

import com.pratica.notificacao.domain.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacaoRepository
        extends JpaRepository<Notificacao, String>, JpaSpecificationExecutor<Notificacao> {
}
