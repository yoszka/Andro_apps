package pl.xt.jokii.db;

import java.util.ArrayList;

public class InventoryResultsSet {
	private ArrayList<InventoryEntry> entries = new ArrayList<InventoryEntry>();
	private ArrayList<String> cathegories = new ArrayList<String>();
	
	
	public InventoryResultsSet() {
		init();
    }
	
	/**
	 * Clear all entries
	 * @return
	 */
	public void init() {
		this.entries.clear();
		this.cathegories.clear();
	}	
	 
	/**
	 * Getter for entries
	 * @return
	 */
	public ArrayList<InventoryEntry> getEntries() {
		return entries;
	}

	/**
	 * Setter for entries
	 * @param entries
	 */
	public void setEntries(ArrayList<InventoryEntry> entries) {
		this.entries = entries;
	}	
	
	/**
	 * Get Headers list
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getHeadersList()
	{
		ArrayList<String>  headersList = new ArrayList<String>();
		
		for(int i = 0; i < this.entries.size(); i++)
		{
			headersList.add(this.entries.get(i).getName());
		}	
		
		return headersList;
	}
	
	/**
	 * Update entry with given id
	 * @param entryId		- key id to find entry
	 * @param carServEntry	- entry with new parameters
	 */
	public void updateEntry(long entryId, InventoryEntry carServEntry)
	{
		InventoryEntry e = getEntryById(entryId);
		
		e.setName	(carServEntry.getName());
		e.setAmount	(carServEntry.getAmount());
		
		/*int index = 0;
		
		for(CarServEntry e: this.entries)
		{
			if(e.getId() == entryId)
			{
				this.entries.get(index).setDate		(carServEntry.getDate());
				this.entries.get(index).setHeader	(carServEntry.getHeader());
				this.entries.get(index).setMileage	(carServEntry.getMileage());
				this.entries.get(index).setType		(carServEntry.getType());
				break;
			}
			index++;
		}
		*/
	}
	
	/**
	 * Get entry by data base id
	 * @param id			- data base id
	 * @return CarServEntry	- entry with all data
	 */
	public InventoryEntry getEntryById(long id)
	{
		InventoryEntry carServEntry = null;
		
		for(InventoryEntry e: this.entries)
		{
			if(e.getId() == id)
			{
				carServEntry = e;
				break;
			}
		}		
		return carServEntry;
	}
	
