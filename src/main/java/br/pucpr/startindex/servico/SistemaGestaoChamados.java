package br.pucpr.startindex.servico;

import br.pucpr.startindex.estrutura.FilaDePrioridade;
import br.pucpr.startindex.estrutura.Historico;
import br.pucpr.startindex.estrutura.ListaEncadeada;
import br.pucpr.startindex.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CLASSE CENTRAL DO SISTEMA (classe SistemaGestaoChamados do diagrama de classes).
 *
 * Ela concentra TODAS as regras de negócio e a interface gráfica apenas chama seus métodos.
 * Compõe as três estruturas de dados, cada uma com um papel:
 *   - listaChamados  (ListaEncadeada)   : todos os chamados, em qualquer estado
 *   - filaPrioridade (FilaDePrioridade) : só os chamados ABERTOS, na ordem de atendimento
 *   - historico      (Historico/pilha)  : os FINALIZADOS, o último finalizado no topo
 *
 * Ciclo de vida de um chamado:
 *
 * <pre>
 *   abrir --> ABERTO --aceitar--> EM_ATENDIMENTO --finalizar--> FINALIZADO
 *               |  ^                                                |
 *               |  +----------------- reabrir (pop da pilha) -------+
 *               +--rejeitar--> REJEITADO        (cancelar remove da fila e da lista)
 * </pre>
 *
 * Um chamado reaberto volta à fila com a prioridade e a data de criação originais.
 * Regras violadas lançam {@link NegocioException}, que a tela mostra ao usuário.
 */
public class SistemaGestaoChamados {

    private final ListaEncadeada listaChamados = new ListaEncadeada();
    private final FilaDePrioridade filaPrioridade = new FilaDePrioridade();
    private final Historico historico = new Historico();
    private final List<Usuario> usuarios = new ArrayList<>();
    private int proximoIdChamado = 1;
    private int proximoIdUsuario = 1;
    // Callback chamado após cada alteração; o App liga isso à gravação em arquivo.
    private Runnable aoAlterar = () -> { };

    /** Chamado após cada operação que altera dados (usado para persistir). */
    public void setAoAlterar(Runnable aoAlterar) {
        this.aoAlterar = aoAlterar == null ? () -> { } : aoAlterar;
    }

    private void alterou() {
        aoAlterar.run();
    }

    // ---------- Persistência ----------

    /** Tira uma "foto" dos dados para salvar. A fila e a pilha são recriadas a partir dela. */
    public Estado exportarEstado() {
        return new Estado(new ArrayList<>(usuarios), listaChamados.paraLista(),
                historico.baseParaTopo().stream().map(Chamado::getId).toList(),
                proximoIdChamado, proximoIdUsuario);
    }

    /** Reconstrói lista, fila e pilha a partir de um estado salvo. */
    public static SistemaGestaoChamados restaurar(Estado e) {
        SistemaGestaoChamados s = new SistemaGestaoChamados();
        s.usuarios.addAll(e.usuarios());
        s.proximoIdChamado = e.proximoIdChamado();
        s.proximoIdUsuario = e.proximoIdUsuario();
        for (Chamado c : e.chamados()) {
            s.listaChamados.inserir(c);
            // Só os ABERTOS pertencem à fila de prioridade.
            if (c.getStatus() == StatusChamado.ABERTO) {
                s.filaPrioridade.enfileirar(c);
            }
        }
        // Reempilha na ordem original (da base para o topo) para manter a ordem LIFO.
        for (int id : e.idsHistorico()) {
            Chamado c = s.listaChamados.buscar(id);
            if (c != null) {
                s.historico.push(c);
            }
        }
        return s;
    }

    public boolean semUsuarios() {
        return usuarios.isEmpty();
    }

    // ---------- Usuários (RF-01, RF-02, RNF-02) ----------

    public Funcionario cadastrarFuncionario(String nome, String email, String senha) {
        validarCadastro(nome, email, senha);
        Funcionario f = new Funcionario(proximoIdUsuario++, nome.trim(), email, senha);
        usuarios.add(f);
        alterou();
        return f;
    }

    public TecnicoTI cadastrarTecnico(String nome, String email, String senha) {
        validarCadastro(nome, email, senha);
        TecnicoTI t = new TecnicoTI(proximoIdUsuario++, nome.trim(), email, senha);
        usuarios.add(t);
        alterou();
        return t;
    }

