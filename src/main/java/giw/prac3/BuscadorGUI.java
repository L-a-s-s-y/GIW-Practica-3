package giw.prac3;

import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.Fragmenter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BuscadorGUI extends JFrame {

    private final JTextField queryField;
    private final JTextArea resultArea;
    private final JButton searchButton;
    private final JButton listarTodosButton;

    private final IndexSearcher searcher;
    private final Analyzer analyzer;
    private final DirectoryReader reader;

    private String indexPath;
    private String stopwordsPath;

    public BuscadorGUI(String the_index, String the_stopwords) throws Exception {
        super("Buscador Lucene");

        this.indexPath = the_index;
        this.stopwordsPath = the_stopwords;

        FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
        this.searcher = new IndexSearcher(DirectoryReader.open(indexDir));
        this.reader = DirectoryReader.open(indexDir);

        CharArraySet stopwords = loadStopwords(stopwordsPath);
        this.analyzer = new StandardAnalyzer(stopwords);

        queryField = new JTextField(30);
        searchButton = new JButton("Buscar");
        listarTodosButton = new JButton("Índice");
        resultArea = new JTextArea(20, 70);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        listarTodosButton.addActionListener(this::listarTodosDocumentos);
        searchButton.addActionListener(this::busquedaPorFrase);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Consulta:"));
        inputPanel.add(queryField);
        inputPanel.add(searchButton);
        inputPanel.add(listarTodosButton);

        this.setLayout(new BorderLayout());
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        this.pack();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null); // centrar
        this.setVisible(true);
    }

    private void listarTodosDocumentos(ActionEvent event){
        resultArea.setText("");
        try{
            QueryParser queryParser = new QueryParser("TITLE", new StandardAnalyzer());
            Query query = queryParser.parse("*:*");
            TopDocs todos = searcher.search(query, 2000);
            if (todos.scoreDocs != null) {
                StoredFields storedFields = searcher.storedFields();
                resultArea.append("Total de documentos: " + this.reader.maxDoc()+"\n");
                for (ScoreDoc doc : todos.scoreDocs){
                    int docidx = doc.doc;
                    Document docRetrieved = storedFields.document(docidx);
                    resultArea.append("------------------------------------------------------------------------\n");
                    resultArea.append("Title: "+docRetrieved.get("TITLE")+"\n");
                    resultArea.append("Path: "+docRetrieved.get("PATH")+"\n");
                }
                resultArea.append("------------------------------------------------------------------------\n");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Funciona igual si se usa un sólo término
     */
    private void busquedaPorFrase(ActionEvent event){

        String searchVal = queryField.getText().trim();
        resultArea.setText("");

        if (searchVal.isEmpty()) {
            resultArea.setText("Por favor, escribe una consulta.");
            return;
        }

        resultArea.append("BUSCANDO: "+searchVal);
        resultArea.append("------------------------------------------------------------------------");

        try{
            ArrayList<String> allSearchKeywords = new ArrayList<>();
            String words[] = searchVal.split(" ");
            for(int i = 0; i<words.length; i++){
                allSearchKeywords.add(words[i]);
            }

            if (allSearchKeywords != null && !allSearchKeywords.isEmpty()){
                QueryBuilder bldr = new QueryBuilder(new StandardAnalyzer());
                BooleanQuery.Builder chainQryBldr = new BooleanQuery.Builder();
            
                for (String txtToSearch : allSearchKeywords){
                    Query q1 = bldr.createPhraseQuery("TITLE", txtToSearch);
                    Query q2 = bldr.createPhraseQuery("PATH", txtToSearch);
                    Query q3 = bldr.createPhraseQuery("CONTENT", txtToSearch);

                    chainQryBldr.add(q1, Occur.SHOULD);
                    chainQryBldr.add(q2, Occur.SHOULD);
                    chainQryBldr.add(q3, Occur.SHOULD);
                }
            
                BooleanQuery finalQry = chainQryBldr.build();
                TopDocs allFound = this.searcher.search(finalQry, 20);

                if (allFound.scoreDocs != null) {

                    QueryScorer scorer = new QueryScorer(finalQry, "CONTENT");
                    Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 100);
                    Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(">", "<"), scorer);
                    highlighter.setTextFragmenter(fragmenter);
                    StoredFields storedFields = this.searcher.storedFields();
                    for (ScoreDoc doc : allFound.scoreDocs){
                        int docidx = doc.doc;
                        Document docRetrieved = storedFields.document(docidx);
                        String aux = "Score: " + doc.score +"| Title: "+docRetrieved.get("TITLE")+"\n";
                        resultArea.append("------------------------------------------------------------------------\n");
                        resultArea.append(aux);
                        aux = "Path: "+docRetrieved.get("PATH")+"\n";
                        resultArea.append(aux);
                        String content = docRetrieved.get("CONTENT");
                        String snippet = highlighter.getBestFragment(analyzer, "CONTENT", content);
                        if (snippet == null) {
                            snippet = content.length() > 100 ? content.substring(0, 100) + "..." : content;
                        }
                        resultArea.append("   " + snippet + "\n");
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

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
    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("Uso: java BuscadorGUI <ruta-indice> <archivo-stopwords>");
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                new BuscadorGUI(args[0], args[1]);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al iniciar el buscador: " + e.getMessage());
            }
        });
    }
}