	/**
	 * Check if category is new
	 * @param category to check
	 * @return
	 */
	private boolean isNewCategory(String category){
		for(String cat : cathegories){
			if(cat.equals(category)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Add entry to the list
	 * @param e new entry
	 */
	public void addEntry(InventoryEntry e)
	{
		String category = e.getCategory();
		if(isNewCategory(category)){
			cathegories.add(category);	
			InventoryEntry iec = new InventoryEntry();
			iec.setCategory(category);
			iec.setType(InventoryEntry.TYPE_CATEGORY);
			addEnd(iec);
		}
		e.setType(InventoryEntry.TYPE_ENTRY);
		addEnd(e);
	}
	
	/**
	 * Add entry at the end of list
	 * @param e new entry
	 */
	private void addEnd(InventoryEntry e)
	{
		entries.add(e);
	}

	/**
	 * Add entry at the begining of the list
	 * @param e new entry
	 */
	private void addBegin(InventoryEntry e)
	{
		ArrayList<InventoryEntry> entriesTmp = new ArrayList<InventoryEntry>();
    	
		entriesTmp.add(e);
		entriesTmp.addAll(entries);
		entries.clear();
		entries.addAll(entriesTmp);		
	}
	
	/**
	 * Delete entry with given id
	 * @param entryId entry to delete
	 */
	public void deleteEntryId(long entryId)
	{
		int index = 0;
		
		for(InventoryEntry e: entries)
		{
			if(e.getId() == entryId)
			{
				entries.remove(index);
				break;
			}
			index++;
		}
	}
	
	/**
	 * Create standard array from list
	 */
	private InventoryEntry[] createArrayFromList(ArrayList<InventoryEntry> inputList)
	{
		InventoryEntry[] entriesArr = new InventoryEntry[entries.size()];
		
		for(int i = 0; i < inputList.size(); i++)
		{
			entriesArr[i] = inputList.get(i);
		}
		
		return entriesArr;
	}
	
	/**
	 * Ceate list from array
	 */
	private ArrayList<InventoryEntry> createListFromArray(InventoryEntry[] inputArray)
	{
		ArrayList<InventoryEntry> entriesList = new ArrayList<InventoryEntry>();
		
		for(int i = 0; i < inputArray.length; i++)
		{
			entriesList.add(inputArray[i]);
		}
		
		return entriesList;
	}
	
	/**
	 * Sorting list by id ASC
	 */
	public void sortByIdAsc()
	{
		InventoryEntry[] entriesArr = createArrayFromList(entries);
		InventoryEntry   entryTmp;
		
		for(int j = 0; j < (entries.size()-1); j++)
		{
			for(int i = 0; i < (entries.size()-1); i++)
			{
				if(entriesArr[i].getId() > entriesArr[i+1].getId())
				{
					entryTmp = entriesArr[i];
					
					entriesArr[i]   = entriesArr[i+1];		
					entriesArr[i+1] = entryTmp;
				}
			}
		}
		
		entries = createListFromArray(entriesArr);
	}
	
	/**
	 * Sorting list by id DESC
	 */
	public void sortByIdDesc()
	{
		InventoryEntry[] entriesArr = createArrayFromList(entries);
		InventoryEntry   entryTmp;
		
		for(int j = 0; j < (entries.size()-1); j++)
		{
			for(int i = 0; i < (entries.size()-1); i++)
			{
				if(entriesArr[i].getId() < entriesArr[i+1].getId())
				{
					entryTmp = entriesArr[i];
					
					entriesArr[i]   = entriesArr[i+1];		
					entriesArr[i+1] = entryTmp;
				}
			}
		}
		
		entries = createListFromArray(entriesArr);
	}
	
	/**
	 * Sorting list by id ASC
	 */
	public void sortByMileageAsc()
	{
		InventoryEntry[] entriesArr = createArrayFromList(entries);
		InventoryEntry   entryTmp;
		
		for(int j = 0; j < (entries.size()-1); j++)
		{
			for(int i = 0; i < (entries.size()-1); i++)
			{
				if(entriesArr[i].getAmount() > entriesArr[i+1].getAmount())
				{
					entryTmp = entriesArr[i];
					
					entriesArr[i]   = entriesArr[i+1];		
					entriesArr[i+1] = entryTmp;
				}
			}
		}
		
		entries = createListFromArray(entriesArr);
	}
	
	/**
	 * Sorting list by id ASC
	 */
	public void sortByMileageDesc()
	{
		InventoryEntry[] entriesArr = createArrayFromList(entries);
		InventoryEntry   entryTmp;
		
		for(int j = 0; j < (entries.size()-1); j++)
		{
			for(int i = 0; i < (entries.size()-1); i++)
			{
				if(entriesArr[i].getAmount() < entriesArr[i+1].getAmount())
				{
					entryTmp = entriesArr[i];
					
					entriesArr[i]   = entriesArr[i+1];		
					entriesArr[i+1] = entryTmp;
				}
			}
		}
		
		entries = createListFromArray(entriesArr);
	}	
	
	/**
	 * Sorting list by id ASC
	 */
//	public void sortByDateAsc()
//	{
//		InventoryEntry[] entriesArr = createArrayFromList(entries);
//		InventoryEntry   entryTmp;
//		
//		for(int j = 0; j < (entries.size()-1); j++)
//		{
//			for(int i = 0; i < (entries.size()-1); i++)
//			{
//				if(entriesArr[i].getDate() > entriesArr[i+1].getDate())
//				{
//					entryTmp = entriesArr[i];
//					
//					entriesArr[i]   = entriesArr[i+1];		
//					entriesArr[i+1] = entryTmp;
//				}
//			}
//		}
//		
//		entries = createListFromArray(entriesArr);
//	}
	
	/**
	 * Sorting list by id ASC
	 */
//	public void sortByDateDesc()
//	{
//		InventoryEntry[] entriesArr = createArrayFromList(entries);
//		InventoryEntry   entryTmp;
//		
//		for(int j = 0; j < (entries.size()-1); j++)
//		{
//			for(int i = 0; i < (entries.size()-1); i++)
//			{
//				if(entriesArr[i].getDate() < entriesArr[i+1].getDate())
//				{
//					entryTmp = entriesArr[i];
//					
//					entriesArr[i]   = entriesArr[i+1];		
//					entriesArr[i+1] = entryTmp;
//				}
//			}
//		}
//		
//		entries = createListFromArray(entriesArr);
//	}	
}
