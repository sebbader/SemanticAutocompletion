package main.java.lucene.search;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.numeric.XsdFloat;

import com.ontologycentral.nxindexer.keyword.KeywordSearcher;
import com.ontologycentral.nxindexer.nodes.NodesSearcher;

import main.java.namespaces.STEP;


public class LuceneSearcher {

	private String indexLocation;


	public static void main(String[] args) {
		LuceneSearcher searcher = new LuceneSearcher("WebContent/resources/StepOntologyIndex/lucene");

		String query = "pult";
		//		query = "Tauch*";
		//		String field = "water";
		try {

			List<Node[]> l = searcher.search(query);


			for ( Iterator<Node[]> iter = l.iterator(); iter.hasNext(); ) {

				Node[] node = iter.next();

				System.out.println(node[0]
						+ " " + node[1]
								+ " " + node[2] + " ." );

			}


			// Ausgabe
			//System.out.println(l);
			// return l; (Methodenaufruf)
			// return Response.ok( l ).build();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public LuceneSearcher(String indexLocation) {
		this.indexLocation = indexLocation;
	}



	public List<Node[]> search(String query) throws IOException {

//		KeywordSearcher searcher = new KeywordSearcher(indexLocation);
		NodesSearcher searcher;

		//*********************************************************
		// 			start a query with the term as is
		//*********************************************************
		searcher = new NodesSearcher(indexLocation);
		List<Pair<List<Node[]>, Float>> basic_results = searcher.getResults(query, 10);
		searcher.close();

		//basic_results = sortByValue(basic_results);


		//*********************************************************
		// 			start an autocomplete query 
		//*********************************************************
		searcher = new NodesSearcher(indexLocation);
		List<Pair<List<Node[]>, Float>> autocomplete_results = searcher.getResults("*" + query + "*", 10);
		searcher.close();
		//autocomplete_results = sortByValue(autocomplete_results);


		//*********************************************************
		// 			start an fuzzy query 
		//*********************************************************
		searcher = new NodesSearcher(indexLocation);
		List<Pair<List<Node[]>, Float>> fuzzy_results = searcher.getResults(query + "~0.7", 10);
		fuzzy_results.forEach( x -> x.setValue( x.getRight() / 3 ));
		searcher.close();
		//fuzzy_results = sortByValue(fuzzy_results);
		

		//*********************************************************
		// 			combine results 
		//*********************************************************
		MaxHashSet<Pair<List<Node[]>, Float>> results = new MaxHashSet<Pair<List<Node[]>, Float>>();
//		results.addMaxAll(basic_results);
		results.addMaxAll(autocomplete_results);
		results.addMaxAll(fuzzy_results);
				
		//results = MaxTreeMap.sortByValue(results);
		
		
		//List<Pair<List<Node[]>, Float>> results = new ArrayList<Pair<List<Node[]>, Float>>();
		//results.addAll(autocomplete_results);
		

		// RDF: (Subject, Predicate, Object) --> Node["Subject", "Predicate", "Object"]
		// RDF graph: {rdf-triples*} --> List<Node[]>
		List<Node[]> graph = new ArrayList<Node[]>();


		for ( Iterator<Pair<List<Node[]>, Float>> iter = results.iterator(); iter.hasNext(); ) {

			Pair<List<Node[]>, Float> pair = iter.next();
			List<Node[]> nodes = pair.getKey();
			Float score = pair.getValue();
			Node subj = nodes.get(0)[0];
			
			
			for ( Iterator<Node[]> i = nodes.iterator(); i.hasNext(); ) {
				Node[] triple = i.next();
				graph.add( triple );
			}
			

			graph.add( new Node[] { subj, STEP.hasScore, new Literal(Float.toString(score), XsdFloat.DT ) } );

		}


		return graph;
	}

	
	public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMap(final Map<K, V> mapToSort) {
		List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(mapToSort.size());
 
		entries.addAll(mapToSort.entrySet());
 
		// Sorts the specified list according to the order induced by the specified comparator
		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(final Map.Entry<K, V> entry1, final Map.Entry<K, V> entry2) {
				// Compares this object with the specified object for order
				return entry1.getValue().compareTo(entry2.getValue()) * (-1);
			}
		});
 
		LinkedHashMap<K, V> sortedCrunchifyMap = new LinkedHashMap<K, V>();
 
		// The Map.entrySet method returns a collection-view of the map
		for (Map.Entry<K, V> entry : entries) {
			sortedCrunchifyMap.put(entry.getKey(), entry.getValue());
		}
 
		return sortedCrunchifyMap;
	}




}
