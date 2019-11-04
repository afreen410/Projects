import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * LERS Algo
 * @author
 * KDD Group 1
 * Anchal Atlani
 * Afreen
 * Ankita Kumari
 * Rishika Ganga Shetty
 * Satish Kumar
 * Satish Kumar
 *
 *
 */
public class LERSAlgo extends Observable {

	private HashSet<String> decisionSetInitial;

	private HashSet<String> decisionSetTo;

	private Map<String, HashSet<HashSet<String>>> certainRules = new HashMap<String, HashSet<HashSet<String>>>();

	private Map<HashSet<String>, HashSet<String>> attributeValues = new HashMap<HashSet<String>, HashSet<String>>();

	private Map<HashSet<String>, Integer> supportRules = new HashMap<HashSet<String>, Integer>();

	/**
	 * List of possible rules which are used to keep track unmarked rules
	 */
	private ArrayList<String> possibleRules = new ArrayList<String>();

	/**
	 * Stores decision variable end value
	 */
	private String decisionValueEnd;

	/*
	* Stores decision variable initial value
	 */
	private String decisionValueStart;


	
	public LERSAlgo(String decisionValueInitial, String decisionValueTo, Map<String, HashSet<String>> attributeValues) {

		this.decisionSetInitial = attributeValues.get(decisionValueInitial);
		this.decisionSetTo = attributeValues.get(decisionValueTo);

		this.decisionValueStart = decisionValueInitial;
		this.decisionValueEnd = decisionValueTo;

		HashSet<String> tempSet;
		for(Map.Entry<String, HashSet<String>> entry : attributeValues.entrySet()) {
			tempSet = new HashSet<String>();
			tempSet.add(entry.getKey());
			this.attributeValues.put(tempSet, entry.getValue());
		}


	}
	
