package br.pucpr.startindex;

import br.pucpr.startindex.estrutura.FilaDePrioridade;
import br.pucpr.startindex.estrutura.ListaEncadeada;
import br.pucpr.startindex.model.*;
import br.pucpr.startindex.servico.NegocioException;
import br.pucpr.startindex.servico.SistemaGestaoChamados;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SistemaTest {

    SistemaGestaoChamados s;
    Funcionario func;
    TecnicoTI tec;
    Categoria cat = Categoria.padrao().get(0);

    @BeforeEach
    void setup() {
        s = new SistemaGestaoChamados();
        func = s.cadastrarFuncionario("Ana", "ana@x.com", "1234");
        tec = s.cadastrarTecnico("Beto", "beto@x.com", "1234");
    }

    @Test
    void filaOrdenaPorPrioridadeEDepoisPorData() {
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 10, 0);
        FilaDePrioridade f = new FilaDePrioridade();
        Chamado simples = new Chamado(1, "a", "d", cat, Prioridade.SIMPLES, func, t);
        Chamado urgenteNovo = new Chamado(2, "b", "d", cat, Prioridade.URGENTE, func, t.plusHours(2));
        Chamado urgenteAntigo = new Chamado(3, "c", "d", cat, Prioridade.URGENTE, func, t.plusHours(1));
        Chamado inter = new Chamado(4, "d", "d", cat, Prioridade.INTERMEDIARIA, func, t);
        for (Chamado c : new Chamado[]{simples, urgenteNovo, inter, urgenteAntigo}) f.enfileirar(c);

        assertEquals(urgenteAntigo, f.desenfileirar());
        assertEquals(urgenteNovo, f.desenfileirar());
        assertEquals(inter, f.desenfileirar());
        assertEquals(simples, f.desenfileirar());
        assertNull(f.desenfileirar());
    }

    @Test
    void listaEncadeadaInsereBuscaERemove() {
        ListaEncadeada l = new ListaEncadeada();
        Chamado a = new Chamado(1, "a", "d", cat, Prioridade.SIMPLES, func);
        Chamado b = new Chamado(2, "b", "d", cat, Prioridade.SIMPLES, func);
        Chamado c = new Chamado(3, "c", "d", cat, Prioridade.SIMPLES, func);
        l.inserir(a); l.inserir(b); l.inserir(c);
        assertEquals(b, l.buscar(2));
        assertTrue(l.remover(3));
        assertTrue(l.remover(1));
        assertFalse(l.remover(99));
        assertEquals(1, l.tamanho());
        l.inserir(a);
        assertEquals(java.util.List.of(b, a), l.paraLista());
    }

    @Test
    void cicloCompletoComReaberturaLifo() {
        Chamado c1 = s.abrirChamado(func, "t1", "d", cat, Prioridade.SIMPLES);
        Chamado c2 = s.abrirChamado(func, "t2", "d", cat, Prioridade.URGENTE);
        assertEquals(c2, s.proximoChamado());

        s.aceitarChamado(tec, c1);
        s.aceitarChamado(tec, c2);
        assertTrue(s.chamadosEmAberto().isEmpty());

        s.finalizarChamado(tec, c1);
        s.finalizarChamado(tec, c2);
        assertEquals(c2, s.historico().get(0));

        assertEquals(c2, s.reabrirChamado());      // último finalizado, primeiro reaberto
        assertEquals(StatusChamado.ABERTO, c2.getStatus());
        assertEquals(c2, s.proximoChamado());
        assertEquals(c1, s.reabrirChamado());
        assertThrows(NegocioException.class, s::reabrirChamado);
    }

    @Test
    void apenasOTecnicoResponsavelFinaliza() {
        TecnicoTI outro = s.cadastrarTecnico("Caio", "caio@x.com", "1234");
        Chamado c = s.abrirChamado(func, "t", "d", cat, Prioridade.SIMPLES);
        s.aceitarChamado(tec, c);
        assertThrows(NegocioException.class, () -> s.finalizarChamado(outro, c));
        assertThrows(NegocioException.class, () -> s.aceitarChamado(outro, c));
    }

    @Test
    void rejeitarRemoveDaFila() {
        Chamado c = s.abrirChamado(func, "t", "d", cat, Prioridade.SIMPLES);
        s.rejeitarChamado(tec, c);
        assertTrue(s.chamadosEmAberto().isEmpty());
        assertEquals(StatusChamado.REJEITADO, c.getStatus());
    }

    @Test
    void autenticacaoECadastro() {
        assertEquals(func, s.autenticar("ANA@x.com", "1234"));
        assertNull(s.autenticar("ana@x.com", "errada"));
        assertThrows(NegocioException.class, () -> s.cadastrarFuncionario("Outra", "ana@x.com", "1234"));
        assertThrows(NegocioException.class, () -> s.cadastrarFuncionario("", "e@x.com", "1234"));
    }

    @Test
    void funcionarioSoVeSeusChamados() {
        Funcionario outra = s.cadastrarFuncionario("Duda", "duda@x.com", "1234");
        s.abrirChamado(func, "t", "d", cat, Prioridade.SIMPLES);
        s.abrirChamado(outra, "t", "d", cat, Prioridade.SIMPLES);
        assertEquals(1, s.chamadosDoFuncionario(func).size());
    }

    @Test
    void cancelarRemoveDaFilaEDaLista() {
        Chamado c = s.abrirChamado(func, "t", "d", cat, Prioridade.SIMPLES);
        Funcionario outra = s.cadastrarFuncionario("Duda", "duda@x.com", "1234");
        assertThrows(NegocioException.class, () -> s.cancelarChamado(outra, c));
        s.cancelarChamado(func, c);
        assertTrue(s.chamadosEmAberto().isEmpty());
        assertEquals(0, s.totalChamados());
    }

    @Test
    void mensagensAutomaticasAcompanhamCicloDeVida() {
        Chamado c = s.abrirChamado(func, "t", "d", cat, Prioridade.URGENTE);
        s.aceitarChamado(tec, c);
        s.finalizarChamado(tec, c);
        s.reabrirChamado();
        assertEquals(4, c.getMensagens().size());
    }

    @Test
    void relatorioContaPorStatusEResolucao() {
        Chamado a = s.abrirChamado(func, "a", "d", cat, Prioridade.URGENTE);
        s.abrirChamado(func, "b", "d", cat, Prioridade.SIMPLES);
        s.aceitarChamado(tec, a);
        s.finalizarChamado(tec, a);
        var r = s.gerarRelatorio();
        assertEquals(2, r.total());
        assertEquals(1L, r.porStatus().get(StatusChamado.FINALIZADO));
        assertEquals(1L, r.porStatus().get(StatusChamado.ABERTO));
        assertNotNull(r.tempoMedioResolucao());
    }

    @Test
    void persistenciaRestauraFilaHistoricoEUsuarios(@org.junit.jupiter.api.io.TempDir java.nio.file.Path dir) throws Exception {
        Chamado c1 = s.abrirChamado(func, "t1", "d", cat, Prioridade.SIMPLES);
        Chamado c2 = s.abrirChamado(func, "t2", "d", cat, Prioridade.URGENTE);
        Chamado c3 = s.abrirChamado(func, "t3", "d", cat, Prioridade.INTERMEDIARIA);
        s.aceitarChamado(tec, c1);
        s.aceitarChamado(tec, c2);
        s.finalizarChamado(tec, c1);
        s.finalizarChamado(tec, c2);

        var p = new br.pucpr.startindex.servico.Persistencia(dir.resolve("d.bin"));
        p.salvar(s.exportarEstado());
        SistemaGestaoChamados r = SistemaGestaoChamados.restaurar(p.carregar());

        assertEquals(3, r.totalChamados());
        assertEquals(java.util.List.of(3), r.chamadosEmAberto().stream().map(Chamado::getId).toList());
        assertEquals(java.util.List.of(2, 1), r.historico().stream().map(Chamado::getId).toList());
        assertNotNull(r.autenticar("ana@x.com", "1234"));
        Chamado reaberto = r.reabrirChamado();
        assertEquals(2, reaberto.getId());
        assertSame(r.autenticar("ana@x.com", "1234"), reaberto.getSolicitante());
        // novos ids continuam a sequência
        assertEquals(4, r.abrirChamado((Funcionario) r.autenticar("ana@x.com", "1234"), "n", "d", cat, Prioridade.SIMPLES).getId());
    }
}
