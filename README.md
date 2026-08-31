# DCJAYS

Projeto Integrador em Java com foco didático em **Threads, concorrência e sincronização**.
O programa simula uma mesa de DJ usando quatro stems WAV do tema de GTA San Andreas.

## O que o projeto faz

- Cada faixa possui uma thread própria.
- As quatro faixas são carregadas antes de começar a reprodução.
- As threads usam uma `CountDownLatch` para começar juntas.
- Ao desmarcar uma faixa, o áudio **não é parado**: ele continua avançando, mas fica mudo.
- Ao marcar novamente, o som volta exatamente do ponto em que a faixa estiver naquele momento.

## Como rodar

```bash
mvn compile
mvn exec:java
```

## Estrutura

```text
src/main/java/io/github/chdevbr/dcjays/
├── Main.java
├── controller/
│   └── DJVisualController.java   -> janela e botões
├── model/
│   └── Instrumento.java          -> estado de cada faixa
├── service/
│   └── MesaDJ.java               -> cria e controla as threads
└── thread/
    └── InstrumentoPlayer.java    -> toca e silencia o áudio
```

## Como funciona a sincronização

O `InstrumentoPlayer` carrega seu `Clip` e avisa a `MesaDJ` que ficou pronto.
A `CountDownLatch` faz cada thread esperar até as quatro faixas estarem carregadas.
Somente depois disso todas chamam `clip.start()`.

A parte mais importante é a pausa: não usamos `clip.stop()` para uma pausa normal.
Em vez disso, o `Clip` continua tocando e só perde o som usando o controle de volume/mudo.
Assim, a posição de reprodução continua avançando junto com as outras faixas.

> Observação: Java Sound usa um `Clip` independente para cada faixa. Isso é adequado para
> o projeto didático e mantém os stems sincronizados na prática, mas não é uma solução
> profissional de mixagem com sincronização de áudio em nível de amostra.
