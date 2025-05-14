package giw.prac3;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;


public class App {
    public static void main(String[] args) throws Exception {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = new ByteBuffersDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);

        Document doc = new Document();
        doc.add(new TextField("title", "Lucene con ByteBuffersDirectory", Field.Store.YES));
        doc.add(new TextField("content", "Estamos usando Lucene moderno sin RAMDirectory", Field.Store.YES));
        writer.addDocument(doc);
        writer.close();

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser parser = new QueryParser("content", analyzer);
        Query query = parser.parse("Lucene");

        TopDocs results = searcher.search(query, 5);
        for (ScoreDoc scoreDoc : results.scoreDocs) {
            Document resultDoc = searcher.doc(scoreDoc.doc);
            System.out.println("Encontrado: " + resultDoc.get("title"));
        }

        reader.close();
        index.close();
    }
}
