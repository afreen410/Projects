import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 *
 * @author
 * KDD Group 1
 * Anchal Atlani
 * Afreen
 * Ankita Kumari
 * Rishika
 * Satish Kumar
 *
 *
 */
public class GenerateRules {


    private Map<ArrayList<String>, ArrayList<String>> actionRules =new HashMap<ArrayList<String>, ArrayList<String>>();
    private Map<ArrayList<String>, ArrayList<Integer>> ruleSuppMap= new HashMap<ArrayList<String>, ArrayList<Integer>>();


    public  int minSupport;
    public int minConfidence;
    public String decisionValueInitial;
    public String decisionValueEnd;
    private HashSet<String> stableAttribute;
    private HashSet<String> flexibleAttrSet;

    public ArrayList<String> attributeNames= new ArrayList<>();
    public HashMap<String, HashSet<String>> attributeMap= new HashMap<String, HashSet<String>>();

    public HashMap<String, ArrayList<String>> data= new HashMap<>();
    public HashMap<String, HashSet<String>> attributeValueRows= new HashMap<>();


    private LERSAlgo LERSAlgo;


    /**
     * @param attributeName
     * @return
     */
    public HashSet<String> getUniqueValues(String attributeName){
        HashSet<String> uniqueValues= null;
        if(attributeMap!=null && attributeMap.size()>0){
            HashSet<String> temp=attributeMap.get(attributeName);
            if(temp!=null && temp.size()>0){
                uniqueValues=temp;
                return uniqueValues;
            }
        }
        return uniqueValues;
    }

    /**
     * @param stableAttributes
     */
    public void setFlexibleAttributes(HashSet<String> stableAttributes)
    {
        this.stableAttribute=stableAttributes;
        flexibleAttrSet= new HashSet<>();
        for(String s:attributeNames){
            if(!stableAttributes.contains(s)){
                flexibleAttrSet.add(s);
            }
        }

    }

    /**
     * @param minSupport
     * @param confidence
     */
    public void setSupportAndConfidence(int minSupport, int confidence){
        this.minConfidence=minConfidence;
        this.minSupport=minSupport;
    }

    /**
     * @param decisionAttInitial
     * @param decisionAttEnd
     */
    public void setDecisionAttributes(String decisionAttInitial, String decisionAttEnd) {
        this.decisionValueInitial=decisionAttInitial;
        this.decisionValueEnd=decisionAttEnd;
    }


    /**
     * This method will read the data and attribute file
     * @param attributeFile
     * @param dataFile
     */
    public void readFile(File attributeFile, File dataFile) {
        System.out.println("Loading the Data");
        String line;
        int count = 0;
        String[] rowValues;

        HashSet<String> attSet = new HashSet<String>();

        try {
            BufferedReader reader = new BufferedReader((new FileReader(attributeFile)));
            while((line = reader.readLine()) != null) {
                for(String att : line.split(",|\t")) {
                    attributeNames.add(att);
                }
            }
        }catch(IOException e) {
            System.out.println("Exception occurred while loading attribute file");
            System.out.println(e.getMessage());
        }

        try {

            BufferedReader reader = new BufferedReader((new FileReader(dataFile)));

            while((line = reader.readLine()) != null) {
                count++;
                rowValues = line.split(",|\t");
                data.put("x" + Integer.toString(count), new ArrayList<String>(Arrays.asList(rowValues)));

                for(int i = 0; i < rowValues.length; i++) {

                    //Skipping if data is not present for that row.
                    if(rowValues[i].equals("?")) {
                        continue;
                    }
                    String currentValue = rowValues[i];
                    attSet = attributeMap.get(attributeNames.get(i));

                    if(attSet != null) {
                        attSet.add(currentValue);
                        attributeMap.put(attributeNames.get(i), attSet);

                    }else {
                        attSet = new HashSet<String>();
                        attSet.add(currentValue);
                        attributeMap.put(attributeNames.get(i), attSet);
                    }

                    String key = attributeNames.get(i) + currentValue;

                    HashSet<String> rows = new HashSet<>();
                    if(attributeValueRows.containsKey(key)) {
                        rows = attributeValueRows.get(key);
                    }
                    rows.add("x" + count);
                    attributeValueRows.put(key, rows);
                }
            }

        } catch(IOException e) {
            System.out.println("Exception occurred while reading the Data File");
            System.out.println(e.getMessage());
        }
    }



