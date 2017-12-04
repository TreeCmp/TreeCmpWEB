package pl.edu.pg.eti.utils;

import java.util.ArrayList;

public class NewickSplitter {
	
	private String newick;
	private ArrayList<String> trees;

	public NewickSplitter(String newickContent) {
		newick = newickContent;
		splitTrees();
	}
	
	private void splitTrees() {
		trees = new ArrayList<String>();
		
		String tree;
		while((tree = getNextTree()).isEmpty() != true) {
			trees.add(tree);
		}		
	}
	
	public String GetAllTreesAsJsTable() {
		return javascriptTable(trees);
	}
	
	public String GetTree(final int id) {
		
		if(trees.isEmpty()) {
			String emptyJsString = "\"\"";
			return emptyJsString;
		}
		
		return "\""+ trees.get(id) + "\"";
	}
	
	private String getNextTree() {
		String nextTree;
		
		int semicolonPosition = newick.indexOf(';');
		Boolean containsOpeningBracket = newick.contains("(");
		
		if(semicolonPosition > 0 && containsOpeningBracket) {
			nextTree = newick.substring(0, semicolonPosition+1);
			newick = newick.substring(semicolonPosition+1);			
		}
		else {
			nextTree = "";
			newick = "";
		}
		
		return nextTree;
	}

	private String javascriptTable(ArrayList<String> array) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");

		for(int i = 0; i < array.size(); i++) {
			buffer.append("\"").append(array.get(i)).append("\"");
			
			if(i+1 < array.size()) {
				buffer.append(",");
			}
		}
		
		buffer.append("]");
		
		return buffer.toString();
	}
}

