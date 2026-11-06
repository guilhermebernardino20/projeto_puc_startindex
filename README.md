# STARTINDEX — Gestão de Chamados de TI

Projeto da disciplina Resolução de Problemas Estruturados em Computação (PUCPR). JavaFX 25 + Maven, Java 25.

## Executar
```
mvn javafx:run
mvn test
```
Contas de demonstração: `tecnico@startindex.com` e `funcionario@startindex.com` (senha `1234`).

## Estruturas de dados (`estrutura/`)
- `ListaEncadeada`: todos os chamados registrados.
- `FilaDePrioridade`: heap binário; chamados em aberto por prioridade e, em empate, o mais antigo.
- `Historico`: pilha LIFO dos finalizados; reabre o último finalizado primeiro.

## Decisões
- Dados apenas em memória (conforme o documento); perdem-se ao fechar.
- Chamado reaberto volta à fila com prioridade e data originais.
- Técnico pode aceitar ou rejeitar (BPMN); rejeitado fica com status "Rejeitado".
- `Date` do diagrama virou `LocalDateTime`; senhas guardadas como hash SHA-256.
# projeto_puc_startindex