    /**
     * Find and add action rules using the certain rules found by performing LERS.
     */
    public void findActionRules() {

        Map<String, HashSet<HashSet<String>>> certainRules = LERSAlgo.getCertainRules();
        HashSet<HashSet<String>> toValueSets  = certainRules.get(decisionValueEnd);

        List<String> stableAtt;
        List<String> stableAttNames;

        if(toValueSets != null && !toValueSets.isEmpty()) {
            Iterator<HashSet<String>> toValIt = toValueSets.iterator();

            //Check all certain rule sets for the decision value
            while(toValIt.hasNext()) {
                stableAtt = new ArrayList<String>();
                List<String>  initialAndStableOccurences = new ArrayList<String>();

                HashSet<String> toValSet = toValIt.next();
                ArrayList<String> toAttNames = new ArrayList<String>();
                stableAttNames = new ArrayList<String>();
                stableAtt = getStableAttributes(toValSet);

                for(String attribute : stableAtt) {
                    stableAttNames.add(getAttributeName(attribute));
                }

                for(String attribute : toValSet) {
                    toAttNames.add(getAttributeName(attribute));
                }

                ArrayList<String> stableSupportSearch = new ArrayList<String>();
                stableSupportSearch.addAll(stableAtt);
                stableSupportSearch.add(decisionValueInitial);
                initialAndStableOccurences = findRows(stableSupportSearch);

                for(Map.Entry<String, HashSet<String>> entry : attributeMap.entrySet()) {

                    //skipping Decision Attributes and Stable Attributes
                    if(stableAttNames.contains(entry.getKey()) || entry.getKey().equals(getAttributeName(decisionValueEnd)))
                        continue;

                    for(String attVal : entry.getValue()) {
                        // Skipping if it is already present in the toValSet
                        if(toValSet.contains(entry.getKey() + attVal))
                            continue;
                        filterAndAddRules(stableAtt, entry.getKey(), attVal, initialAndStableOccurences, toAttNames, toValSet);

                    }
                }
            }
        }
    }


    /**
     *
     * Filtering and adding the rules
     * @param stableAtt
     * @param attrKey
     * @param attVal
     * @param initialAndStableValOccurences
     * @param toAttNames
     * @param toValSet
     */
    public void filterAndAddRules(List<String> stableAtt,String attrKey, String attVal,List<String>
            initialAndStableValOccurences, ArrayList<String> toAttNames,HashSet<String> toValSet){

        ArrayList<String> attributeTest = new ArrayList<String>();
        attributeTest.addAll(stableAtt);
        attributeTest.add(attrKey + attVal);
        List<String> attributeTestOccurrences = findRows(attributeTest);
        ArrayList<String> toActionSet;

        if(!attributeTestOccurrences.isEmpty() && initialAndStableValOccurences.containsAll(attributeTestOccurrences)) {
            if(!toAttNames.contains(attrKey)) {
                toActionSet = new ArrayList<String>(toValSet);
                toActionSet.add(attrKey + attVal);
                addRule(attributeTest, toActionSet);

            }else {
                toActionSet = new ArrayList<String>(toValSet);
                addRule(attributeTest, toActionSet);
            }
        }
    }

    /**
     * Rule addition after checking whether it is valid or not
     * @param fromAction from values
     * @param toAction to values
     */
    private void addRule(ArrayList<String> fromAction, ArrayList<String> toAction) {
        ArrayList<String> tempFrom = new ArrayList<String>();
        ArrayList<String> tempTo = new ArrayList<String>();

        tempFrom.addAll(fromAction);
        tempFrom.removeAll(getStableAtt(fromAction));
        tempTo.addAll(toAction);
        tempTo.removeAll(getStableAtt(toAction));

        boolean isValid = true;

        for(Map.Entry<ArrayList<String>, ArrayList<String>> entry : actionRules.entrySet()) {
            if(entry.getKey().equals(fromAction) && entry.getValue().equals(toAction)){
                isValid = false;
            }
        }

        if(checkSupportConfidence(fromAction, toAction)) {
            if(isValid){
                actionRules.put(fromAction, toAction);
            }
        }

    }

