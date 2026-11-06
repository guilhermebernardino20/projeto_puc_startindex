package br.pucpr.startindex;

import br.pucpr.startindex.servico.Estado;
import br.pucpr.startindex.servico.Persistencia;
import br.pucpr.startindex.servico.SistemaGestaoChamados;
import br.pucpr.startindex.ui.Janela;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Ponto de entrada do JavaFX. Monta o sistema (carregando os dados salvos, se houver),
 * liga a gravação automática e abre a tela de login.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Persistencia persistencia = Persistencia.padrao();
        Estado salvo = persistencia.carregar();
        SistemaGestaoChamados sistema = salvo != null
                ? SistemaGestaoChamados.restaurar(salvo)
                : new SistemaGestaoChamados();

        if (sistema.semUsuarios()) {
            // Contas de demonstração na primeira execução.
            sistema.cadastrarTecnico("Técnico Demo", "tecnico@startindex.com", "1234");
            sistema.cadastrarFuncionario("Funcionário Demo", "funcionario@startindex.com", "1234");
        }
        // Toda alteração no sistema dispara a gravação em arquivo.
        sistema.setAoAlterar(() -> {
            try {
                persistencia.salvar(sistema.exportarEstado());
            } catch (IOException e) {
                System.err.println("Não foi possível salvar os dados: " + e.getMessage());
            }
        });
        try {
            persistencia.salvar(sistema.exportarEstado());
        } catch (IOException ignorado) {
            // segue sem persistir; o erro é reportado nas próximas gravações
        }
        new Janela(stage, sistema).mostrarLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
