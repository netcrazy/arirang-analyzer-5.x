package org.apache.lucene.ko;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * Hello Lucene
 * 2015-06-24
 * 참고) http://www.lucenetutorial.com/lucene-in-5-minutes.html
 * @author ikchoi
 *
 */
public class testQueryToIndex extends TestCase {
	Directory index = new RAMDirectory();

	public void testQuery() throws Exception {
		String querystr = "수면양말";

		//인덱스 호출
		index();

		//검색수행
		System.out.println("=== search ===");
		KoreanAnalyzer analyzer = new KoreanAnalyzer();
		analyzer.setOriginCNoun(true);
		analyzer.setHasOrigin(false);
		analyzer.setQueryMode(true);
		analyzer.setBigrammable(false);

		QueryParser qp = new QueryParser("title", analyzer);
		qp.setDefaultOperator(Operator.OR);
		Query query = qp.parse(querystr);

		System.out.println(String.format("Query : %s", query.toString()));

		int hitsPerPage = 10;

		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);

		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		searcher.search(query, collector);

		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		System.out.println("Found "+hits.length+" hits.");
		for(int i=0;i<hits.length;i++) {
			int docid = hits[i].doc;
			Document d = searcher.doc(docid);

			System.out.println(d.get("title"));
		}
	}

	/**
	 * Document 인덱싱
	 * @throws IOException
	 */
	private void index() throws IOException {

		String[] idxstrs = new String[] {"수면양말", "수면","양말","c#개발","파이썬개발자"};

		KoreanAnalyzer analyzer = new KoreanAnalyzer();
		analyzer.setOriginCNoun(true);
		analyzer.setHasOrigin(true);
		analyzer.setBigrammable(true);

		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		IndexWriter writer = new IndexWriter(index, config);

		for(int i=0; i<idxstrs.length;i++) {
			Document doc = new Document();
			doc.add(new TextField("title", idxstrs[i], Field.Store.YES));
			writer.addDocument(doc);
		}

		writer.close();
	}
}
