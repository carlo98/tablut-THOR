package thor;

/**
 * @author Carlo Cena, Giacomo Zamprogno
 * @implNote Class used to represent a state in the hash table used to identify draws.
 *
 */
public class StateDictEntry {
	private int key;
	private int used;

	/**
	 * @param key: hash of the element to be added to the dictionary
	 * @param used: 1 if element is used, 0 otherwise
	 */
	public StateDictEntry(int key, int used) {
		this.key = key;
		this.used = used;
	}

	int getKey() {
		return key;
	}

	int getUsed() {
		return used;
	}

	void setUsed() {
		this.used = 1;
	}

	@Override
	public String toString() {
		return "StateDictEntry [key=" + key + ", used=" + used + "]";
	}
}
