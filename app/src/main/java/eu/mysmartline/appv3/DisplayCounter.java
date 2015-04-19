package eu.mysmartline.appv3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class DisplayCounter {
	private static DisplayCounter instance = null;

	private List<String> list;

	private DisplayCounter() {
		this.list = new ArrayList<String>();
	}

	public static DisplayCounter getInstance() {
		if (instance == null) {
			instance = new DisplayCounter();
		}
		return instance;
	}

	public void add(String message) {
		
		if (!this.list.contains(message)) {
			List<String>newList = new ArrayList<String>();
			newList.add(message);
			int i = 0;
			for(String item: this.list){
				newList.add(item);
				if (i==10){
					break;
				}
			}
			this.list = newList;
		}
	}

	public List<String> getCoutner() {
		return this.list;
	}
}
