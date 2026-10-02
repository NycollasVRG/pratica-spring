package com.pratica.notificacao.specification;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.domain.enums.TipoNotificacao;

public final class NotificacaoSpecs {

    private static final Map<String, String> ORDENACOES = Map.ofEntries(
            Map.entry("dataNotificacao", "dataNotificacao"),
            Map.entry("numeroNotificacao", "numeroNotificacao"),
            Map.entry("ufNotificacao", "ufNotificacao"),
            Map.entry("municipioNotificacao", "municipioNotificacao"),
            Map.entry("nomePaciente", "paciente.nomePaciente"),
            Map.entry("dataNascimento", "paciente.dataNascimento"));

    private NotificacaoSpecs() {}

    public static Specification<Notificacao> comFiltros(
            String uf, String municipio, TipoNotificacao tipo, Sexo sexo,
            LocalDate dataInicio, LocalDate dataFim) {

        return (raiz, consulta, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (uf != null && !uf.isBlank()) {
                predicados.add(cb.equal(raiz.get("ufNotificacao"), uf.trim().toUpperCase()));
            }
            if (municipio != null && !municipio.isBlank()) {
                predicados.add(cb.equal(
                        cb.lower(raiz.<String>get("municipioNotificacao")),
                        municipio.trim().toLowerCase()));
            }
            if (tipo != null) {
                predicados.add(cb.equal(raiz.get("tipoNotificacao"), tipo));
            }
            if (sexo != null) {
                predicados.add(cb.equal(raiz.get("paciente").get("sexo"), sexo));
            }
            if (dataInicio != null) {
                predicados.add(cb.greaterThanOrEqualTo(
                        raiz.<LocalDate>get("dataNotificacao"), dataInicio));
            }
            if (dataFim != null) {
                predicados.add(cb.lessThanOrEqualTo(
                        raiz.<LocalDate>get("dataNotificacao"), dataFim));
            }

            if (predicados.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicados.toArray(Predicate[]::new));
        };
    }

  
    public static String propriedadeDeOrdenacao(String chave) {
        return ORDENACOES.get(chave);
    }

    public static Set<String> chavesDeOrdenacao() {
        return ORDENACOES.keySet();
    }

   
    public static Specification<Notificacao> comAgravoDuplicado() {
        return (raiz, consulta, cb) -> {
            HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

            Subquery<Integer> subquery = consulta.subquery(Integer.class);
            Root<Notificacao> outra = subquery.from(Notificacao.class);

            Predicate agravoInformado = cb.and(
                    cb.isNotNull(raiz.<String>get("agravoDoenca")),
                    cb.notEqual(cb.trim(raiz.<String>get("agravoDoenca")), ""),
                    cb.isNotNull(outra.<String>get("agravoDoenca")),
                    cb.notEqual(cb.trim(outra.<String>get("agravoDoenca")), ""));

            Predicate mesmoAgravo = cb.equal(
                    cb.lower(cb.trim(raiz.<String>get("agravoDoenca"))),
                    cb.lower(cb.trim(outra.<String>get("agravoDoenca"))));

            Predicate naoPropria = cb.notEqual(
                    outra.get("numeroNotificacao"), raiz.get("numeroNotificacao"));

            Predicate dentroDaJanelaDeTresDias = cb.and(
                    cb.greaterThanOrEqualTo(
                            outra.<LocalDate>get("dataNotificacao"),
                            hcb.subtractDuration(raiz.<LocalDate>get("dataNotificacao"),
                                    Duration.ofDays(3))),
                    cb.lessThanOrEqualTo(
                            outra.<LocalDate>get("dataNotificacao"),
                            hcb.addDuration(raiz.<LocalDate>get("dataNotificacao"),
                                    Duration.ofDays(3))));

            subquery.select(cb.literal(1)).where(cb.and(
                    agravoInformado, mesmoAgravo, naoPropria, dentroDaJanelaDeTresDias));

            return cb.exists(subquery);
        };
    }

    public static Sort ordenacao(String chave, String direcao) {
        String propriedade = propriedadeDeOrdenacao(chave);
        Sort.Direction sentido = direcao.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(sentido, propriedade);
    }
}
