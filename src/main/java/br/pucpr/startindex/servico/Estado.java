package br.pucpr.startindex.servico;

import br.pucpr.startindex.model.Chamado;
import br.pucpr.startindex.model.Usuario;

import java.io.Serializable;
import java.util.List;

/** Instantâneo serializável do sistema. A fila e o histórico são reconstruídos a partir dele. */
public record Estado(List<Usuario> usuarios, List<Chamado> chamados, List<Integer> idsHistorico,
                     int proximoIdChamado, int proximoIdUsuario) implements Serializable {
}
