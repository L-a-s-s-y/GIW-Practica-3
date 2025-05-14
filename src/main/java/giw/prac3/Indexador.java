package giw.prac3;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Indexador {
    public static void main(String[] args) throws Exception {

        String docsPath = "/home/lassy/MasterUGR/GIW/Practica3/los-documentos/";
        String stopwordsPath = "/home/lassy/MasterUGR/GIW/Practica3/lista_stop_words.txt";
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";


        // Leer stopwords
        CharArraySet stopwords = loadStopwords(stopwordsPath);

        // Configurar el analizador con stopwords
        Analyzer analyzer = new StandardAnalyzer(stopwords);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // Crear el índice en disco
        FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
        IndexWriter writer = new IndexWriter(indexDir, config);

        // Indexar documentos
        indexDocs(writer, Paths.get(docsPath));

        writer.close();
        // Verificación de la indexación
        try (DirectoryReader reader = DirectoryReader.open(indexDir)) {
            System.out.println("Total de documentos indexados: " + reader.maxDoc());

            //IndexSearcher searcher = new IndexSearcher(reader);
            //String queryStr = "kill"; // cambia por tu palabra clave
            //QueryParser parser = new QueryParser("contents", analyzer); // usamos el mismo Analyzer
            //Query query = parser.parse(queryStr);
            //TopDocs hits = searcher.search(query, 10);
            //System.out.println("Documentos encontrados para \"" + queryStr + "\": " + hits.totalHits.value);
            //for (ScoreDoc scoreDoc : hits.scoreDocs) {
            //    Document doc = searcher.doc(scoreDoc.doc);
            //    System.out.println(" -> " + doc.get("path"));
            //}
        }
        System.out.println("Indexación completa.");
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

    private static void indexDocs(IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walk(path)
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.toString().endsWith(".srt") || p.toString().endsWith(".txt"))
                .forEach(p -> {
                    try {
                        indexDoc(writer, p);
                    } catch (IOException e) {
                        System.err.println("Error indexando " + p + ": " + e.getMessage());
                    }
                });
        } else {
            indexDoc(writer, path);
        }
    }

    private static void indexDoc(IndexWriter writer, Path file) throws IOException {
        try {
            String content = readFileWithFallbackEncodings(file);

            if (content.trim().isEmpty()) {
                return; // no indexar vacíos
            }

            Document doc = new Document();
            doc.add(new StringField("path", file.toString(), Field.Store.YES));
            doc.add(new TextField("contents", content, Field.Store.YES));
            writer.addDocument(doc);
        } catch (Exception e) {
            System.err.println("Error indexando " + file + ": " + e.getMessage());
        }
    }

    private static String readFileWithFallbackEncodings(Path file) throws IOException {
        List<Charset> encodingsToTry = List.of(
            StandardCharsets.UTF_8,
            Charset.forName("windows-1252"),
            StandardCharsets.ISO_8859_1
        );

        for (Charset charset : encodingsToTry) {
            try {
                return Files.readString(file, charset);
            } catch (Exception e) {
                // intentar siguiente codificación
            }
        }

        throw new IOException("No se pudo leer el archivo con ninguna codificación compatible.");
    }
}
