package giw.prac3;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

public class Buscador {
    public static void main(String[] args) throws Exception {
        listarTodosDocumentos();
        //busquedaPorFrase();
    }

    private static void listarTodosDocumentos(){
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";
        System.out.println("------------------------------------------------------------------------");
        try{
            FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));
            QueryParser queryParser = new QueryParser("title", new StandardAnalyzer());
            Query query = queryParser.parse("*:*");
            TopDocs todos = searcher.search(query, 2000);
            if (todos.scoreDocs != null) {
                StoredFields storedFields = searcher.storedFields();
                DirectoryReader reader = DirectoryReader.open(indexDir);
                System.out.println("Total de documentos: " + reader.maxDoc());
                for (ScoreDoc doc : todos.scoreDocs){
                    int docidx = doc.doc;
                    Document docRetrieved = storedFields.document(docidx);
                    System.out.println("------------------------------------------------------------------------");
                    System.out.println("Score: " + doc.score +"| Title: "+docRetrieved.get("TITLE"));
                    System.out.println("Path: "+docRetrieved.get("PATH"));

                }
                System.out.println("------------------------------------------------------------------------");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Funciona igual si se usa un sólo término
     */
    private static void busquedaPorFrase(){
        String indexPath = "/home/lassy/MasterUGR/GIW/Practica3/index";
        String searchVal = "Keep silent about this!";
        //String searchVal = "The Woman of Dunes";
        //String searchVal = "love";
        System.out.println("------------------------------------------------------------------------");
        System.out.println("BUSCANDO: "+searchVal);

        try{
            FSDirectory indexDir = FSDirectory.open(Paths.get(indexPath));
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));

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
}
