package br.pucpr.startindex.servico;

import br.pucpr.startindex.model.Categoria;
import br.pucpr.startindex.model.Prioridade;
import br.pucpr.startindex.model.StatusChamado;

import java.time.Duration;
import java.util.Map;

/** Indicadores consolidados dos chamados. {@code tempoMedioResolucao} é nulo se nada foi finalizado. */
public record Relatorio(int total,
                        Map<StatusChamado, Long> porStatus,
                        Map<Categoria, Long> porCategoria,
                        Map<Prioridade, Long> porPrioridade,
                        Duration tempoMedioResolucao) {
}
