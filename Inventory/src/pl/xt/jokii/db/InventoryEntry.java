package pl.xt.jokii.db;

import android.util.Log;
import pl.xt.jokii.inventory.BuildConfig;
import pl.xt.jokii.inventory.Debug;

public class InventoryEntry {
	public static final int TYPE_ENTRY 	 = 0;
	public static final int TYPE_CATEGORY = 1;
	private EntryState mEntryState = EntryState.NORMAL;
	private long 	id;
	private String 	name;
	private String 	category;
	private int 	amount;
	private int 	type;		// type of item for list adapter (category or entry)
	
	public enum EntryState{
		NORMAL,
		DISMISSED,
		REMOVED
	}
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getCategory() {
	    return category;
    }
	public void setCategory(String category) {
	    this.category = category;
    }
	public int getType() {
	    return type;
    }
	public void setType(int type) {
	    this.type = type;
    }
	public EntryState getEntryState() {
		return mEntryState;
	}
	public void setEntryState(EntryState entryState) {
	    Debug.log("setEntryState", entryState);
		mEntryState = entryState;
	}
}