	/**
	 * LERS Algorithm, loops until no unmarked sets found.
	 * Each iteration finds out certain rules and then from the set of possible rules combines
	 * the attributes and again make them marked if possible.
	 */
	public void executeLERSAlgo() {

		HashSet<String> keyRemoveSet;
		HashMap<HashSet<String>, HashSet<String>> valueMap = new HashMap<HashSet<String>, HashSet<String>>();
		File output = new File("output.txt");
		int iteration = 0;
		
		if(output.exists())
			output.delete();
		
		Path file = Paths.get("output.txt");
		
		try  {
			BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE);

			keyRemoveSet = new HashSet<String>();
			keyRemoveSet.add(decisionValueStart);
			attributeValues.remove(keyRemoveSet);
			keyRemoveSet.clear();
			keyRemoveSet.add(decisionValueEnd);
			attributeValues.remove(keyRemoveSet);
			
			//Continue until there are no more attribute values to combine
			while(!attributeValues.isEmpty()) {
				valueMap.clear();
				valueMap.putAll(attributeValues);
				iteration++;
				writeAttributes(iteration,writer);
				
				//Check each attribute value set to check if it is a certain rule
				for(Map.Entry<HashSet<String>, HashSet<String>> entry : valueMap.entrySet()) {
					if (decisionSetInitial.containsAll(entry.getValue())) { 
						addCertainRule(entry.getKey(), entry.getValue().size(), decisionValueStart);
						attributeValues.remove(entry.getKey());
					}else if(decisionSetTo.containsAll(entry.getValue())){
						addCertainRule(entry.getKey(), entry.getValue().size(), decisionValueEnd);
						attributeValues.remove(entry.getKey()); 
					}else {
						addPossibleRule(entry.getValue(), entry.getKey());
					}
				}
				
				writeRules(iteration, writer);
				mergeAttributes(iteration);
			}
			writer.close();
		} catch (IOException error) {
		    System.out.println(error.getStackTrace());

		}
	}

	/**
	 * Combines the attribute values and their line occurrences for next loop of the LERS algorithm.
	 */
	private void mergeAttributes(int iteration) {

		HashMap<HashSet<String>, HashSet<String>> updatedAttributeValues = new HashMap<HashSet<String>, HashSet<String>>();
		HashMap<HashSet<String>, HashSet<String>> oldAttributeValues = new HashMap<HashSet<String>, HashSet<String>>();

		HashSet<String> outerKey;
		HashSet<String> outerValue;
		HashSet<String> newKey;
		HashSet<String> commonRows;

		updatedAttributeValues.putAll(attributeValues);

		oldAttributeValues.putAll(attributeValues);

		for(Map.Entry<HashSet<String>, HashSet<String>> entry : attributeValues.entrySet()) {
			outerKey = entry.getKey();
			outerValue = entry.getValue();

			updatedAttributeValues.remove(entry.getKey());
			oldAttributeValues.remove(entry.getKey());

			//Combine the first set with all other attribute value sets
			for(Map.Entry<HashSet<String>, HashSet<String>> entryRemain : oldAttributeValues.entrySet()) {
				newKey = new HashSet<String>();
				newKey.addAll(outerKey);
				newKey.addAll(entryRemain.getKey());

				if(updatedAttributeValues.containsKey(newKey))
					continue;

				if(newKey.size() != (iteration+1))
					continue;

				commonRows=findIntersection(outerValue, entryRemain.getValue());

				if(commonRows.size() > 0) {
					boolean isAlreadySubset = checkIsSubset(newKey, certainRules);
					if(!isAlreadySubset) {
						updatedAttributeValues.put(newKey, commonRows);
					}
				}
			}
		}

		attributeValues = updatedAttributeValues;
	}

	//if a set in certain rules is a subset of the new combination, don't continue with set
	private boolean checkIsSubset(HashSet<String> newSetKey, Map<String, HashSet<HashSet<String>>> certainRules) {
		boolean isAlreadySubset=false;
		for(Map.Entry<String, HashSet<HashSet<String>>> ruleSets : certainRules.entrySet()) {
			Iterator<HashSet<String>> setsIterator = ruleSets.getValue().iterator();

			while(setsIterator.hasNext()) {
				if(newSetKey.containsAll(setsIterator.next()))
					isAlreadySubset = true;
			}
		}
		return isAlreadySubset;
	}

	private HashSet<String> findIntersection(HashSet<String> firstSetValue, HashSet<String> value) {
		Iterator<String> iterator=  firstSetValue.iterator();
		HashSet<String> commonRows= new HashSet<String>();
		String temp=null;
		while(iterator.hasNext()) {
			temp = iterator.next();
			if(value.contains(temp)) {
				commonRows.add(temp);
			}
		}
		return commonRows;
	}

	/**
	 * Sets all current attribute sets and their line occurrences to the result string for runLers to 
	 * save to file
	 * 
	 * @param iteration Number of current loop
	 */
	private void writeAttributes(int iteration,BufferedWriter writer ) throws IOException {
		StringBuilder sb= new StringBuilder();
		sb.append("\n");
		sb.append("Iteration");
		sb.append(iteration);
		sb.append("\n");
		sb.append("Sets:");
		sb.append("\n");
		
		for(Map.Entry<HashSet<String>, HashSet<String>> entry : attributeValues.entrySet()) {
			sb.append(entry.getKey().toString());
			sb.append(":");
			sb.append(entry.getValue().toString());
			sb.append("\n");
		}
		sb.append("[");
		sb.append(decisionValueStart);
		sb.append("]: ");
		sb.append(decisionSetInitial.toString());
		sb.append("\n");
		sb.append("[");
		sb.append(decisionValueEnd);
		sb.append("\n");
		System.out.println(sb.toString());
		writer.write(sb.toString());

	}

	/**
	 * Sets all current possible rules and the certain rules to the result string for runLers to 
	 * save to file
	 * 
	 * @param iteration number of current loop
	 */
	private void writeRules(int iteration, BufferedWriter writer) throws IOException {
		StringBuilder sb= new StringBuilder();
		sb.append("\n");
		sb.append("CERTAIN RULES");
		sb.append("\n");


		HashSet<String> setOfValues;
		
		for(Map.Entry<String, HashSet<HashSet<String>>> entry : certainRules.entrySet()) {
			Iterator<HashSet<String>> valueIterator= entry.getValue().iterator();
			
			while(valueIterator.hasNext()) {
				setOfValues = new HashSet<String>();
				setOfValues.addAll(valueIterator.next());
				sb.append(setOfValues.toString());//Set on left of certain rule
				sb.append(" ----> ");
				sb.append(entry.getKey()); //Set on right of certain rule
				setOfValues.add(entry.getKey());
				sb.append("  SUPPORT: ");
				sb.append(supportRules.get(setOfValues).toString());
				sb.append("  CONFIDENCE: 100%"); //Any certain rule has 100% confidence
				sb.append("\n");
			}
		}
		
		sb.append("\n");
		sb.append("POSSIBLE RULES :");
		sb.append("\n");

		for(int i = 0; i<possibleRules.size(); i++) {
			sb.append(possibleRules.get(i));
		}

		writer.write(sb.toString());
		possibleRules = new ArrayList<String>();
	}



	/**
	 * Calculates possible rules based on given set and line occurrences
	 * 
	 * @param value line occurrences 
	 * @param key attribute set
	 */
	private void addPossibleRule(HashSet<String> value, HashSet<String> key) {
		int startValueSupport = 0;
		int endValueSupport = 0;
		float confidence;
		StringBuilder sb= new StringBuilder();
		NumberFormat formatter = new DecimalFormat("#0.00");

		String[] valueArray=value.toArray(new String[value.size()]);
		for(String currOccurence : valueArray) {
			if(decisionSetInitial.contains(currOccurence)) {
				startValueSupport++;
			}else if(decisionSetTo.contains(currOccurence)) {
				endValueSupport++;
			}
		}
		
		if(startValueSupport > 0) {
			String[] keyString = key.toArray(new String[key.size()]);
			sb.append(keyString[0]);
			for(int i = 1; i < keyString.length; i++) {
				sb.append("^");
				sb.append(keyString[i]);

			}
			sb.append(" --> ");
			sb.append(decisionValueStart);
			sb.append( "  SUPPORT:");
			sb.append( startValueSupport);
			
			confidence = ((float)startValueSupport/value.size() * 100);
			sb.append(" CONFIDENCE:" );
			sb.append(formatter.format((confidence)));
			sb.append("%");
			sb.append("\n");
			possibleRules.add(sb.toString());
		}

		sb= new StringBuilder();
		if(endValueSupport > 0) {
			String[] keyString = key.toArray(new String[key.size()]);
			sb.append(keyString[0]);
			for(int i = 1; i < keyString.length; i++) {
				sb.append("^");
				sb.append(keyString[i]);
			}
			sb.append(" --> ");
			sb.append(decisionValueEnd);
			sb.append(" SUPPORT:" );
			sb.append(endValueSupport);
			
			confidence = ((float)endValueSupport/value.size() * 100);
			sb.append(" CONFIDENCE:");
			sb.append(formatter.format((confidence)));
			sb.append("%");
			sb.append("\n");
			possibleRules.add(sb.toString());
		}		
	}

	/**
	 * Adds certain rule to the certainRules mapping and support value to rulesSupport
	 * 
	 * @param value set of attributes on left of arrow
	 * @param support the support value for this rule
	 * @param key decision attribute on right of rule
	 */
	private void addCertainRule(HashSet<String> value, int support, String key) {
		HashSet<HashSet<String>> tempSet;
		HashSet<String> supportSet = new HashSet<String>();
		
		if(certainRules.containsKey(key)) {
			tempSet = certainRules.get(key);
			tempSet.add(value);
			certainRules.put(key, tempSet);
		}else{
			tempSet = new HashSet<HashSet<String>>();
			tempSet.add(value);
			certainRules.put(key, tempSet);
		}
		
		supportSet.addAll(value);
		supportSet.add(key);
		supportRules.put(supportSet, support);
	}

	/**
	 * Returns certain rules map
	 * @return certain rules map
	 */
	public Map<String, HashSet<HashSet<String>>> getCertainRules(){
		return certainRules;
	}
	
}
