package pl.xt.jokii.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class InventoryResultsSet {
	private ArrayList<InventoryEntry> mEntries = new ArrayList<InventoryEntry>();
	private ArrayList<String> mCategories = new ArrayList<String>();
	
	
	public InventoryResultsSet() {
		init();
    }
	
	/**
	 * Clear all entries
	 * @return
	 */
	public void init() {
		mEntries.clear();
		mCategories.clear();
	}	
	 
	/**
	 * Getter for entries
	 * @return
	 */
	public ArrayList<InventoryEntry> getEntries() {
		return mEntries;
	}

	/**
	 * Setter for entries
	 * @param entries
	 */
	public void setEntries(ArrayList<InventoryEntry> entries) {
		this.mEntries = entries;
	}	
	
//	/**
//	 * Get Categoriws list
//	 * @return ArrayList<String>
//	 */
//	public ArrayList<String> getCategoriesList()
//	{
//		ArrayList<String>  categoriesList = new ArrayList<String>();
//		
//		for(int i = 0; i < mEntries.size(); i++)
//		{
//			categoriesList.add(mEntries.get(i).getCategory());
//		}	
//		
//		return categoriesList;
//	}
	
	/**
	 * Update entry with given id
	 * @param entryDbId		 - key id from data base to find entry
	 * @param inventoryEntry - entry with new parameters
	 */
	public void updateEntry(long entryDbId, InventoryEntry inventoryEntry)
	{
		InventoryEntry e = getEntryByDbId(entryDbId);
		
		e.setName	 (inventoryEntry.getName());
		e.setAmount	 (inventoryEntry.getAmount());
		e.setCategory(inventoryEntry.getCategory());
	}
	
	/**
	 * Get entry by data base id
	 * @param dbId			  - data base id
	 * @return InventoryEntry - entry with all data
	 */
	public InventoryEntry getEntryByDbId(long dbId)
	{
		InventoryEntry inventoryEntry = null;
		
		for(InventoryEntry e: mEntries)
		{
			if(e.getId() == dbId)
			{
				inventoryEntry = e;
				break;
			}
		}		
		return inventoryEntry;
	}
	
	/**
	 * Check if category is new
	 * @param category to check
	 * @return
	 */
	private boolean isNewCategory(String category){
		for(String cat : mCategories){
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
			mCategories.add(category);	
//			InventoryEntry iec = new InventoryEntry();
//			iec.setCategory(category);
//			iec.setType(InventoryEntry.TYPE_CATEGORY);
//			addEnd(iec);
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
		mEntries.add(e);
	}

//	/**
//	 * Add entry at the begining of the list
//	 * @param e new entry
//	 */
//	private void addBegin(InventoryEntry e)
//	{
//		ArrayList<InventoryEntry> entriesTmp = new ArrayList<InventoryEntry>();
//    	
//		entriesTmp.add(e);
//		entriesTmp.addAll(mEntries);
//		mEntries.clear();
//		mEntries.addAll(entriesTmp);		
//	}
	
	/**
	 * Delete entry with given id
	 * @param entryDbId entry data base id to delete
	 */
	public void deleteEntryDbId(long entryDbId)
	{
		int index = 0;
		
		for(InventoryEntry e: mEntries)
		{
			if(e.getId() == entryDbId)
			{
				mEntries.remove(index);
				break;
			}
			index++;
		}
	}
	
//	/**
//	 * Create standard array from list
//	 */
//	private InventoryEntry[] createArrayFromList(ArrayList<InventoryEntry> inputList)
//	{
//		InventoryEntry[] entriesArr = new InventoryEntry[mEntries.size()];
//		
//		for(int i = 0; i < inputList.size(); i++)
//		{
//			entriesArr[i] = inputList.get(i);
//		}
//		
//		return entriesArr;
//	}
	
//	/**
//	 * Ceate list from array
//	 */
//	private ArrayList<InventoryEntry> createListFromArray(InventoryEntry[] inputArray)
//	{
//		ArrayList<InventoryEntry> entriesList = new ArrayList<InventoryEntry>();
//		
//		for(int i = 0; i < inputArray.length; i++)
//		{
//			entriesList.add(inputArray[i]);
//		}
//		
//		return entriesList;
//	}
	
//	/**
//	 * Sorting list by id ASC
//	 */
//	public void sortByIdAsc()
//	{
//		InventoryEntry[] entriesArr = createArrayFromList(mEntries);
//		InventoryEntry   entryTmp;
//		
//		for(int j = 0; j < (mEntries.size()-1); j++)
//		{
//			for(int i = 0; i < (mEntries.size()-1); i++)
//			{
//				if(entriesArr[i].getId() > entriesArr[i+1].getId())
//				{
//					entryTmp = entriesArr[i];
//					
//					entriesArr[i]   = entriesArr[i+1];		
//					entriesArr[i+1] = entryTmp;
//				}
//			}
//		}
//		
//		mEntries = createListFromArray(entriesArr);
//	}
	
//	/**
//	 * Sorting list by id DESC
//	 */
//	public void sortByIdDesc()
//	{
//		InventoryEntry[] entriesArr = createArrayFromList(mEntries);
//		InventoryEntry   entryTmp;
//		
//		for(int j = 0; j < (mEntries.size()-1); j++)
//		{
//			for(int i = 0; i < (mEntries.size()-1); i++)
//			{
//				if(entriesArr[i].getId() < entriesArr[i+1].getId())
//				{
//					entryTmp = entriesArr[i];
//					
//					entriesArr[i]   = entriesArr[i+1];		
//					entriesArr[i+1] = entryTmp;
//				}
//			}
//		}
//		
//		mEntries = createListFromArray(entriesArr);
//	}
	
//	/**
//	 * Sorting list by id ASC
//	 */
//	public void sortByMileageAsc()
//	{
//		InventoryEntry[] entriesArr = createArrayFromList(mEntries);
//		InventoryEntry   entryTmp;
//		
//		for(int j = 0; j < (mEntries.size()-1); j++)
//		{
//			for(int i = 0; i < (mEntries.size()-1); i++)
//			{
//				if(entriesArr[i].getAmount() > entriesArr[i+1].getAmount())
//				{
//					entryTmp = entriesArr[i];
//					
//					entriesArr[i]   = entriesArr[i+1];		
//					entriesArr[i+1] = entryTmp;
//				}
//			}
//		}
//		
//		mEntries = createListFromArray(entriesArr);
//	}
	
//	/**
//	 * Sorting list by id ASC
//	 */
//	public void sortByMileageDesc()
//	{
//		InventoryEntry[] entriesArr = createArrayFromList(mEntries);
//		InventoryEntry   entryTmp;
//		
//		for(int j = 0; j < (mEntries.size()-1); j++)
//		{
//			for(int i = 0; i < (mEntries.size()-1); i++)
//			{
//				if(entriesArr[i].getAmount() < entriesArr[i+1].getAmount())
//				{
//					entryTmp = entriesArr[i];
//					
//					entriesArr[i]   = entriesArr[i+1];		
//					entriesArr[i+1] = entryTmp;
//				}
//			}
//		}
//		
//		mEntries = createListFromArray(entriesArr);
//	}

	public ArrayList<String> getCategories() {
		return mCategories;
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
	
	/**
	 * Get entries array with additional dummy category entries for list adapter
	 * @return
	 */
	public ArrayList<InventoryEntry> getEntriesWithCategories(){
		ArrayList<InventoryEntry> entriesWithCategowies = new ArrayList<InventoryEntry>(mEntries.size() + mCategories.size());
		int i = 0;
		String previousCategory = null;
		String currentCategory  = null;
		
		// First sort entries by category
		sortByCategoryAsc();
		
		for(InventoryEntry entry : mEntries){
			currentCategory = entry.getCategory();

			if((i == 0) || (!previousCategory.equals(currentCategory))){
				InventoryEntry ie = new InventoryEntry();
				ie.setCategory(currentCategory);
				ie.setType(InventoryEntry.TYPE_CATEGORY);
				entriesWithCategowies.add(ie);
			}
			entriesWithCategowies.add(entry);
			
			previousCategory = currentCategory;
			i++;
		}
		
		return entriesWithCategowies;
	}
	
	
	/**
	 * Sorting list by category ASC
	 */
	public void sortByCategoryAsc()
	{
		Collections.sort(mEntries, new InventoryCategoryComparatorAsc());
	}
	
	private class InventoryCategoryComparatorAsc implements Comparator<InventoryEntry> {

		@Override
        public int compare(InventoryEntry lhs, InventoryEntry rhs) {
	        return lhs.getCategory().compareTo(rhs.getCategory());
        }
	}
}
