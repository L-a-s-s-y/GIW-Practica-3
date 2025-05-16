package giw.prac3;

//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
import java.nio.file.Paths;
import java.util.ArrayList;

//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

public class Buscador {
    public static void main(String[] args) throws Exception {
        //busquedaPorTermino();
        //listarTodosDocumentos();
        busquedaPorFrase();
        
    }

    private static void listarTodosDocumentos(){
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";
        System.out.println("------------------------------------------------------------------------");
        //System.out.println("D: "+searchVal);
        try{
            FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));
            //QueryBuilder queryBuilder = new QueryBuilder(new StandardAnalyzer());
            QueryParser queryParser = new QueryParser("title", new StandardAnalyzer());
            Query query = queryParser.parse("*:*");
            TopDocs todos = searcher.search(query, 2000);
            if (todos.scoreDocs != null) {
                StoredFields storedFields = searcher.storedFields();
                DirectoryReader reader = DirectoryReader.open(indexDir);
                System.out.println("Total de documentos: " + reader.maxDoc());
                for (ScoreDoc doc : todos.scoreDocs){
                    System.out.println("------------------------------------------------------------------------");
                    int docidx = doc.doc;
                    //Document docRetrieved = searcher.doc(docidx);
                    Document docRetrieved = storedFields.document(docidx);
                    System.out.println("TITULO: "+docRetrieved.get("TITLE"));
                    System.out.println("PATH: "+docRetrieved.get("PATH"));
                    //System.out.println(docRetrieved.get("CONTENT"));

                }
                System.out.println("------------------------------------------------------------------------");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

        private static void busquedaPorFrase(){
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";
        //String searchVal = "Keep silent about this!";
        String searchVal = "The Woman of Dunes";
        //String stopwordsPath = "/home/lassy/MasterUGR/GIW/Practica3/lista_stop_words.txt";
        System.out.println("------------------------------------------------------------------------");
        System.out.println("BUSCANDO: "+searchVal);

        try{
            FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));
            //QueryBuilder queryBuilder = new QueryBuilder(new StandardAnalyzer());
            
            //List<String> allSearchKeywords = splitSearchText(searchVal);
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
                System.out.println("Final Query: " + finalQry.toString());

                TopDocs allFound = searcher.search(finalQry, 20);

                if (allFound.scoreDocs != null) {
                    StoredFields storedFields = searcher.storedFields();
                    for (ScoreDoc doc : allFound.scoreDocs){
                        int docidx = doc.doc;
                        Document docRetrieved = storedFields.document(docidx);
                        System.out.println("------------------------------------------------------------------------");
                        System.out.println("Score: " + doc.score +"| Title: "+docRetrieved.get("TITLE"));
                        System.out.println("Path: "+docRetrieved.get("PATH"));
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("------------------------------------------------------------------------");
        System.out.println("Búsqueda finalizada");
        System.out.println("------------------------------------------------------------------------");
    }

    private static void busquedaPorTermino(){
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";
        String searchVal = "love";
        //String stopwordsPath = "/home/lassy/MasterUGR/GIW/Practica3/lista_stop_words.txt";
        System.out.println("------------------------------------------------------------------------");
        System.out.println("BUSCANDO: "+searchVal);

        try{
            FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));
            QueryBuilder queryBuilder = new QueryBuilder(new StandardAnalyzer());
            
            Query q1 = queryBuilder.createPhraseQuery("TITLE", searchVal);
            Query q2 = queryBuilder.createPhraseQuery("PATH", searchVal);
            Query q3 = queryBuilder.createPhraseQuery("CONTENT", searchVal);
            Query q6 = new TermQuery(new Term("CONTENT", searchVal));

            BooleanQuery.Builder chainQueryBuilder = new BooleanQuery.Builder();
            chainQueryBuilder.add(q1, Occur.SHOULD);
            chainQueryBuilder.add(q2, Occur.SHOULD);
            chainQueryBuilder.add(q3, Occur.SHOULD);
            chainQueryBuilder.add(q6, Occur.SHOULD);         

            BooleanQuery finalQuery = chainQueryBuilder.build();
            TopDocs allFound = searcher.search(finalQuery, 1);

            if (allFound.scoreDocs != null) {
                StoredFields storedFields = searcher.storedFields();
                for (ScoreDoc doc : allFound.scoreDocs){
                    System.out.println("------------------------------------------------------------------------");
                    System.out.println("Score: " + doc.score);
                    int docidx = doc.doc;
                    //Document docRetrieved = searcher.doc(docidx);
                    Document docRetrieved = storedFields.document(docidx);
                    System.out.println("\tTITULO: "+docRetrieved.get("TITLE"));
                    System.out.println("\tPATH: "+docRetrieved.get("PATH"));
                    //System.out.println(docRetrieved.get("CONTENT"));
                    System.out.println("------------------------------------------------------------------------");
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("------------------------------------------------------------------------");
        System.out.println("Búsqueda finalizada");
        System.out.println("------------------------------------------------------------------------");
    }

    /*private static void Resultados_UNO(){
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";
        String queryStr = "love";
        String stopwordsPath = "/home/lassy/MasterUGR/GIW/Practica3/lista_stop_words.txt";
        try {
            FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
            
            DirectoryReader reader = DirectoryReader.open(indexDir);
            System.out.println("Total de documentos indexados: " + reader.maxDoc());

            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));

            CharArraySet stopwords = loadStopwords(stopwordsPath);
            Analyzer analyzer = new StandardAnalyzer(stopwords);
            QueryParser parser = new QueryParser("contents", analyzer);

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
    }*/

    /*private static CharArraySet loadStopwords(String filePath) throws IOException {
        List<String> stopwordList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopwordList.add(line.trim());
            }
        }
        return new CharArraySet(stopwordList, true);
    }*/
}
