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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        config.setCodec(new SimpleTextCodec());

        // Crear el índice en disco
        FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
        IndexWriter writer = new IndexWriter(indexDir, config);

        // Indexar documentos
        indexDocs(writer, Paths.get(docsPath));

        writer.close();
        // Verificación de la indexación
        try (DirectoryReader reader = DirectoryReader.open(indexDir)) {
            System.out.println("Total de documentos indexados: " + reader.maxDoc());
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
            String nombreArchivo [] = file.toString().split("/");
            //System.out.println(test[test.length-1]);
            //limpiarTitulo(test[test.length-1]);
            doc.add(new TextField("PATH", file.toString(), Field.Store.YES));
            //doc.add(new TextField("TITLE", limpiarTitulo(nombreArchivo[nombreArchivo.length-1]), Field.Store.YES));
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

    private static String limpiarTitulo(String nombreArchivo) {

        String originalName = nombreArchivo;
        String cleaned = originalName;

        // 1) Quitar extensión .srt
        cleaned = cleaned.replaceAll("(?i)\\.srt$", "");

        // 2) Eliminar corchetes y paréntesis (metadatos de release)
        cleaned = cleaned.replaceAll("\\[.*?\\]", " ");
        cleaned = cleaned.replaceAll("\\(.*?\\)", " ");

        // 3) Eliminar resoluciones y contenedores
        cleaned = cleaned.replaceAll("(?i)\\b(480p|720p|1080p|1080i|2160p|4K|BluRay|BRRip|WEB[-\\.]?DL|WEBRip|HDRip|HDTV|DVDRip|REMASTERED|PROPER|REMUX|NF|AMZN|BD|Restauration|U[-_]?NEXT)\\b", " ");

        // 4) Eliminar códecs y pistas de audio
        cleaned = cleaned.replaceAll("(?i)\\b(x264|x265|H\\.264|HEVC|AVC|10bit|FLAC\\d(?:\\.\\d)?|AAC(?:5\\.1|2\\.0|\\d(?:\\.\\d)?)?|AC3|DDP\\d(?:\\.\\d)?|2CH|6CH|Dual|Commentary|PvtCdx|Jpn)\\b", " ");

        // 5) Eliminar tracks, IDs y sufijos sueltos
        cleaned = cleaned.replaceAll("(?i)\\btrack\\d+\\b", " ");
        cleaned = cleaned.replaceAll("tt\\d+", " ");
        cleaned = cleaned.replaceAll("(?i)\\b(srt|en|subs?)\\b", " ");

        // 6) Eliminar nombres de grupos más exhaustivo
        cleaned = cleaned.replaceAll("(?i)\\b(YTS\\.MX|YTS\\.AM|YTS\\.LT|VXT|RO|PLB|HANDJOB|RSG|NickiEX|MagicStar|HeVK|eng|ENG|english|English|ENGLISH|BiPOLAR|Classics|TEMHO|GDK|PLiSSKEN)\\b", " ");

        // 7) Eliminar años
        cleaned = cleaned.replaceAll("\\b(19\\d{2}|20[0-2]\\d)\\b", " ");

        // 8) Ahora sí tokenizar: puntos, guiones y guiones bajos → espacio
        cleaned = cleaned.replaceAll("[._\\-]+", " ");

        // 9) Eliminar cualquier dígito sobrante
        cleaned = cleaned.replaceAll("\\d+", " ");

        // 10) Colapsar espacios repetidos y recortar
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        // Mostrar resultado
        //System.out.println("Original: " + originalName);
        //System.out.println("Título limpio: " + cleaned + "\n");
        return cleaned;
    }
}
