package io.github.chdevbr.dcjays.controller;

import io.github.chdevbr.dcjays.model.Instrumento;
import io.github.chdevbr.dcjays.model.Musica;
import io.github.chdevbr.dcjays.service.CatalogoMusicas;
import io.github.chdevbr.dcjays.service.MesaDJ;
import io.github.chdevbr.dcjays.ui.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DJVisualController {

    private static final Color FUNDO = Color.BLACK;
    private static final Color PAINEL = Color.BLACK;
    private static final Color TEXTO = new Color(241, 241, 238);
    private static final Color VERMELHO = new Color(235, 103, 103);

    private static final Color CREME =
            new Color(236, 226, 199);

    private static final Color DOURADO =
            new Color(227, 174, 65);

    private static final Color VERDE =
            new Color(50, 84, 35);

    private static final Color LARANJA =
            new Color(230, 135, 50);

    private MesaDJ mesaDJ;

    private JComboBox<Musica> seletorMusicas;

    private JPanel painelInstrumentos;

    private final Map<String, JCheckBox> caixas = new LinkedHashMap<>();
    private final Map<String, JLabel> status = new LinkedHashMap<>();

    private JFrame janela;

    private Timer timer;

    private Image imagemLogo;

    private JLabel logo;

    private JPanel raiz;

    private final CatalogoMusicas catalogoMusicas =
            new CatalogoMusicas();

    private final List<Musica> musicas =
            catalogoMusicas.listar();

    public void iniciar() {
        SwingUtilities.invokeLater(this::criarJanela);
    }

    private JLabel criarLogo() {

        var url = getClass()
                .getClassLoader()
                .getResource("images/logo-dcjays.png");

        if (url == null) {
            return new JLabel("DCJAYS");
        }

        ImageIcon icone = new ImageIcon(url);
        imagemLogo = icone.getImage();

        Image imagemRedimensionada =
                imagemLogo.getScaledInstance(
                        190,
                        190,
                        Image.SCALE_SMOOTH
                );

        logo = new JLabel(
                new ImageIcon(imagemRedimensionada)
        );

        return logo;
    }

    private void criarJanela() {
        janela = new JFrame("DCJAYS - Grand Theft Auto Mixer");
        janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        janela.setMinimumSize(new Dimension(560, 520));
        janela.setPreferredSize(new Dimension(980, 720));
        janela.setLocationByPlatform(true);

        janela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                encerrarAplicacao();
            }
        });

        janela.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                atualizarEscalaInterface();
            }
        });

        raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBackground(DJVisualController.FUNDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        raiz.add(criarCabecalho(), BorderLayout.NORTH);

        painelInstrumentos = new JPanel(new BorderLayout());
        painelInstrumentos.setOpaque(false);

        raiz.add(painelInstrumentos, BorderLayout.CENTER);

        raiz.add(criarBotoes(), BorderLayout.SOUTH);

        janela.setContentPane(raiz);
        janela.pack();

        janela.setLocationRelativeTo(null);

        janela.setVisible(true);
        atualizarEscalaInterface();

        timer = new Timer(250, event -> atualizarStatus());
        timer.start();
    }

    private void atualizarEscalaInterface() {
        if (janela == null || raiz == null) {
            return;
        }

        int altura = janela.getHeight();
        int largura = janela.getWidth();

        int margemVertical = altura < 620 ? 12 : 20;
        int margemHorizontal = largura < 700 ? 14 : 24;

        raiz.setBorder(BorderFactory.createEmptyBorder(
                margemVertical,
                margemHorizontal,
                margemVertical,
                margemHorizontal
        ));

        if (imagemLogo != null && logo != null) {
            int tamanhoLogo = altura < 620 || largura < 700
                    ? 130
                    : altura < 760 ? 160 : 190;

            Image imagemRedimensionada = imagemLogo.getScaledInstance(
                    tamanhoLogo,
                    tamanhoLogo,
                    Image.SCALE_SMOOTH
            );

            logo.setIcon(new ImageIcon(imagemRedimensionada));
        }

        raiz.revalidate();
        raiz.repaint();
    }

    private void carregarMusica() {

        Musica musica =
            (Musica) seletorMusicas.getSelectedItem();

        if (musica == null) {
            return;
        }

        new Thread(() -> {

            try {

                if (mesaDJ != null) {
                    mesaDJ.encerrarTudo();
                }

                MesaDJ novaMesa = criarMesa(musica);

                novaMesa.iniciar();

                mesaDJ = novaMesa;

                SwingUtilities.invokeLater(
                        this::atualizarPainelInstrumentos
                );

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

            }

        }, "troca-musica").start();
    }

    private MesaDJ criarMesa(Musica musica) {

        MesaDJ novaMesa = new MesaDJ();

        String audio = musica.getPasta();

        novaMesa.adicionarInstrumento(
                "bateria",
                audio + "bateria.wav"
        );

        novaMesa.adicionarInstrumento(
                "baixo",
                audio + "baixo.wav"
        );

        novaMesa.adicionarInstrumento(
                "beat",
                audio + "outro.wav"
        );

        novaMesa.adicionarInstrumento(
                "vocal",
                audio + "vocal.wav"
        );

        return novaMesa;
    }

    private JPanel criarCabecalho() {

        JPanel painel = new JPanel(new GridBagLayout());
        painel.setOpaque(false);

        JLabel logo = criarLogo();

        JLabel subtitulo =
                new JLabel("Grand Theft Auto Music Mixer");

        subtitulo.setForeground(CREME);
        subtitulo.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        20
                )
        );

        JLabel labelMusica =
                new JLabel("Escolha uma música:");

        labelMusica.setForeground(CREME);
        labelMusica.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        16
                )
        );

        seletorMusicas =
                new JComboBox<>(musicas.toArray(new Musica[0])
        );

        seletorMusicas.setBackground(CREME);
        seletorMusicas.setForeground(Color.BLACK);
        seletorMusicas.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        16
                )
        );
        seletorMusicas.setMinimumSize(
                new Dimension(220, 40)
        );
        seletorMusicas.setPreferredSize(
                new Dimension(320, 40)
        );

        JButton carregar =
                new RoundedButton("CARREGAR FAIXA", 14);

        carregar.setBackground(DOURADO);
        carregar.setForeground(Color.BLACK);
        carregar.setFont(
                new Font(
                      Font.SANS_SERIF,
                      Font.BOLD,
                      13
                )
        );
        carregar.setMinimumSize(
                new Dimension(180, 44)
        );
        carregar.setPreferredSize(
                new Dimension(260, 44)
        );

        carregar.setFocusPainted(false);

        carregar.addActionListener(
                event -> carregarMusica()
        );

        GridBagConstraints c =
                new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 6, 0);

        painel.add(logo, c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 14, 0);

        painel.add(subtitulo, c);

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 6, 12);

        painel.add(labelMusica, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        painel.add(seletorMusicas, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 0, 0);

        painel.add(carregar, c);

        return painel;
    }

    private JPanel criarListaInstrumentos() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(PAINEL);
        painel.setBorder(BorderFactory.createLineBorder(DOURADO, 2));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(16, 18, 16, 18);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        int linha = 0;
        for (Instrumento instrumento : mesaDJ.listarInstrumentos()) {
            JCheckBox caixa = new JCheckBox(capitalizar(instrumento.getNome()));
            caixa.setSelected(!instrumento.isPausado());
            caixa.setOpaque(false);
            caixa.setForeground(TEXTO);
            caixa.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            caixa.addActionListener(event -> alternarInstrumento(instrumento, caixa.isSelected()));

            JLabel textoStatus = new JLabel();
            textoStatus.setHorizontalAlignment(SwingConstants.RIGHT);
            textoStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

            caixas.put(instrumento.getNome(), caixa);
            status.put(instrumento.getNome(), textoStatus);

            c.gridy = linha;
            c.gridx = 0;
            c.weightx = 1;
            painel.add(caixa, c);

            c.gridx = 1;
            c.weightx = 0;
            painel.add(textoStatus, c);

            linha++;
        }

        return painel;
    }

    private void atualizarPainelInstrumentos() {

        painelInstrumentos.removeAll();

        caixas.clear();
        status.clear();

        JScrollPane rolagem = new JScrollPane(criarListaInstrumentos());
        rolagem.setBorder(null);
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);
        rolagem.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rolagem.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        painelInstrumentos.add(rolagem, BorderLayout.CENTER);

        painelInstrumentos.revalidate();
        painelInstrumentos.repaint();
    }

    private JPanel criarBotoes() {
        JPanel painel = new JPanel(new GridLayout(1, 3, 8, 0));
        painel.setOpaque(false);

        Font fonteBotao =
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        15
                );

        Dimension tamanhoBotao =
                new Dimension(140, 48);


        RoundedButton tocarTodos = new RoundedButton("Tocar todos", 16);
        tocarTodos.addActionListener(event -> alterarTodas(true));

        tocarTodos.setBackground(VERDE);
        tocarTodos.setForeground(CREME);
        tocarTodos.setFocusPainted(false);
        tocarTodos.setBorderPainted(false);
        tocarTodos.setFont(fonteBotao);
        tocarTodos.setPreferredSize(tamanhoBotao);

        RoundedButton pausarTodos = new RoundedButton("Pausar todos", 16);
        pausarTodos.addActionListener(event -> alterarTodas(false));

        pausarTodos.setBackground(LARANJA);
        pausarTodos.setForeground(Color.BLACK);
        pausarTodos.setFocusPainted(false);
        pausarTodos.setBorderPainted(false);
        pausarTodos.setFont(fonteBotao);
        pausarTodos.setPreferredSize(tamanhoBotao);

        RoundedButton sair = new RoundedButton("Sair", 16);
        sair.addActionListener(event -> encerrarAplicacao());

        sair.setBackground(CREME);
        sair.setForeground(Color.BLACK);
        sair.setFocusPainted(false);
        sair.setBorderPainted(false);
        sair.setFont(fonteBotao);
        sair.setPreferredSize(tamanhoBotao);

        painel.add(tocarTodos);
        painel.add(pausarTodos);
        painel.add(sair);

        return painel;
    }

    private void alternarInstrumento(Instrumento instrumento, boolean tocar) {
        if (tocar) {
            mesaDJ.retomar(instrumento.getNome());
        } else {
            mesaDJ.pausar(instrumento.getNome());
        }

        atualizarStatus();
    }

    private void alterarTodas(boolean tocar) {

        if (mesaDJ == null) {
            return;
        }

        for (Instrumento instrumento : mesaDJ.listarInstrumentos()) {
            if (tocar) {
                mesaDJ.retomar(instrumento.getNome());
            } else {
                mesaDJ.pausar(instrumento.getNome());
            }
        }

        atualizarStatus();
    }

    private void atualizarStatus() {

        if (mesaDJ == null) {
            return;
        }

        for (Instrumento instrumento : mesaDJ.listarInstrumentos()) {
            boolean tocando = !instrumento.isPausado() && !instrumento.isEncerrado();

            JCheckBox caixa = caixas.get(instrumento.getNome());
            JLabel textoStatus = status.get(instrumento.getNome());

            caixa.setSelected(tocando);
            textoStatus.setText(tocando ? "TOCANDO" : "MUDO");
            textoStatus.setForeground(tocando ? VERDE : VERMELHO);
        }
    }

    private String capitalizar(String nome) {
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }

    private void encerrarAplicacao() {
        if (timer != null) {
            timer.stop();
        }

        new Thread(() -> {
            try {

                if (mesaDJ != null) {
                    mesaDJ.encerrarTudo();
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

            } finally {

                SwingUtilities.invokeLater(() -> {
                    janela.dispose();
                    System.exit(0);

                });
            }
        }, "encerramento").start();
    }
}