    /** @return o usuário autenticado, ou {@code null} se as credenciais forem inválidas. */
    public Usuario autenticar(String email, String senha) {
        return usuarios.stream().filter(u -> u.login(email, senha)).findFirst().orElse(null);
    }

    /** Validações do cadastro: nome, e-mail válido e único, senha mínima. */
    private void validarCadastro(String nome, String email, String senha) {
        if (nome == null || nome.isBlank()) throw new NegocioException("Informe o nome.");
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new NegocioException("Informe um e-mail válido.");
        if (senha == null || senha.length() < 4)
            throw new NegocioException("A senha deve ter ao menos 4 caracteres.");
        String normalizado = email.trim().toLowerCase();
        if (usuarios.stream().anyMatch(u -> u.getEmail().equals(normalizado)))
            throw new NegocioException("Já existe um usuário com esse e-mail.");
    }

    // ---------- Chamados ----------

    /** RF-03: o funcionário abre um chamado. */
    public Chamado abrirChamado(Funcionario solicitante, String titulo, String descricao,
                                Categoria categoria, Prioridade prioridade) {
        if (titulo == null || titulo.isBlank()) throw new NegocioException("Informe o título do chamado.");
        if (descricao == null || descricao.isBlank()) throw new NegocioException("Descreva o problema.");
        if (categoria == null) throw new NegocioException("Escolha a categoria.");
        if (prioridade == null) throw new NegocioException("Escolha a prioridade.");
        // O id é gerado pelo sistema; a data de criação é preenchida no construtor de Chamado.
        Chamado chamado = new Chamado(proximoIdChamado++, titulo.trim(), descricao.trim(),
                categoria, prioridade, solicitante);
        registrarChamado(chamado);
        return chamado;
    }

    /** Insere na lista encadeada e na fila de prioridade (RF-06, RF-07). */
    public void registrarChamado(Chamado chamado) {
        listaChamados.inserir(chamado);      // registro central
        filaPrioridade.enfileirar(chamado);  // entra na fila, já na posição certa pela prioridade
        chamado.adicionarMensagem("Chamado recebido. Ele entrou na fila de atendimento (prioridade "
                + chamado.getPrioridade() + ") e será atendido por um técnico de TI em breve.");
        alterou();
    }

    /** O funcionário cancela um chamado seu ainda em aberto: sai da fila e da lista encadeada. */
    public void cancelarChamado(Funcionario solicitante, Chamado chamado) {
        exigirStatus(chamado, StatusChamado.ABERTO, "Só é possível cancelar chamados em aberto.");
        if (chamado.getSolicitante() != solicitante)
            throw new NegocioException("Você só pode cancelar os seus próprios chamados.");
        filaPrioridade.remover(chamado);                 // sai da fila...
        listaChamados.remover(chamado.getId());          // ...e do cadastro central
        alterou();
    }

    /** RF-04: o técnico aceita um chamado em aberto, que sai da fila. */
    public void aceitarChamado(TecnicoTI tecnico, Chamado chamado) {
        exigirStatus(chamado, StatusChamado.ABERTO, "Só é possível aceitar chamados em aberto.");
        filaPrioridade.remover(chamado);  // deixa a fila: agora tem um responsável
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        chamado.setTecnicoResponsavel(tecnico);
        chamado.adicionarMensagem("Chamado aceito pelo técnico " + tecnico.getNome() + ". Atendimento iniciado.");
        alterou();
    }

    /** BPMN: o técnico pode recusar o chamado recebido. */
    public void rejeitarChamado(TecnicoTI tecnico, Chamado chamado) {
        exigirStatus(chamado, StatusChamado.ABERTO, "Só é possível rejeitar chamados em aberto.");
        filaPrioridade.remover(chamado);
        chamado.setStatus(StatusChamado.REJEITADO);
        chamado.setTecnicoResponsavel(tecnico);
        chamado.adicionarMensagem("Chamado rejeitado pelo técnico " + tecnico.getNome()
                + ". Abra um novo chamado com mais detalhes, se necessário.");
        alterou();
    }

