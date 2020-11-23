package thor;

import java.util.List;

public class StateDictEntry {
	private int key;
	private List<List<Integer>> actions;
	private double value = Double.MAX_VALUE;
	private int max_depth;
	private int used;
	private int current_depth;

	public StateDictEntry(int key, double value, List<List<Integer>> actions, int used, int max_depth, int current_depth) {
		this.key = key;
		this.actions = actions;
		this.value = value;
		this.used = used;
		this.max_depth = max_depth;
		this.current_depth = current_depth;
	}

	int getKey() {
		return key;
	}

	List<List<Integer>> getActions() {
		return actions;
	}

	double getValue() {
		return value;
	}
	
	int getUsed() {
		return used;
	}
	
	void setUsed() {
		this.used = 1;
	}

	int getMax_depth() {
		return max_depth;
	}

	int getCurrent_depth() {
		return current_depth;
	}
	
	void setActions(List<List<Integer>> actions) {
		this.actions = actions;
	}
}
