package pl.xt.jokii.gcpaymentcalculator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Tomek on 01.06.13.
 */
public class OptionManager {
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mPrefsEditor;
    public  static final String OPTIONS              = "OPTIONS";
    public  static final String OPTIONS_JSON         = "OPTIONS_JSON";
    private static final String CURRENT_OPTIONS_JSON = "CURRENT_OPTIONS_JSON";
    private static final String OPTION_SAVE_NAME     = "OPTION_SAVE_NAME";
    private static final String OPTION_SAVE_SET      = "OPTION_SAVE_SET";
    private static final String OPTION_NAME          = "OPTION_NAME";
    private static final String OPTION_PAY_COMPANY   = "OPTION_PAY_COMPANY";
    private static final String OPTION_PAY_EMPLOYEE  = "OPTION_PAY_EMPLOYEE";

    public OptionManager(Context ctx){
        mSharedPrefs = ctx.getSharedPreferences(OPTIONS, Activity.MODE_PRIVATE);
        mPrefsEditor = mSharedPrefs.edit();
    }

    /**
     * Get current options from storage
     * @return options
     */
    public ArrayList<OptionStore> getCurrentOptions(){
        ArrayList<OptionStore> options = new ArrayList<OptionStore>(0);
        JSONArray jArrayCurrentAll;
        String optionsJsonStr = mSharedPrefs.getString(CURRENT_OPTIONS_JSON, null);
        if(optionsJsonStr != null){
            try {
                options = parseCurrentOptions(new JSONArray(optionsJsonStr));
            } catch (JSONException e) {}
        }

        return options;
    }