    /**
     * Writes action rules in the output file.
     */
    public void printRules() {

        ArrayList<String> fromAction;
        ArrayList<String> fromNames;


        ArrayList<String> toNames;
        ArrayList<String> toAction;


        List<String> stable;
        NumberFormat formatter = new DecimalFormat("#0.00");
        StringBuilder sb= new StringBuilder();

        sb.append("NEW");
        sb.append("\n");
        sb.append("ACTION RULES: ");
        sb.append("\n");

        StringBuilder re= new StringBuilder();
        Path file = Paths.get("output.txt");

        try  {
                BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.APPEND);
            if(actionRules.isEmpty()) {
                sb.append("No valid action rules found");
                sb.append("\n");
                writer.write(sb.toString());
            }
            else {
                for (Map.Entry<ArrayList<String>, ArrayList<String>> entry : actionRules.entrySet()) {

                    fromAction = new ArrayList<String>();
                    toAction = new ArrayList<String>();


                    fromNames = new ArrayList<String>();
                    toNames = new ArrayList<String>();

                    fromAction.addAll(entry.getKey());
                    toAction.addAll(entry.getValue());

                    stable = getStableAtt(toAction);

                    for (String attribute : stable) {
                        if (re.toString().isEmpty()) {
                            re.append("[(");
                            re.append(attribute);
                            re.append(")");
                        } else {
                            re.append("^");
                            re.append("(");
                            re.append(attribute);
                            re.append(")");
                        }
                    }
                    fromAction.removeAll(stable);
                    toAction.removeAll(stable);


                    for (String name : fromAction) {
                        fromNames.add(getAttributeName(name));
                    }

                    for (String name : toAction) {
                        toNames.add(getAttributeName(name));
                    }

                    for (String name : toNames) {
                        if (fromNames.contains(name)) {
                            String fromValue = fromAction.get(fromNames.indexOf(name));
                            String toValue = toAction.get(toNames.indexOf(name));

                            if (re.toString().isEmpty()) {
                                if (fromValue.equals(toValue)) { //When there is no change to flexible attribute
                                    re.append("[(");
                                    re.append(toValue);
                                    re.append(")");
                                } else {
                                    re.append("[(");
                                    re.append(name);
                                    re.append(", ");
                                    re.append(fromValue);
                                    re.append("-->");
                                    re.append(toValue);
                                    re.append(")");
                                }
                            } else {
                                if (fromValue.equals(toValue)) {
                                    re.append("^(");
                                    re.append(toValue);
                                    re.append(")");
                                } else {
                                    re.append("^(");
                                    re.append(name);
                                    re.append(", ");
                                    re.append(fromValue);
                                    re.append("-->");
                                    re.append(toValue);
                                    re.append(")");
                                }
                            }
                        } else {
                            if (re.toString().isEmpty()) {
                                re.append("[(");
                                re.append(name);
                                re.append(", ");
                                re.append("-->");
                                re.append(toAction.get(toNames.indexOf(name)));
                                re.append(")");
                            } else {
                                re.append("^(");
                                re.append(name);
                                re.append(", ");
                                re.append("-->");
                                re.append(toAction.get(toNames.indexOf(name)));
                                re.append(")");
                            }
                        }
                    }

                    re.append("] --> (");
                    re.append(getAttributeName(decisionValueInitial));
                    re.append(", ");
                    re.append(decisionValueInitial);
                    re.append("-->");
                    re.append(decisionValueEnd);
                    re.append(")");


                    ArrayList<Integer> suppConf = ruleSuppMap.get(entry.getKey());
                    re.append(" SUPPORT: ");
                    re.append(suppConf.get(0));
                    re.append(" CONFIDENCE: ");
                    re.append(formatter.format((suppConf.get(1))));
                    re.append("%");
                    re.append("\n");
                    sb.append(re.toString());
                    writer.write(sb.toString());
                    sb = new StringBuilder();
                    re = new StringBuilder();
                }
            }
            System.out.println("Closing File");
            writer.close();
        }catch (IOException error) {
            System.out.println(error.getStackTrace());

        }
    }

    /**
     * Sets minimum values for support and confidence of action rules
     * @param support
     * @param confidence
     */
    public void setMinSupportConfidence(int support, int confidence) {
        minSupport = support;
        minConfidence = confidence;
    }

    /**
     * Calculates the support and confidence and if it matches the min support and min confidence then return true and add in the rules.
     * @param fromAction
     * @param toAction
     * @return true if the rule has the min support & confidence
     */
    public boolean checkSupportConfidence(ArrayList<String> fromAction, ArrayList<String> toAction) {
        boolean isValidRule = true;
        int supportStart = 0;
        int supportFromStart = 0;
        int supportEnd = 0;
        int supportToEnd = 0;
        int support;
        int confidence;
        HashSet<String> fromAttIntersection = new HashSet<String>();
        HashSet<String> toAttIntersection = new HashSet<String>();


        fromAttIntersection=findSupportSet(fromAction, fromAttIntersection);
        supportStart=fromAttIntersection.size();
        fromAttIntersection=findSupportSetForDecision(attributeValueRows.get(decisionValueInitial), fromAttIntersection);
        supportFromStart = fromAttIntersection.size();

        toAttIntersection=findSupportSet(toAction,toAttIntersection);
        supportEnd = toAttIntersection.size();
        toAttIntersection=findSupportSetForDecision(attributeValueRows.get(decisionValueEnd), toAttIntersection);
        supportToEnd = toAttIntersection.size();

        support=supportFromStart < supportToEnd? supportFromStart:supportToEnd;
        confidence=supportStart == 0 || supportEnd == 0?0:(supportFromStart/supportStart) * (supportToEnd/supportEnd) * 100;

        if(support < minSupport || confidence < minConfidence) {
            isValidRule = false;
        }

        if(isValidRule) {
            ArrayList<Integer> suppConf = new ArrayList<Integer>();
            suppConf.add(support);
            suppConf.add(confidence);
            ruleSuppMap.put(fromAction, suppConf);
        }

        return isValidRule;
    }

    private HashSet<String> findSupportSetForDecision(HashSet<String> strings, HashSet<String> fromAttIntersection) {

        HashSet<String> remove= new HashSet<>();
        if(!fromAttIntersection.isEmpty()){
            for(String s: fromAttIntersection){
                if(!strings.contains(s)){
                    remove.add(s); // Not common
                }
            }
        }
        fromAttIntersection.removeAll(remove);
        return fromAttIntersection;
    }

    private HashSet<String> findSupportSet(ArrayList<String> fromAction, HashSet<String> fromAttIntersection) {


        for(String attribute : fromAction) {
            HashSet<String> temp = attributeValueRows.get(attribute);
            if(fromAttIntersection.isEmpty()) {
                fromAttIntersection.addAll(temp);
            }else {
                HashSet<String> notCommon = new HashSet<String>();
                for(String potentialLine : fromAttIntersection) {
                    if(!temp.contains(potentialLine))
                        notCommon.add(potentialLine); //The set does not occur together on that line
                }

                fromAttIntersection.removeAll(notCommon);

                if(fromAttIntersection.isEmpty()) //Set does not occur together at all. Reiteration would add next attributes lines
                    break;
            }
        }
        return fromAttIntersection;
    }


    /**
     * Find what lines the given list occurs on
     * @param attVal List of attribute values
     * @return The row numbers that the set occurs on
     */
    public List<String> findRows(List<String> attVal){

        HashSet<String> notCommon = new HashSet<String>();
        HashSet<String> current = new HashSet<String>();
        ArrayList<String> commonSet = new ArrayList<String>();


        for(String attribute : attVal) {
            current = attributeValueRows.get(attribute);

            if(commonSet.isEmpty()) {
                commonSet.addAll(current);
            }else {
                notCommon = new HashSet<String>();
                for(String potentialLine : commonSet) {
                    if(!current.contains(potentialLine))
                        notCommon.add(potentialLine);
                }

                commonSet.removeAll(notCommon);
                if(commonSet.isEmpty()) {
                    break;
                }
            }
        }

        return commonSet;
    }

    /**
     * Returns the stable attributes if it contains any.
     * @param attSet Set to get the stable attributes from
     * @return The header of the given set
     */
    private List<String> getStableAttributes(HashSet<String> attSet) {
        ArrayList<String> attributeNames = new ArrayList<String>();

        for(String attribute: attSet) {
            if(stableAttribute.contains(getAttributeName(attribute)))
                attributeNames.add(attribute);
        }

        return attributeNames;
    }


    private List<String> getStableAtt(ArrayList<String> set) {
        ArrayList<String> header = new ArrayList<String>();

        for(String attribute: set) {
            if(stableAttribute.contains(getAttributeName(attribute)))
                header.add(attribute);
        }

        return header;
    }



    /**
     *
     * @param value
     * @return attribute value
     */
    public String getAttributeValue(String value) {
        String aValue = "";

        for(String name : attributeNames) {
            if(value.startsWith(name)) {
                aValue = value.substring(name.length());
                break;
            }
        }

        return aValue;
    }

    /**
     *
     * @param value
     * @return attribute name
     */
    public String getAttributeName(String value) {
        String attName = "";

        for(String name : attributeNames) {
            if(value.startsWith(name)) {
                attName = name;
                break;
            }
        }

        return attName;
    }


    public void generateRules(){
        LERSAlgo = new LERSAlgo(decisionValueInitial, decisionValueEnd, attributeValueRows);
        LERSAlgo.executeLERSAlgo();
        findActionRules();
        printRules();

    }


}
