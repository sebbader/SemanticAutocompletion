package main.java.lucene.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

import org.semanticweb.yars.nx.Node;

/**
 * A HashMap that only stores the highest values for keys
 * @author sba
 *
 * @param <K>
 * @param <V>
 */
public class MaxHashSet<T extends Pair<List<Node[]>, Float>> extends HashSet<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Associates the specified value with the specified key 
	 * in this map. If the map previously contained a smaller 
	 * mapping for the key, the smaller value is replaced.
	 * 
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 * @return the smaller and therefore replaced value. If the new value 
	 * is smaller, this one is returned. If the old one is smaller, the old
	 * one is returned
	 * 
	 */
	@Override
	public boolean add(T new_pair) {
		
		Node key = new_pair.getKey().get(0)[0];
		Pair<List<Node[]>, Float> old_pair = get( key );

		if ( old_pair != null) {
			
			
			List<Node[]> old_nodes = old_pair.getKey();
			List<Node[]> new_nodes = new_pair.getKey();
			
			for (Iterator<Node[]> iter_new = new_nodes.iterator(); iter_new.hasNext() ; ) {
				
				Node[] new_triple = iter_new.next();
				boolean is_new = true;
				
				for (Iterator<Node[]> iter_old = old_nodes.iterator() ; iter_old.hasNext() ; ) {

					Node[] old_triple = iter_old.next();
					
					if (old_triple[1].toString().equalsIgnoreCase(new_triple[1].toString())) 
						if (old_triple[2].toString().equalsIgnoreCase(new_triple[2].toString()))
							is_new = false;
															
				}
				
				if (is_new) {
					old_pair.getKey().add(new_triple);
				}
				
				
			}
			
			
			

			float old_value = (float) old_pair.getValue();
			float new_value = (float) new_pair.getValue();
			
			if ( old_value < new_value ) {
				old_pair.setValue(new_value);
			} 
			return false;

		} else {
			return super.add(new_pair);
		}

	}
	
	
	
	public boolean addMaxAll(Collection<T> new_set) {
		
		boolean is_changed = false;
		
		for (Iterator<T> iter = new_set.iterator(); iter.hasNext(); ) {
			boolean result = add( iter.next() );
			if ( result ) is_changed = true;
		}
		
		return is_changed;
	}
	
	

	public Pair<List<Node[]>, Float> get(Node subject) {
		
		for (Iterator<T> iter =  super.iterator(); iter.hasNext(); ) {
			Pair<List<Node[]>, Float> pair = iter.next();
			if (!pair.getKey().isEmpty()) {
				if (pair.getKey().get(0)[0].toString().equalsIgnoreCase(subject.toString()) ) {
					return pair;
				}
			}
		}
		
		return null;
	}
	

	/*
	public void putAllMax(Map<? extends K, Float> m) {
		
		for ( Iterator<? extends K> iter = m.keySet().iterator(); iter.hasNext(); ) {
			K key = iter.next();
			put(key, m.get(key));
		}

    }

	

	public static <K, V extends Comparable<? super V>> MaxHashSet<K, V> sortByValue(MaxHashSet<K, V> map) {
		
		 Map<K, V> normal_map = map.entrySet()
				.stream()
				.sorted(Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(
						Map.Entry::getKey, 
						Map.Entry::getValue, 
						(e1, e2) -> e1, 
						LinkedHashMap::new
						));
		 
		 MaxHashSet<K, V> maxmap = new MaxHashSet<>();
		 maxmap.putAllMax(normal_map);
		 
		 return maxmap;
	}
	*/
}
