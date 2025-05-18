package giw.prac3;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;

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

        if (args.length != 3) {
            System.err.println("Uso: java Indexador <directorio-documentos> <archivo-stopwords> <directorio-indice>");
            System.exit(1);
        }

        docsPath = args[0];
        stopwordsPath = args[1];
        indexPath = args[2];

        FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));

        CharArraySet stopwords = loadStopwords(stopwordsPath);
        Analyzer analyzer = new StandardAnalyzer(stopwords);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        config.setCodec(new SimpleTextCodec());
        IndexWriter writer = new IndexWriter(indexDir, config);

        indexDocs(writer, Paths.get(docsPath));

        writer.close();

        try (DirectoryReader reader = DirectoryReader.open(indexDir)) {
            System.out.println("Total de documentos indexados: " + reader.maxDoc());
        }
        System.out.println("Indexación completa.");
    }

    private static CharArraySet loadStopwords(String filePath) throws IOException {
        ArrayList<String> stopwordList = new ArrayList<>();
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
                .filter(p -> p.toString().endsWith(".srt"))
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
                return;
            }

            Document doc = new Document();
            String nombreArchivo [] = file.toString().split("/");
            doc.add(new TextField("PATH", file.toString(), Field.Store.YES));
            doc.add(new TextField("TITLE", nombreArchivo[nombreArchivo.length-1], Field.Store.YES));
            doc.add(new TextField("CONTENT", content, Field.Store.YES));
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
