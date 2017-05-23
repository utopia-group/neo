package synth.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {
	public static class Maybe<T> {
		private final T t;
		public Maybe(T t) {
			this.t = t;
		}
		public Maybe() {
			this.t = null;
		}
		public T get() {
			if(!this.has()) {
				throw new RuntimeException("Invalid access!");
			}
			return this.t;
		}
		public boolean has() {
			return this.t != null;
		}
	}
	
	public static class MultivalueMap<K,V> {
		private final Map<K,List<V>> map = new HashMap<K,List<V>>();
		public void add(K k, V v) {
			if(!this.map.containsKey(k)) {
				this.map.put(k, new ArrayList<V>());
			}
			this.map.get(k).add(v);
		}
		public List<V> get(K k) {
			return this.map.containsKey(k) ? this.map.get(k) : new ArrayList<V>();
		}
		public Set<K> keySet() {
			return this.map.keySet();
		}
		public int size() {
			return this.map.size();
		}
		public boolean containsKey(K k) {
			return this.map.containsKey(k);
		}
	}
	
	public static <T> Map<T,Integer> getInverse(T[] ts) {
		Map<T,Integer> inverse = new HashMap<T,Integer>();
		for(int i=0; i<ts.length; i++) {
			inverse.put(ts[i], i);
		}
		return inverse;
	}
	
	public static <T> boolean equals(Set<T> ts0, Set<T> ts1) {
		if(ts0.size() != ts1.size()) {
			return false;
		}
		for(T t : ts0) {
			if(!ts1.contains(t)) {
				return false;
			}
		}
		for(T t : ts1) {
			if(!ts0.contains(t)) {
				return false;
			}
		}
		return true;
	}
	
	public static class Pair<T0,T1> {
		public final T0 t0;
		public final T1 t1;
		public Pair(T0 t0, T1 t1) {
			this.t0 = t0;
			this.t1 = t1;
		}
	}
}
