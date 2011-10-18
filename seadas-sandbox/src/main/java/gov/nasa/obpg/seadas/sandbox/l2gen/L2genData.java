package gov.nasa.obpg.seadas.sandbox.l2gen;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genData {

    public final String SPIXL = "spixl";
    public final String EPIXL = "epixl";
    public final String DPIXL = "dpixl";
    public final String SLINE = "sline";
    public final String ELINE = "eline";
    public final String DLINE = "dline";
    public final String NORTH = "north";
    public final String SOUTH = "south";
    public final String WEST = "west";
    public final String EAST = "east";
    public final String IFILE = "ifile";
    public final String OFILE = "ofile";
    public final String PROD = "l2prod";

    public final String PARFILE_TEXT_CHANGE_EVENT_NAME = "PARFILE_TEXT_CHANGE_EVENT_NAME";
    public final String MISSION_STRING_CHANGE_EVENT_NAME = "MISSION_STRING_CHANGE_EVENT_NAME";
    public final String UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT = "UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT";
    public final String WAVE_DEPENDENT_PRODUCT_CHANGED = "WAVE_DEPENDENT_PRODUCT_CHANGED";
    public final String WAVE_INDEPENDENT_PRODUCT_CHANGED = "WAVE_INDEPENDENT_PRODUCT_CHANGED";
    public final String DEFAULTS_CHANGED_EVENT = "DEFAULTS_CHANGED_EVENT";

    // Groupings of Parameter Keys
    private final String[] coordinateParamKeys = {NORTH, SOUTH, WEST, EAST};
    private final String[] pixelLineParamKeys = {SPIXL, EPIXL, DPIXL, SLINE, ELINE, DLINE};
    private final String[] fileIOParamKeys = {IFILE, OFILE};
    private final String[] remainingGUIParamKeys = {};

    private L2genReader l2genReader = new L2genReader(this);
    private String OCDATAROOT = System.getenv("OCDATAROOT");
    private HashMap<String, String> parfileHashMap = new HashMap();
    private HashMap<String, String> defaultParfileHashMap = new HashMap();
    private TreeSet<String> l2prodlist = new TreeSet<String>();
    private ArrayList<ProductInfo> waveIndependentProductInfoArray = new ArrayList<ProductInfo>();
    private ArrayList<ProductInfo> waveDependentProductInfoArray = new ArrayList<ProductInfo>();

    private ArrayList<WavelengthInfo> wavelengthLimiterArray = new ArrayList<WavelengthInfo>();


    private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);


    public enum RegionType {Coordinates, PixelLines}


    public EventInfo[] eventInfos = {
            new EventInfo(WAVE_DEPENDENT_PRODUCT_CHANGED, this),
            new EventInfo(WAVE_INDEPENDENT_PRODUCT_CHANGED, this)
    };


    public L2genData() {

    }


    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        boolean found = false;

        for (EventInfo eventInfo : eventInfos) {
            if (propertyName.equals(eventInfo.getName())) {
                eventInfo.addPropertyChangeListener(listener);
                found = true;
            }
        }

        if (!found) {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        boolean found = false;

        for (EventInfo eventInfo : eventInfos) {
            if (propertyName.equals(eventInfo.getName())) {
                eventInfo.removePropertyChangeListener(listener);
                found = true;
            }
        }

        if (!found) {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }
    }


    public void disableEvent(String eventName) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {
                System.out.println("Disabling event" + eventName);
                eventInfo.setEnabled(false);
            }
        }
    }

    public void enableEvent(String eventName) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {

                System.out.println("Enabling event" + eventName);
                eventInfo.setEnabled(true);
            }
        }
    }

    private void fireEvent(String eventName) {
        fireEvent(eventName, null, null);
    }


    private void fireEvent(String eventName, Object oldValue, Object newValue) {
        for (EventInfo eventInfo : eventInfos) {
            if (eventName.equals(eventInfo.toString())) {
                System.out.println("Firing event" + eventName);
                eventInfo.fireEvent(oldValue, newValue);
            }
        }
    }


    public void setSelectedWaveDependentProduct(WavelengthInfo inWavelengthInfo, boolean isSelected) {
        for (ProductInfo productInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                for (WavelengthInfo wavelengthInfo : algorithmInfo.getWavelengthInfoArray()) {
                    if (inWavelengthInfo == wavelengthInfo) {
                        if (wavelengthInfo.isSelected() != isSelected) {
                            wavelengthInfo.setSelected(isSelected);
                            fireEvent(WAVE_DEPENDENT_PRODUCT_CHANGED);
                            return;
                        }
                    }
                }
            }
        }
    }


    public void setSelectedWaveIndependentProduct(AlgorithmInfo inAlgorithmInfo, boolean isSelected) {

        for (ProductInfo productInfo : waveIndependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                if (inAlgorithmInfo == algorithmInfo) {
                    if (algorithmInfo.isSelected() != isSelected) {
                        algorithmInfo.setSelected(isSelected);
                        fireEvent(WAVE_INDEPENDENT_PRODUCT_CHANGED);
                    }
                }
            }
        }
    }


    public String getProdlist() {
        StringBuilder prodlist = new StringBuilder("");

        for (ProductInfo productInfo : waveIndependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                if (algorithmInfo.isSelected()) {
                    StringBuilder product = new StringBuilder();

                    product.append(productInfo.getName());

                    if (algorithmInfo.getName() != null) {
                        product.append("_");
                        product.append(algorithmInfo.getName());
                    }

                    if (prodlist.length() > 0) {
                        prodlist.append(" ");
                    }

                    prodlist.append(product);
                }
            }
        }

        for (ProductInfo productInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                for (WavelengthInfo wavelengthInfo : algorithmInfo.getWavelengthInfoArray()) {
                    if (wavelengthInfo.isSelected()) {
                        StringBuilder product = new StringBuilder();

                        product.append(productInfo.getName());
                        product.append("_");
                        product.append(wavelengthInfo.getWavelengthString());

                        if (algorithmInfo.getName() != null) {
                            product.append("_");
                            product.append(algorithmInfo.getName());
                        }

                        if (prodlist.length() > 0) {
                            prodlist.append(" ");
                        }

                        prodlist.append(product.toString());
                    }
                }
            }
        }

        return prodlist.toString();
    }


    public void setIsSelectedWavelengthInfoArray(String wavelength, boolean isSelected) {

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelength.equals(wavelengthInfo.getWavelengthString())) {
                System.out.println(wavelength + ":" + wavelengthInfo.isSelected() + ":" + isSelected);
                if (isSelected != wavelengthInfo.isSelected()) {
                    wavelengthInfo.setSelected(isSelected);
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT, null, null));
                }
            }
        }


    }


    public boolean isSelectedWavelengthTypeIii() {

        boolean infraredNotSelectedFound = false;

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isIR() && !wavelengthInfo.isSelected()) {
                infraredNotSelectedFound = true;
            }
        }

        if (infraredNotSelectedFound) {
            return false;
        } else {
            return true;
        }
    }


    public void setSelectedWavelengthTypeIii(boolean selectedWavelengthTypeIii) {

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isIR()) {
                wavelengthInfo.setSelected(selectedWavelengthTypeIii);
            }
        }

        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT, null, null));

    }

    public boolean isSelectedWavelengthTypeVvv() {
        boolean visibleNotSelectedFound = false;

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isVisible() && !wavelengthInfo.isSelected()) {
                visibleNotSelectedFound = true;
            }
        }

        if (visibleNotSelectedFound) {
            return false;
        } else {
            return true;
        }
    }

    public void setSelectedWavelengthTypeVvv(boolean selectedWavelengthTypeVvv) {

        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
            if (wavelengthInfo.isVisible()) {
                wavelengthInfo.setSelected(selectedWavelengthTypeVvv);
            }
        }

        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, UPDATE_WAVELENGTH_CHECKBOX_STATES_EVENT, null, null));
    }


    public void addWaveIndependentProductInfoArray(ProductInfo productInfo) {
        waveIndependentProductInfoArray.add(productInfo);
    }

    public void addWaveDependentProductInfoArray(ProductInfo productInfo) {
        waveDependentProductInfoArray.add(productInfo);
    }

    public void clearWaveIndependentProductInfoArray() {
        waveIndependentProductInfoArray.clear();
    }

    public void clearWaveDependentProductInfoArray() {
        waveDependentProductInfoArray.clear();
    }

    public void sortWaveDependentProductInfoArray(Comparator<ProductInfo> comparator) {
        Collections.sort(waveDependentProductInfoArray, comparator);
    }

    public void sortWaveIndependentProductInfoArray(Comparator<ProductInfo> comparator) {
        Collections.sort(waveIndependentProductInfoArray, comparator);
    }

    public ArrayList<ProductInfo> getWaveIndependentProductInfoArray() {
        return waveIndependentProductInfoArray;
    }

    public ArrayList<ProductInfo> getWaveDependentProductInfoArray() {
        return waveDependentProductInfoArray;
    }


    public ArrayList<WavelengthInfo> getWavelengthLimiterArray() {
        return wavelengthLimiterArray;
    }


    private void specifyRegionType(String paramKey) {

        //---------------------------------------------------------------------------------------
        // Determine the regionType for paramKey
        //---------------------------------------------------------------------------------------
        RegionType regionType = null;

        // Look for paramKey in coordinateParamKeys
        for (String currKey : coordinateParamKeys) {
            if (currKey.equals(paramKey)) {
                regionType = RegionType.Coordinates;
            }
        }

        // Look for paramKey in pixelLineParamKeys
        if (regionType == null) {
            for (String currKey : pixelLineParamKeys) {
                if (currKey.equals(paramKey)) {
                    regionType = RegionType.PixelLines;
                }
            }
        }

        //---------------------------------------------------------------------------------------
        // Perform actions based on the regionType:
        // - if Coordinates are being used purge PixelLine fields
        // - if PixelLines are being used purge Coordinate fields
        //---------------------------------------------------------------------------------------
        if (regionType == RegionType.Coordinates) {
            // Since Coordinates are being used purge PixelLine fields
            for (String currKey : pixelLineParamKeys) {
                parfileHashMap.remove(currKey);
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(

                        this, currKey, null, ""));

            }

        } else if (regionType == RegionType.PixelLines) {
            // Since PixelLines are being used purge Coordinate fields

            for (String currKey : coordinateParamKeys) {
                parfileHashMap.remove(currKey);
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(

                        this, currKey, null, ""));

            }

        }
    }


    //    For any given paramValue assemble it formatted parfile 'name=value' entry
    //    Do not make an entry if it matches the default value

    private void makeParfileKeyValueEntry(StringBuilder stringBuilder, String currKey) {

        boolean makeEntry = false;


        if (parfileHashMap.containsKey(currKey)) {
            if (defaultParfileHashMap.containsKey(currKey)) {
                if (!defaultParfileHashMap.get(currKey).equals(parfileHashMap.get(currKey))) {
                    makeEntry = true;
                    debug("LATER currKey=" + currKey + ":main=" + parfileHashMap.get(currKey) + ":default=" + defaultParfileHashMap.get(currKey) + ":makeEntry=" + makeEntry);
                }
            } else {
                makeEntry = true;
            }
        }

        //      debug("LATER currKey="+currKey+":main="+paramValueHashMap.get(currKey)+":default="+defaultParamValueHashMap.get(currKey)+":makeEntry="+makeEntry);

        if (makeEntry == true) {
            stringBuilder.append(currKey);
            stringBuilder.append("=");
            stringBuilder.append(parfileHashMap.get(currKey));
            stringBuilder.append("\n");
        }
    }


    private String makeRequiredParfileKeyValueEntry(String name, String value) {

        StringBuilder stringBuilder = new StringBuilder("");


        stringBuilder.append(name);
        stringBuilder.append("=");
        stringBuilder.append(value);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }


    public String getParfile() {

        // Remove prod entry because we do not store it here.
        // This is just a precaution it should never have been stored.
        if (parfileHashMap.containsKey(PROD)) {
            parfileHashMap.remove(PROD);
        }

        HashMap<String, Object> paramValueCopyHashMap = new HashMap();

        for (String currKey : parfileHashMap.keySet()) {
            paramValueCopyHashMap.put(currKey, parfileHashMap.get(currKey));
        }

        StringBuilder parfileStringBuilder = new StringBuilder("");


        StringBuilder fileIOBlockStringBuilder = new StringBuilder("");

        for (String currKey : fileIOParamKeys) {
            makeParfileKeyValueEntry(fileIOBlockStringBuilder, currKey);
            paramValueCopyHashMap.remove(currKey);
        }

        if (fileIOBlockStringBuilder.length() > 0) {
            parfileStringBuilder.append("# FILE IO PARAMS\n");
            parfileStringBuilder.append(fileIOBlockStringBuilder.toString());
        }


        String productEntry = makeRequiredParfileKeyValueEntry(PROD, getProdlist());
        parfileStringBuilder.append("# PRODUCT PARAM\n");
        parfileStringBuilder.append(productEntry);


        StringBuilder coordinateBlockStringBuilder = new StringBuilder("");

        for (String currKey : coordinateParamKeys) {
            makeParfileKeyValueEntry(coordinateBlockStringBuilder, currKey);

            paramValueCopyHashMap.remove(currKey);
        }


        for (String currKey : pixelLineParamKeys) {
            makeParfileKeyValueEntry(coordinateBlockStringBuilder, currKey);
            paramValueCopyHashMap.remove(currKey);
        }

        if (coordinateBlockStringBuilder.length() > 0) {
            parfileStringBuilder.append("# COORDINATE PARAMS\n");
            parfileStringBuilder.append(coordinateBlockStringBuilder.toString());
        }


        StringBuilder userBlockStringBuilder = new StringBuilder("");

        for (String currKey : paramValueCopyHashMap.keySet()) {
            makeParfileKeyValueEntry(userBlockStringBuilder, currKey);
        }

        if (userBlockStringBuilder.length() > 0) {
            parfileStringBuilder.append("# ADDITIONAL USER PARAMS\n");
            parfileStringBuilder.append(userBlockStringBuilder.toString());
        }

        return parfileStringBuilder.toString();
    }


    private HashMap<String, String> parseParfile(String parfileString) {

        HashMap<String, String> thisParfileHashMap = null;

        if (parfileString != null) {

            thisParfileHashMap = new HashMap<String, String>();

            String parfileLines[] = parfileString.split("\n");

            for (String parfileLine : parfileLines) {

                // skip the comment lines in file
                if (!parfileLine.trim().startsWith("#")) {

                    String splitLine[] = parfileLine.split("=");
                    if (splitLine.length == 2) {

                        final String key = splitLine[0].toString().trim();
                        final String value = splitLine[1].toString().trim();
                        thisParfileHashMap.put(key, value);
                    }
                }
            }
        }

        return thisParfileHashMap;
    }


    public void setParfile(String inParfile) {

        HashMap<String, String> inParfileHashMap = parseParfile(inParfile);

        if (inParfileHashMap != null && inParfileHashMap.size() > 0) {

            HashMap<String, String> tmpParamValueHashMap = new HashMap<String, String>();
            HashMap<String, String> copyParamValueHashMap = new HashMap<String, String>();

            for (String key : parfileHashMap.keySet()) {
                copyParamValueHashMap.put(key, parfileHashMap.get(key));
            }

            // Initialize tmpParamValueHashMap with defaultParamValueHashMap
            for (String key : defaultParfileHashMap.keySet()) {
                tmpParamValueHashMap.put(key, defaultParfileHashMap.get(key));
            }

            // Update  tmpParamValueHashMap  with  inParfileHashMap.
            for (String key : inParfileHashMap.keySet()) {
                tmpParamValueHashMap.put(key, inParfileHashMap.get(key));
            }

            // Remove any keys in paramValueHashMap which are not in tmpParamValueHashMap
            for (String key : copyParamValueHashMap.keySet()) {

                if (!key.equals(IFILE) && !tmpParamValueHashMap.containsKey(key)) {
                    deleteParam(key);
                }
            }

            // Do ifile first
            if (tmpParamValueHashMap.containsKey(IFILE)) {
                setParamValue(IFILE, tmpParamValueHashMap.get(IFILE));

                tmpParamValueHashMap.remove(IFILE);
            }

            for (String key : tmpParamValueHashMap.keySet()) {
                setParamValue(key, tmpParamValueHashMap.get(key));
            }
        }
    }


    public void setDefaultParfile(String defaultParfile) {

        HashMap<String, String> defaultParfileHashMap = parseParfile(defaultParfile);

        if (defaultParfileHashMap != null && defaultParfileHashMap.size() > 0) {
            for (String key : defaultParfileHashMap.keySet()) {
                setDefaultParamValue(key, defaultParfileHashMap.get(key));
            }
        }
    }


    public void applyProductDefaults() {

        boolean waveIndependentProductInfoChanged = false;

        for (ProductInfo productInfo : waveIndependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                if (algorithmInfo.isSelected() != algorithmInfo.isDefaultSelected()) {
                    algorithmInfo.setSelected(algorithmInfo.isDefaultSelected());
                    waveIndependentProductInfoChanged = true;
                }
            }
        }

        boolean waveDependentProductInfoChanged = false;


        for (ProductInfo productInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                for (WavelengthInfo wavelengthInfo : algorithmInfo.getWavelengthInfoArray()) {
                    if (wavelengthInfo.isSelected() != wavelengthInfo.isDefaultSelected()) {
                        wavelengthInfo.setSelected(wavelengthInfo.isDefaultSelected());
                        waveDependentProductInfoChanged = true;
                    }
                }
            }
        }


        if (waveDependentProductInfoChanged == true) {
            fireEvent(WAVE_DEPENDENT_PRODUCT_CHANGED);
        }

        if (waveIndependentProductInfoChanged == true) {
            fireEvent(WAVE_INDEPENDENT_PRODUCT_CHANGED);
        }
    }

    public void applyParfileDefaults() {

        HashMap<String, String> copyParamValueHashMap = new HashMap<String, String>();

        for (String key : parfileHashMap.keySet()) {
            copyParamValueHashMap.put(key, parfileHashMap.get(key));
        }

        // remove any params which are not in defaultParamValueHashMap
        for (String key : copyParamValueHashMap.keySet()) {
            if (!key.equals(IFILE) && !defaultParfileHashMap.containsKey(key)) {
                deleteParam(key);
            }
        }

        // Set all  paramValueHashMap with  defaultParamValueHashMap
        for (String key : defaultParfileHashMap.keySet()) {
            setParamValue(key, defaultParfileHashMap.get(key));
        }
    }






    public String getParamValue(String key) {
        if (key != null && parfileHashMap.get(key) != null) {
            if (key.equals(PROD)) {
                return getProdlist();
            } else {
                return parfileHashMap.get(key);
            }
        } else {
            return "";
        }
    }

    public void deleteParam(String inKey) {

        if (inKey != null && inKey.length() > 0) {
            inKey = inKey.trim();
            if (parfileHashMap.containsKey(inKey)) {
                if (defaultParfileHashMap.containsKey(inKey)) {
                    parfileHashMap.put(inKey, defaultParfileHashMap.get(inKey));
                } else {
                    parfileHashMap.remove(inKey);
                }

                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, inKey, null, null));
            }
        }
    }

    public void setParamValue(String inKey, String inValue) {

        if (inKey != null && inKey.length() > 0) {
            inKey = inKey.trim();

            if (inKey.equals(PROD)) {
                handleProdKeyChange(inValue, false);
            } else {
                if (inValue != null && inValue.length() > 0) {
                    inValue = inValue.trim();
                    debug("new Param" + inKey + "=" + inValue);

                    if (!inValue.equals(parfileHashMap.get(inKey))) {
                        if (inKey.equals(IFILE)) {
                            handleIfileChange(inValue);
                        } else {
                            parfileHashMap.put(inKey, inValue);
                            specifyRegionType(inKey);
                        }

                        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, inKey, null, null));
                    }
                } else {
                    deleteParam(inKey);
                }
            }
        }
    }


    public void setDefaultParamValue(String inKey, String inValue) {

        if (inKey != null && inKey.length() > 0) {
            inKey = inKey.trim();

            boolean defaultsChanged = false;

            if (inKey.equals(PROD)) {
                debug("PROD="+inValue);
                handleProdKeyChange(inValue, true);
            } else {
                if (inValue != null && inValue.length() > 0) {
                    inValue = inValue.trim();

                    if (!inValue.equals(defaultParfileHashMap.get(inKey))) {
                        defaultParfileHashMap.put(inKey, inValue);
                    }
                } else {
                    defaultParfileHashMap.remove(inKey);
                }
            }


            if (defaultsChanged) {
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, DEFAULTS_CHANGED_EVENT, null, null));
            }
        }
    }

    private boolean isValidL2prod(String singleL2prodEntry) {

        if (singleL2prodEntry != null) {
            for (ProductInfo productInfo : waveIndependentProductInfoArray) {
                for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                    if (singleL2prodEntry.equals(getProductNameForSingleEntry(productInfo, algorithmInfo))) {
                        return true;
                    }
                }
            }

            for (ProductInfo productInfo : waveDependentProductInfoArray) {
                for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                    for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
                        if (singleL2prodEntry.equals(getProductNameForSingleEntry(productInfo, algorithmInfo, wavelengthInfo))) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    private String getProductNameForSingleEntry(ProductInfo productInfo, AlgorithmInfo algorithmInfo, WavelengthInfo wavelengthInfo) {
        StringBuilder l2genProductName = new StringBuilder();

        l2genProductName.append(productInfo.getName());
        l2genProductName.append("_");
        l2genProductName.append(wavelengthInfo.getWavelengthString());

        if (algorithmInfo.getName() != null) {
            l2genProductName.append("_");
            l2genProductName.append(algorithmInfo.getName());
        }
        return l2genProductName.toString();
    }

    private String getProductNameForSingleEntry(ProductInfo productInfo, AlgorithmInfo algorithmInfo) {
        StringBuilder l2genProductName = new StringBuilder();

        l2genProductName.append(productInfo.getName());

        if (algorithmInfo.getName() != null) {
            l2genProductName.append("_");
            l2genProductName.append(algorithmInfo.getName());
        }
        return l2genProductName.toString();
    }


    private void handleProdKeyChange(String newProd, boolean setDefaults) {

        //----------------------------------------------------------------------------------------------------
        // Put newProd into newProdTreeSet
        //----------------------------------------------------------------------------------------------------

        TreeSet<String> newProdTreeSet = new TreeSet<String>();

        debug("newProd="+newProd);
        if (newProd != null) {
            for (String prodEntry : newProd.split(" ")) {
                prodEntry.trim();

                if (isValidL2prod(prodEntry)) {
                    newProdTreeSet.add(prodEntry);
                    debug("prodEntry="+prodEntry);
                }
            }
        }

        boolean waveDependentProductInfoArrayChanged = false;
        boolean waveIndependentProductInfoArrayChanged = false;
        boolean defaultsChanged = false;
        //----------------------------------------------------------------------------------------------------
        // For every product in waveDependentProductInfoArray set selected to agree with newProdTreeSet
        //----------------------------------------------------------------------------------------------------


        for (ProductInfo productInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                for (WavelengthInfo wavelengthInfo : algorithmInfo.getWavelengthInfoArray()) {
                    String currL2genDataProductName = getProductNameForSingleEntry(productInfo, algorithmInfo, wavelengthInfo);

                    if (newProdTreeSet.contains(currL2genDataProductName)) {
                        debug("TEMP default "+wavelengthInfo.toString());
                        if (setDefaults == true) {
                            if (!wavelengthInfo.isDefaultSelected()) {
                                wavelengthInfo.setDefaultSelected(true);
                                debug("setting default "+wavelengthInfo.toString());
                                defaultsChanged = true;
                            }
                        } else {
                            if (!wavelengthInfo.isSelected()) {
                                wavelengthInfo.setSelected(true);
                                waveDependentProductInfoArrayChanged = true;
                            }
                        }

                        newProdTreeSet.remove(currL2genDataProductName);
                    } else {
                        if (setDefaults == true) {
                            if (wavelengthInfo.isDefaultSelected()) {
                                wavelengthInfo.setDefaultSelected(false);
                                debug("setting default "+wavelengthInfo.toString());
                                defaultsChanged = true;
                            }
                        } else {
                            if (wavelengthInfo.isSelected()) {
                                wavelengthInfo.setSelected(false);
                                waveDependentProductInfoArrayChanged = true;
                            }
                        }
                    }
                }
            }
        }


        //----------------------------------------------------------------------------------------------------
        // For every product in waveIndependentProductInfoArray set selected to agree with newProdTreeSet
        //----------------------------------------------------------------------------------------------------


        for (ProductInfo productInfo : waveIndependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                String currL2genDataProductName = getProductNameForSingleEntry(productInfo, algorithmInfo);

                if (newProdTreeSet.contains(currL2genDataProductName)) {
                    if (setDefaults == true) {
                        if (!algorithmInfo.isDefaultSelected()) {
                            algorithmInfo.setDefaultSelected(true);
                            debug("setting default "+algorithmInfo.toString());
                            defaultsChanged = true;
                        }

                    } else {
                        if (!algorithmInfo.isSelected()) {
                            algorithmInfo.setSelected(true);
                            waveIndependentProductInfoArrayChanged = true;
                        }
                    }

                    newProdTreeSet.remove(currL2genDataProductName);
                } else {
                    if (setDefaults == true) {
                        if (algorithmInfo.isSelected()) {
                            algorithmInfo.setSelected(false);
                            debug("setting default "+algorithmInfo.toString());
                            defaultsChanged = true;
                        }
                    } else {
                        if (algorithmInfo.isDefaultSelected()) {
                            algorithmInfo.setDefaultSelected(false);
                            waveIndependentProductInfoArrayChanged = true;
                        }

                    }
                }
            }
        }

        debug("prod=" + getProdlist());
        if (setDefaults == true) {
            if (waveDependentProductInfoArrayChanged) {
                debug("waveDependentProductInfoArrayChanged");
                fireEvent(WAVE_DEPENDENT_PRODUCT_CHANGED);
            }

            if (waveIndependentProductInfoArrayChanged) {
                debug("waveIndependentProductInfoArrayChanged");
                fireEvent(WAVE_INDEPENDENT_PRODUCT_CHANGED);
            }
        } else {
            if (defaultsChanged) {
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, DEFAULTS_CHANGED_EVENT, null, null));
            }
        }
    }

    public String getMissionString() {

        String missionString = "";

        if (parfileHashMap.containsKey(IFILE) && parfileHashMap.get(IFILE) != null) {
            File file = new File(parfileHashMap.get(IFILE));

            if (file != null && file.getName() != null) {
                missionString = file.getName().substring(0, 1);
            }
        }

        return missionString;
    }


    private String getSensorInfoFilename() {

        // lookup hash relating mission letter with mission directory name
        final HashMap<String, String> missionDirectoryNameHashMap = new HashMap();
        missionDirectoryNameHashMap.put("S", "seawifs");
        missionDirectoryNameHashMap.put("A", "modisa");
        missionDirectoryNameHashMap.put("T", "modist");

        String missionDirectoryName = missionDirectoryNameHashMap.get(getMissionString());

        // determine the filename which contains the wavelengths
        final StringBuilder sensorInfoFilenameStringBuilder = new StringBuilder("");
        sensorInfoFilenameStringBuilder.append(OCDATAROOT);
        sensorInfoFilenameStringBuilder.append("/");
        sensorInfoFilenameStringBuilder.append(missionDirectoryName);
        sensorInfoFilenameStringBuilder.append("/");
        sensorInfoFilenameStringBuilder.append("msl12_sensor_info.dat");

        return sensorInfoFilenameStringBuilder.toString();
    }

    // runs this if IFILE changes
    // it will reset missionString
    // it will reset and make new wavelengthInfoArray
    private void handleIfileChange(String newIfile) {

        String previousMissionString = getMissionString();
        parfileHashMap.put(IFILE, newIfile);

        debug("new missionString=" + getMissionString());
        if (getMissionString() != null) {
            if (previousMissionString == null || !previousMissionString.equals(getMissionString())) {

                wavelengthLimiterArray.clear();

                // determine the filename which contains the wavelengths
                String sensorInfoFilename = getSensorInfoFilename();

                // read in the mission's datafile which contains the wavelengths
                //  final ArrayList<String> SensorInfoArrayList = myReadDataFile(sensorInfoFilename.toString());
                final ArrayList<String> SensorInfoArrayList = l2genReader.readFileIntoArrayList(sensorInfoFilename);
                debug("sensorInfoFilename=" + sensorInfoFilename);


                // loop through datafile
                for (String myLine : SensorInfoArrayList) {

                    // skip the comment lines in file
                    if (!myLine.trim().startsWith("#")) {

                        // just look at value pairs of the form Lambda(#) = #
                        String splitLine[] = myLine.split("=");
                        if (splitLine.length == 2 &&
                                splitLine[0].trim().startsWith("Lambda(") &&
                                splitLine[0].trim().endsWith(")")
                                ) {

                            // get current wavelength and add into in a JCheckBox
                            final String currWavelength = splitLine[1].trim();

                            WavelengthInfo wavelengthInfo = new WavelengthInfo(currWavelength);
                            wavelengthLimiterArray.add(wavelengthInfo);
                            debug("wavelengthLimiterArray adding wave=" + wavelengthInfo.getWavelengthString());
                        }
                    }
                }

                debug("resetWavelengthInfosInWaveDependentProductInfoArray");
                resetWavelengthInfosInWaveDependentProductInfoArray();


                String defaultsParfile = l2genReader.readFileIntoString(getDefaultsFilename());

                setDefaultParfile(defaultsParfile);
                applyParfileDefaults();

                debug(MISSION_STRING_CHANGE_EVENT_NAME.toString() + "being fired");
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, MISSION_STRING_CHANGE_EVENT_NAME, null, getMissionString()));
            }
        }
    }


    private String getDefaultsFilename() {
        String SEADAS_SEAWIFS_DEFAULTS_FILENAME = "/home/knowles/SeaDAS/seadas/seadas-sandbox/SampleParFile.txt";
        String SEADAS_AQUA_DEFAULTS_FILENAME = "/home/knowles/SeaDAS/seadas/seadas-sandbox/SampleParFile.txt";
        String SEADAS_TERRA_DEFAULTS_FILENAME = "/home/knowles/SeaDAS/seadas/seadas-sandbox/SampleParFile.txt";

        // todo add logic to get correct defaults file

        return SEADAS_AQUA_DEFAULTS_FILENAME;
    }

    private void debug(String string) {
        System.out.println(string);
    }

    public void resetWavelengthInfosInWaveDependentProductInfoArray() {
        for (ProductInfo productInfo : waveDependentProductInfoArray) {
            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
                algorithmInfo.clearWavelengthInfoArray();

                for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {

                    if (wavelengthInfo.getWavelength() < WavelengthInfo.VISIBLE_UPPER_LIMIT) {
                        if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.VISIBLE ||
                                algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                            WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                            newWavelengthInfo.setAlgorithmInfo(algorithmInfo);
                            algorithmInfo.addWavelengthInfoArray(newWavelengthInfo);
                        }
                    } else {
                        if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.IR ||
                                algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                            WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                            newWavelengthInfo.setAlgorithmInfo(algorithmInfo);
                            algorithmInfo.addWavelengthInfoArray(newWavelengthInfo);
                        }
                    }
                }
            }
        }
    }


    //  The below lines are not currently in use

