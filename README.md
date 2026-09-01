# DCJAYS

Projeto Integrador em Java com foco didatico em **threads, concorrencia e sincronizacao**.
O programa simula uma mesa de DJ usando quatro stems WAV por musica.

## O que o projeto faz

- Cada faixa possui uma thread propria.
- As quatro faixas sao carregadas antes de comecar a reproducao.
- As threads usam uma `CountDownLatch` para comecar juntas.
- Ao silenciar uma faixa, o audio nao e parado: ele continua avancando, mas fica mudo.
- Ao retomar, o som volta do ponto em que a faixa estiver naquele momento.
- A interface principal agora e web e controla o backend Java local.

## Como rodar

Compile com Java:

```powershell
$files = Get-ChildItem -Recurse src\main\java -Filter *.java
javac -encoding UTF-8 -d target\classes $files.FullName
```

Inicie o servidor:

```powershell
java -cp target\classes io.github.chdevbr.dcjays.Main
```

Abra no navegador:

```text
http://localhost:8080
```

Se a porta `8080` estiver ocupada, o app tenta automaticamente as proximas
portas ate `8090` e mostra no terminal a URL correta para abrir.

## Interface legada Swing

O `DJVisualController` continua no projeto como fallback. Para abrir a versao Swing:

```powershell
java -cp target\classes io.github.chdevbr.dcjays.Main --swing
```

## Estrutura

```text
src/main/java/io/github/chdevbr/dcjays/
|-- Main.java
|-- controller/
|   |-- DJVisualController.java   -> interface Swing legada
|-- model/
|   |-- Instrumento.java          -> estado de cada faixa
|   |-- Musica.java               -> musica do catalogo
|-- service/
|   |-- CatalogoMusicas.java      -> lista de musicas
|   |-- MesaDJ.java               -> cria e controla as threads
|-- thread/
|   |-- InstrumentoPlayer.java    -> toca e silencia o audio
|-- web/
|   |-- ServidorWeb.java          -> servidor HTTP local
|   |-- MesaDJWebController.java  -> controle da mesa via API
|   |-- RecursoAplicacao.java     -> leitura de recursos
```

```text
src/main/resources/web/
|-- index.html
|-- styles.css
|-- app.js
```

## API local

- `GET /api/musicas`
- `POST /api/mesa/carregar`
- `GET /api/mesa/status`
- `POST /api/mesa/instrumentos/{nome}/pausar`
- `POST /api/mesa/instrumentos/{nome}/retomar`
- `POST /api/mesa/tocar-todos`
- `POST /api/mesa/pausar-todos`
- `POST /api/mesa/encerrar`

## Como funciona a sincronizacao

O `InstrumentoPlayer` carrega seu `Clip` e avisa que ficou pronto.
A `CountDownLatch` faz cada thread esperar ate as faixas estarem carregadas.
Depois disso todas entram em loop juntas.

A pausa nao usa `clip.stop()` para uma pausa normal. Em vez disso, o `Clip`
continua tocando e so perde o som usando controle de mudo ou volume. Assim, a
posicao de reproducao continua avancando junto com as outras faixas.
