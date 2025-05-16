package giw.prac3;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
//import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;

import javax.swing.*;
import javax.swing.text.Highlighter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class Gui extends JFrame {

    private final JTextField queryField;
    private final JTextArea resultArea;
    private final JButton searchButton;

    private final IndexSearcher searcher;
    private final Analyzer analyzer;

    public Gui() throws Exception {
        super("Buscador Lucene");

        String stopwordsPath = "/home/lassy/MasterUGR/GIW/Practica3/lista_stop_words.txt";
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";

        // Configuración del índice
        CharArraySet stopwords = loadStopwords(stopwordsPath);
        this.analyzer = new StandardAnalyzer(stopwords);
        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        this.searcher = new IndexSearcher(reader);

        // Configuración GUI
        queryField = new JTextField(30);
        searchButton = new JButton("Buscar");
        resultArea = new JTextArea(20, 70);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        //searchButton.addActionListener(this::realizarBusqueda);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Consulta:"));
        inputPanel.add(queryField);
        inputPanel.add(searchButton);

        this.setLayout(new BorderLayout());
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        this.pack();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null); // centrar
        this.setVisible(true);
    }

//    private void realizarBusqueda(ActionEvent e) {
//        String input = queryField.getText().trim();
//        resultArea.setText("");
//
//        if (input.isEmpty()) {
//            resultArea.setText("Por favor, escribe una consulta.");
//            return;
//        }
//
//        try {
//            QueryParser parser = new QueryParser("contents", analyzer);
//            Query query = parser.parse(input);
//
//            TopDocs results = searcher.search(query, 10);
//            resultArea.append("Se encontraron " + results.totalHits.value()+ " documentos relevantes:\n\n");
//
//            // Preparar resaltador
//            QueryScorer scorer = new QueryScorer(query, "contents");
//            Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 100);
//            Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("***", "***"), scorer);
//            highlighter.setTextFragmenter(fragmenter);
//
//            for (ScoreDoc scoreDoc : results.scoreDocs) {
//                Document doc = searcher.doc(scoreDoc.doc);
//                String path = doc.get("path");
//                String content = doc.get("contents");
//
//                String snippet = highlighter.getBestFragment(analyzer, "contents", content);
//                if (snippet == null) {
//                    snippet = content.length() > 100 ? content.substring(0, 100) + "..." : content;
//                }
//
//                resultArea.append(String.format("-> [%.4f] %s\n", scoreDoc.score, path));
//                resultArea.append("   " + snippet + "\n\n");
//            }
//
//            guardarResultados(input, results, highlighter);
//        } catch (Exception ex) {
//            resultArea.append("Error en la búsqueda: " + ex.getMessage());
//        }
//    }
//
//    private void guardarResultados(String consulta, TopDocs results, Highlighter highlighter) throws Exception {
//        String fileName = "resultados-" + normalizeFilename(consulta) + ".txt";
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
//            writer.write("Consulta: " + consulta + "\n");
//            writer.write("Documentos encontrados: " + results.totalHits.value + "\n\n");
//
//            for (ScoreDoc scoreDoc : results.scoreDocs) {
//                Document doc = searcher.doc(scoreDoc.doc);
//                String path = doc.get("path");
//                String content = doc.get("contents");
//
//                String snippet = highlighter.getBestFragment(analyzer, "contents", content);
//                if (snippet == null) {
//                    snippet = content.length() > 100 ? content.substring(0, 100) + "..." : content;
//                }
//
//                writer.write(String.format("-> [%.4f] %s\n", scoreDoc.score, path));
//                writer.write("   " + snippet + "\n\n");
//            }
//        }
//    }
//
    private static CharArraySet loadStopwords(String filePath) throws IOException {
        List<String> stopwordList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopwordList.add(line.trim());
            }
        }
        return new CharArraySet(stopwordList, true);
    }
//
//    private static String normalizeFilename(String input) {
//        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
//        normalized = normalized.replaceAll("[^\\w\\s-]", "").replaceAll("\\s+", "_");
//        return normalized.toLowerCase();
//    }
//
    public static void main(String[] args) throws Exception {
        //if (args.length != 2) {
        //    System.err.println("Uso: java BuscadorGUI <ruta-indice> <archivo-stopwords>");
        //    System.exit(1);
        //}

        SwingUtilities.invokeLater(() -> {
            try {
                new Gui();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al iniciar el buscador: " + e.getMessage());
            }
        });
    }
}

