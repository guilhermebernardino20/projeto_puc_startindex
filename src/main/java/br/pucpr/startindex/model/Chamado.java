package br.pucpr.startindex.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Entidade principal: um chamado de suporte aberto por um funcionário.
 * É guardada nas três estruturas de dados ao mesmo tempo (os mesmos objetos, sem cópias).
 */
public class Chamado implements java.io.Serializable {

    /** Ordem de atendimento: prioridade (URGENTE primeiro), depois o mais antigo, depois o menor id. */
    // Regra de ordenação da fila de prioridade (RF-07). Prioridade vem do enum, onde
    // URGENTE é declarado primeiro, logo é "menor" e vai à frente. Id desempata por fim.
    public static final Comparator<Chamado> ORDEM_ATENDIMENTO =
            Comparator.comparing(Chamado::getPrioridade)
                    .thenComparing(Chamado::getDataCriacao)
                    .thenComparingInt(Chamado::getId);

    private final int id;
    private final String titulo;
    private final String descricao;
    private final Prioridade prioridade;
    private final Categoria categoria;
    private final LocalDateTime dataCriacao;
    private final Funcionario solicitante;
    private LocalDateTime dataFinalizacao;
    private final List<String> mensagens = new ArrayList<>();
    private StatusChamado status = StatusChamado.ABERTO;
    private TecnicoTI tecnicoResponsavel;

    public Chamado(int id, String titulo, String descricao, Categoria categoria,
                   Prioridade prioridade, Funcionario solicitante) {
        this(id, titulo, descricao, categoria, prioridade, solicitante, LocalDateTime.now());
    }

    public Chamado(int id, String titulo, String descricao, Categoria categoria,
                   Prioridade prioridade, Funcionario solicitante, LocalDateTime dataCriacao) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.prioridade = prioridade;
        this.solicitante = solicitante;
        this.dataCriacao = dataCriacao;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Prioridade getPrioridade() { return prioridade; }
    public Categoria getCategoria() { return categoria; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public Funcionario getSolicitante() { return solicitante; }
    public StatusChamado getStatus() { return status; }
    public TecnicoTI getTecnicoResponsavel() { return tecnicoResponsavel; }

    public LocalDateTime getDataFinalizacao() { return dataFinalizacao; }
    public void setDataFinalizacao(LocalDateTime d) { this.dataFinalizacao = d; }
    public List<String> getMensagens() { return List.copyOf(mensagens); }

    /** Registra uma mensagem automática de acompanhamento, visível ao solicitante. */
    public void adicionarMensagem(String texto) {
        mensagens.add("[" + DateTimeFormatter.ofPattern("dd/MM HH:mm").format(LocalDateTime.now()) + "] " + texto);
    }

    public void setStatus(StatusChamado status) { this.status = status; }
    public void setTecnicoResponsavel(TecnicoTI tecnico) { this.tecnicoResponsavel = tecnico; }

    @Override
    public String toString() {
        return "#" + id + " " + titulo;
    }
}