//
//    private ArrayList<String> myReadDataFile(String fileName) {
//        String lineData;
//        ArrayList<String> fileContents = new ArrayList<String>();
//        BufferedReader moFile = null;
//        try {
//            moFile = new BufferedReader(new FileReader(new File(fileName)));
//            while ((lineData = moFile.readLine()) != null) {
//
//                fileContents.add(lineData);
//            }
//        } catch (IOException e) {
//            ;
//        } finally {
//            try {
//                moFile.close();
//            } catch (Exception e) {
//                //Ignore
//            }
//        }
//        return fileContents;
//    }
//
//
//    public String getMissionString() {
//        return missionString;
//    }
//
//    public void setMissionString(String missionString) {
//        if (!missionString.equals(this.missionString)) {
//            String oldValue = this.missionString;
//            this.missionString = missionString;
//            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, MISSION_STRING_CHANGE_EVENT_NAME, oldValue, missionString));
//        }
//    }
//
//
//    // apply all entries in l2prodHash to wavelengthInfoArray
//    private void setIsSelectedWavelengthInfoArrayWithProdHash() {
//
//
//        for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
//            wavelengthInfo.setSelected(false);
//        }
//
//
//        for (String l2prodEntry : l2prodlist) {
//
//            WavelengthInfo wavelengthInfo = getWavelengthFromL2prod(l2prodEntry);
//
//            if (wavelengthInfo != null) {
//
//                for (WavelengthInfo currwavelengthInfo : wavelengthLimiterArray) {
//                    if (currwavelengthInfo.getWavelength() == wavelengthInfo.getWavelength()) {
//                        wavelengthInfo.setSelected(true);
//                    }
//                }
//            }
//        }
//    }
//
//
//    private AlgorithmInfo getAlgorithmInfoFromL2prod(String L2prodEntry) {
//
//        for (ProductInfo currProductInfo : waveIndependentProductInfoArray) {
//            for (AlgorithmInfo currAlgorithmInfo : currProductInfo.getAlgorithmInfoArrayList()) {
//
//                String algorithm = currAlgorithmInfo.getName();
//                String product = currAlgorithmInfo.getProductName();
//
//                if (L2prodEntry.equals(assembleL2productName(product, null, algorithm))) {
//                    return currAlgorithmInfo;
//                }
//            }
//        }
//
//
//        for (ProductInfo currProductInfo : waveDependentProductInfoArray) {
//            for (AlgorithmInfo currAlgorithmInfo : currProductInfo.getAlgorithmInfoArrayList()) {
//
//                String algorithm = currAlgorithmInfo.getName();
//                String product = currAlgorithmInfo.getProductName();
//
//                for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
//
//                    String wavelength = wavelengthInfo.toString();
//
//                    if (L2prodEntry.equals(assembleL2productName(product, wavelength, algorithm))) {
//                        return currAlgorithmInfo;
//                    }
//                }
//            }
//        }
//
//        return null;
//    }
//
//
//    private void setIsSelectedWaveIndependentProductInfoArrayWithProdHash() {
//
//
//        for (ProductInfo productInfo : waveIndependentProductInfoArray) {
//            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
//                algorithmInfo.setSelected(false);
//            }
//        }
//
//
//        for (String l2prodEntry : l2prodlist) {
//            AlgorithmInfo algorithmInfo = getAlgorithmInfoFromL2prod(l2prodEntry);
//            if (algorithmInfo != null) {
//                algorithmInfo.setSelected(true);
//            } else {
//                // todo do something with this like send to log file or something
//            }
//        }
//    }
//
//
//    private void setIsSelectedWaveDependentProductInfoArrayWithProdHash() {
//
//
//        for (ProductInfo productInfo : waveDependentProductInfoArray) {
//            for (AlgorithmInfo algorithmInfo : productInfo.getAlgorithmInfoArrayList()) {
//                algorithmInfo.setSelected(false);
//            }
//        }
//
//
//        for (String l2prodEntry : l2prodlist) {
//            AlgorithmInfo algorithmInfo = getAlgorithmInfoFromL2prod(l2prodEntry);
//            if (algorithmInfo != null) {
//                algorithmInfo.setSelected(true);
//            } else {
//                // todo do something with this like send to log file or something
//            }
//        }
//    }
//
//
//    private void setProdWithProdlist() {
//
//        StringBuilder newProdList = new StringBuilder();
//
//        for (String prodEntry : l2prodlist) {
//            newProdList.append(prodEntry);
//            newProdList.append(" ");
//        }
//        paramValueHashMap.put(PROD, newProdList.toString().trim());
//    }
//
//
//    private WavelengthInfo getWavelengthFromL2prod(String L2prodEntry) {
//        for (ProductInfo currProductInfo : waveDependentProductInfoArray) {
//            for (AlgorithmInfo currAlgorithmInfo : currProductInfo.getAlgorithmInfoArrayList()) {
//
//                String algorithm = currAlgorithmInfo.getName();
//                String product = currAlgorithmInfo.getProductName();
//
//                for (WavelengthInfo wavelengthInfo : wavelengthLimiterArray) {
//
//                    String wavelength = wavelengthInfo.toString();
//
//
//                    if (L2prodEntry.equals(assembleL2productName(product, wavelength, algorithm))) {
//                        return wavelengthInfo;
//                    }
//                }
//            }
//        }
//
//        return null;
//    }
//
//
//    private String assembleL2productName(String product, String wavelength, String algorithm) {
//
//        StringBuilder l2prod = new StringBuilder();
//
//        l2prod.append(product);
//
//        if (wavelength != null) {
//            l2prod.append("_");
//            l2prod.append(wavelength);
//        }
//
//        if (algorithm != null) {
//            l2prod.append("_");
//            l2prod.append(algorithm);
//        }
//
//        return l2prod.toString();
//    }

}