    /**
     * Parse JSONArray with options to ArrayList of options
     * @param jArrayCurrentAll
     * @return
     */
    private ArrayList<OptionStore> parseCurrentOptions(JSONArray jArrayCurrentAll){
        ArrayList<OptionStore> options = new ArrayList<OptionStore>(0);
        int arrayLength = jArrayCurrentAll.length();
        JSONObject entryElement;
        String optionName;
        double optionPayCompany;
        double optionPayEmployee;
        try {
            for(int i = 0; i < arrayLength; i++){
                entryElement =  (JSONObject) jArrayCurrentAll.get(i);
                optionName = entryElement.getString(OPTION_NAME);
                optionPayCompany = entryElement.getDouble(OPTION_PAY_COMPANY);
                optionPayEmployee = entryElement.getDouble(OPTION_PAY_EMPLOYEE);
                options.add(new OptionStore(optionName, optionPayCompany, optionPayEmployee));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return options;
    }

    /**
     * Save current options to curent storage
     * @param options
     */
    public void saveCurrentOptions(ArrayList<OptionStore> options){
        String optionsJSONString = currentOptionsToJSONString(options);
        if(!TextUtils.isEmpty(optionsJSONString)){
            mPrefsEditor.putString(CURRENT_OPTIONS_JSON, optionsJSONString);
            mPrefsEditor.commit();
        }
    }


    /**
     * Convert current options array to JSON string representation
     * @param options
     * @return
     */
    private String currentOptionsToJSONString(ArrayList<OptionStore> options){
        JSONArray jArrayAll = new JSONArray();
        JSONObject jsonEntry;
        for(OptionStore option: options){
            jsonEntry = new JSONObject();
            try {
                jsonEntry.put(OPTION_NAME, 	        option.name);		    // String
                jsonEntry.put(OPTION_PAY_COMPANY, 	option.payCompany);		// double
                jsonEntry.put(OPTION_PAY_EMPLOYEE, 	option.payEmployee);    // double
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jArrayAll.put(jsonEntry);
        }

        return jArrayAll.toString();
    }

    /**
     * Add another option to current storage
     * @param option
     */
    public void addCurrentOption(OptionStore option){
        ArrayList<OptionStore> options = getCurrentOptions();
        options.add(option);
        saveCurrentOptions(options);
    }


    /**
     * Save set of options to preferences
     * @param optionsSet
     */
    public void saveOptions(HashMap<String, ArrayList<OptionStore> > optionsSet){
        JSONArray jArrayAll = new JSONArray();
        JSONObject jsonEntry;
        Set<HashMap.Entry<String, ArrayList<OptionStore> >> optSet =  optionsSet.entrySet();
        HashMap.Entry<String, ArrayList<OptionStore> > entry;
        for(HashMap.Entry<String, ArrayList<OptionStore> > currentOption: optSet){
            jsonEntry = new JSONObject();
            try {
                jsonEntry.put(OPTION_SAVE_NAME, 	currentOption.getKey());                                // String
                jsonEntry.put(OPTION_SAVE_SET, 	    new JSONArray(currentOptionsToJSONString(currentOption.getValue())));  // JSON array
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jArrayAll.put(jsonEntry);
        }

        String optionsJSONString = jArrayAll.toString();
        if(!TextUtils.isEmpty(optionsJSONString)){
            mPrefsEditor.putString(OPTIONS_JSON, optionsJSONString);
            mPrefsEditor.commit();
        }
    }


    /**
     * Load options set from preferences
     * @return
     */
    public HashMap<String, ArrayList<OptionStore> > loadOptions(){
        HashMap<String, ArrayList<OptionStore> > options = new HashMap<String, ArrayList<OptionStore> >();
        JSONArray jArrayAll;
        String optionsJsonStr = mSharedPrefs.getString(OPTIONS_JSON, null);
        if(optionsJsonStr != null){
            try {
                jArrayAll = new JSONArray(optionsJsonStr);
                int arrayLength = jArrayAll.length();
                JSONObject entryElement;
                String optionSaveName;
                JSONArray jArrayCurrentAll;
                for(int i = 0; i < arrayLength; i++){
                    entryElement =  (JSONObject) jArrayAll.get(i);
                    optionSaveName = entryElement.getString(OPTION_SAVE_NAME);
                    jArrayCurrentAll = entryElement.getJSONArray(OPTION_SAVE_SET);
                    options.put(optionSaveName, parseCurrentOptions(jArrayCurrentAll));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                options = new HashMap<String, ArrayList<OptionStore> >(0);
            }
        }

        return options;
    }


    /**
     * Store current options to preference in order to load it further
     * @param name save name
     * @return true if success, false otherwise (if name already exist)
     */
    public boolean storeCurrentOptions(String name){
        HashMap<String, ArrayList<OptionStore> > optionsMap = loadOptions();
        ArrayList<OptionStore> options = optionsMap.get(name);

        if(options != null){
            // There is options named like that
            return false;
        }else{
            optionsMap.put(name, getCurrentOptions());
            saveOptions(optionsMap);
            return true;
        }
    }
    /**
     * remove given options set from storage
     * @param name options set to remove from storage
     * @return true if success, false otherwise (no option found)
     */
    public boolean removeFromOptionsStorage(String name){
        boolean result = false;
        HashMap<String, ArrayList<OptionStore> > optionsMap = loadOptions();

        result = (optionsMap.remove(name) != null);
        if(result){
            saveOptions(optionsMap);
        }

        return result;
    }

    /**
     * Update current options named "name"
     * @param name name of options to update
     * @return true if success, false otherwise (if no entry to update found)
     */
    public boolean updateCurentOptionsStorage(String name){
        HashMap<String, ArrayList<OptionStore> > optionsMap = loadOptions();
        ArrayList<OptionStore> options = optionsMap.get(name);

        if(options != null){
            optionsMap.put(name, getCurrentOptions());
            saveOptions(optionsMap);
            return true;
        }else{
            // There is options named like that
            return false;
        }
    }

    /**
     * Restore current (visible) options from preferences by save name
     * @param name save name
     */
    public void restoreCurrentOptions(String name){
        HashMap<String, ArrayList<OptionStore> > optionsMap = loadOptions();
        ArrayList<OptionStore> options = optionsMap.get(name);
        if(options != null){
            saveCurrentOptions(options);
        }
    }


}