    /** RF-08/RF-09: finaliza o chamado e o empilha no histórico. */
    public void finalizarChamado(TecnicoTI tecnico, Chamado chamado) {
        exigirStatus(chamado, StatusChamado.EM_ATENDIMENTO, "Só é possível finalizar chamados em atendimento.");
        if (chamado.getTecnicoResponsavel() != tecnico)
            throw new NegocioException("Apenas o técnico responsável pode finalizar este chamado.");
        chamado.setStatus(StatusChamado.FINALIZADO);
        chamado.setDataFinalizacao(LocalDateTime.now());
        historico.push(chamado);  // empilha: passa a ser o topo do histórico
        chamado.adicionarMensagem("Chamado finalizado pelo técnico " + tecnico.getNome() + ".");
        alterou();
    }

    /** RF-10: reabre o último chamado finalizado (LIFO) e o devolve à fila. */
    public Chamado reabrirChamado() {
        Chamado chamado = historico.pop();  // LIFO: devolve sempre o último finalizado
        if (chamado == null) throw new NegocioException("O histórico está vazio.");
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setTecnicoResponsavel(null);
        chamado.setDataFinalizacao(null);
        filaPrioridade.enfileirar(chamado);  // prioridade e data originais definem sua nova posição
        chamado.adicionarMensagem("Chamado reaberto: o problema não foi resolvido. Voltou à fila de atendimento.");
        alterou();
        return chamado;
    }

    /** Garante que o chamado exista e esteja no status esperado, senão lança NegocioException. */
    private void exigirStatus(Chamado c, StatusChamado esperado, String mensagem) {
        if (c == null) throw new NegocioException("Selecione um chamado.");
        if (c.getStatus() != esperado) throw new NegocioException(mensagem);
    }

    // ---------- Consultas ----------

    /** Aba "Em aberto": ordem de atendimento. */
    public List<Chamado> chamadosEmAberto() {
        return filaPrioridade.emOrdem();
    }

    /** Próximo a ser atendido (topo da fila), sem removê-lo. */
    public Chamado proximoChamado() {
        return filaPrioridade.consultarProximo();
    }

    /** Chamados que este técnico aceitou e ainda não finalizou. */
    public List<Chamado> chamadosEmAtendimento(TecnicoTI tecnico) {
        return listaChamados.paraLista().stream()
                .filter(c -> c.getStatus() == StatusChamado.EM_ATENDIMENTO && c.getTecnicoResponsavel() == tecnico)
                .sorted(Chamado.ORDEM_ATENDIMENTO)
                .toList();
    }

    /** Aba "Histórico": último finalizado primeiro. */
    public List<Chamado> historico() {
        return historico.topoPrimeiro();
    }

    /** RNF-04: o funcionário só enxerga os próprios chamados. */
    public List<Chamado> chamadosDoFuncionario(Funcionario f) {
        return listaChamados.paraLista().stream()
                .filter(c -> c.getSolicitante() == f)
                .toList();
    }

    /** Indicadores para a aba de relatórios. */
    public Relatorio gerarRelatorio() {
        List<Chamado> todos = listaChamados.paraLista();
        Map<StatusChamado, Long> status = new EnumMap<>(StatusChamado.class);
        Map<Prioridade, Long> prioridade = new EnumMap<>(Prioridade.class);
        for (StatusChamado st : StatusChamado.values()) status.put(st, 0L);
        for (Prioridade p : Prioridade.values()) prioridade.put(p, 0L);
        Map<Categoria, Long> categoria = new LinkedHashMap<>();
        for (Categoria c : Categoria.padrao()) categoria.put(c, 0L);
        for (Chamado c : todos) {
            status.merge(c.getStatus(), 1L, Long::sum);
            prioridade.merge(c.getPrioridade(), 1L, Long::sum);
            categoria.merge(c.getCategoria(), 1L, Long::sum);
        }
        List<Chamado> resolvidos = todos.stream().filter(c -> c.getDataFinalizacao() != null).toList();
        Duration media = resolvidos.isEmpty() ? null : Duration.ofSeconds(Math.round(resolvidos.stream()
                .mapToLong(c -> Duration.between(c.getDataCriacao(), c.getDataFinalizacao()).toSeconds())
                .average().orElse(0)));
        return new Relatorio(todos.size(), status, categoria, prioridade, media);
    }

    public int totalChamados() {
        return listaChamados.tamanho();
    }
}
