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
import org.apache.lucene.util.QueryBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Buscador {
    public static void main(String[] args) throws Exception {
        Resultados_DOS();
    }

    private static void Resultados_DOS(){
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";
        String queryStr = "love";
        String stopwordsPath = "/home/lassy/MasterUGR/GIW/Practica3/lista_stop_words.txt";

        try{
            FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));
            QueryBuilder queryBuilder = new QueryBuilder(new StandardAnalyzer());
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Búsqueda finalizada");
    }

    private static void Resultados_UNO(){
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";
        String queryStr = "love";
        String stopwordsPath = "/home/lassy/MasterUGR/GIW/Practica3/lista_stop_words.txt";
        try {
            FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
            
            DirectoryReader reader = DirectoryReader.open(indexDir);
            System.out.println("Total de documentos indexados: " + reader.maxDoc());

            //IndexSearcher searcher = new IndexSearcher(reader);
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));

            CharArraySet stopwords = loadStopwords(stopwordsPath);
            Analyzer analyzer = new StandardAnalyzer(stopwords);
            QueryParser parser = new QueryParser("contents", analyzer); // usamos el mismo Analyzer

            Query query = parser.parse(queryStr);
            TopDocs hits = searcher.search(query, 10);

            System.out.println("Documentos encontrados para \"" + queryStr + "\": " + hits.totalHits.value);
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                System.out.println(" -> " + doc.get("path"));
            }
        }
        catch (Exception e){
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
}
