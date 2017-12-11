package pl.edu.pg.eti.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class HtmlUtils {
	
	private int summarySize;
	
	public String GenerateShortenedReport(String content, int firstTreeId, int secondTreeId) {
		StringBuilder report = new StringBuilder();
		String table = content.split(table_separator)[0];
		
		report.append(parseShortTable(table, firstTreeId, secondTreeId));
		return report.toString();
	}
	
	public String GenerateReportTable(String content, Boolean containsSummary) {
		String[] tables = content.split(table_separator);		
		StringBuilder report = new StringBuilder();
		Boolean isSummaryTable = false;
		report.append(parseTable(tables[0], isSummaryTable));
		
		if(containsSummary == true) {
			isSummaryTable = true;
			report.append(parseTable(tables[1], isSummaryTable));
		}
		return report.toString();
	}
	
	private String parseShortTable(String data, int firstId, int secondId) {
		StringBuilder table = new StringBuilder();
		Boolean isBold = true;
		Boolean notSummary = false;
		Boolean shortTable = true;
		
		ArrayList<String> lines = new ArrayList(Arrays.asList(data.split(line_separator)));
		String headerLine = lines.get(0);
		
		table.append(table_start);
		table.append(line_separator);
		table.append(rowFrom(headerLine, isBold, notSummary, shortTable));
		
		isBold = false;
		int treePairRowIndex = indexOfTreePair(firstId, secondId, lines);		
		table.append(rowFrom(lines.get(treePairRowIndex), isBold, notSummary, shortTable));
		table.append(table_end);
		
		return table.toString();
	}
	
	private int indexOfTreePair(int firstId, int secondId, ArrayList<String> lines) {
		for(int i = 1; i < lines.size(); i++) {
			String[] fields = lines.get(i).split(whitespace);
			int id1 = Integer.parseInt(fields[1]);
			int id2 = Integer.parseInt(fields[2]);
			
			if(firstId == id1 && secondId == id2)
				return i;
		}	
		
		return 0;
	}
	
	private String parseTable(String data, Boolean isSummary) {		
		StringBuilder table = new StringBuilder();
		Boolean isBold = true;
		Boolean isShortTable = false;
		ArrayList<String> lines = new ArrayList(Arrays.asList(data.split(line_separator)));		
		
		if(isSummary == true) {
			lines.remove(0);
			summarySize = lines.get(1).split(whitespace).length-1;
		}			
		
		String headerLine = lines.get(0);
		
		table.append(sort_css);
		table.append(line_separator);
		table.append(table_start);
		table.append(line_separator);
		table.append(tablehead_start);
		table.append(rowFrom(headerLine, isBold, isSummary, isShortTable));
		table.append(tablehead_end);
		
		isBold = false;
		
		table.append(tablebody_start);
		for(int i = 1; i < lines.size(); i++) {
			table.append(rowFrom(lines.get(i), isBold, isSummary, isShortTable));
		}
		table.append(tablebody_end);
		table.append(table_end);
		
		return table.toString();
	}
	
	private String rowFrom(String data, Boolean header, Boolean isSummary, Boolean shortTable) {		
		StringBuilder row = new StringBuilder();		
		String[] cells = data.split(whitespace);
		
		row.append(tr_start);
		
		for(int i = 0; i < cells.length; i++) {
			if(shortTable == true && i < 3)
				continue;
			
			String arrows="";
			if (isSummary == false && shortTable == false) arrows=arrow_bottom+arrow_top;
			
			if(header == true) {
				row.append(boldedCell(cells[i]+arrows));
			}
			else {
				row.append(cell(cells[i]));
			}
		}
		
		if(isSummary == false && shortTable == false) {
			row.append(addButtons(header, cells[1], cells[2]));
		}
		else {
			if(header == true && shortTable == false) {
				for(int i = 0; i < summarySize; i++) {
					row.append(cell(""));
				}
			}
		}		
		
		row.append(tr_end);
		row.append(line_separator);
		
		return row.toString();		
	}

	private String addButtons(Boolean header, String firstId, String secondId) {
		if(header == true) {
			return boldedCell("Draw trees with");
		}
		else {
			String trees = firstId + "," + secondId;
			return cell(button(trees));
		}
	}

	private String addRadioButtons(Boolean header, String firstId, String secondId) {
		if(header == true) {
			return boldedCell("Draw trees with");
		}
		else {
			String trees = firstId + "," + secondId;
			return cell(radioButton(trees));
		}
	}
	
	
	private String cell(String content) {
		return td_start + content + td_end;
	}
	
	private String boldedCell(String content) {
		return td_start + bold_start + content + bold_end + td_end;
	}
	
	private String radioButton(String value) {
		String[] ids = value.split(",");
		String firstTreeId = ids[0];
		String secondTreeId = ids[1];
		
		return "<input type=\"radio\" name=\"treesId\" value=\""+ value + "\" onclick=\"popup("+ firstTreeId + "," + secondTreeId + ")\"/>";
	}

	private String button(String value) {
		String[] ids = value.split(",");
		String firstTreeId = ids[0];
		String secondTreeId = ids[1];

		return "<button type=\"button\" name=\"treesId\" value=\""+ value + "\" onclick=\"popup("+ firstTreeId + "," + secondTreeId + ")\" class=\"btn\" >Phylo.io</button>";
	}

	private final String table_separator = "---------";
	private final String line_separator = System.getProperty("line.separator");
	private final String table_start = "<div class=\"table-responsive\"/> <table id=\"reportTable\" class=\"table table-hover\">";
	private final String table_end = "</table> </div>";
	private final String tablehead_start = "<thead>";
	private final String tablehead_end = "</thead>";
	private final String tablebody_start = "<tbody>";
	private final String tablebody_end = "</tbody>";
	private final String bold_start="<b>";
	private final String bold_end="</b>";
	private final String tr_start = "<tr>";
	private final String tr_end = "</tr>";
	private final String td_start = "<td>";
	private final String td_end = "</td>";
	private final String arrow_top = "<span class=\"glyphicon glyphicon-triangle-top\"></span>";
	private final String arrow_bottom = "<span class=\"glyphicon glyphicon-triangle-bottom\"></span>";
	private final String sort_css = "<style>" +
			".tablesorter-header span {  display: none; }" +
			".tablesorter-headerDesc .glyphicon-triangle-bottom {  display: inline;}" +
			".tablesorter-headerAsc .glyphicon-triangle-top {  display: inline;}" +
			"</style>";
	private final String whitespace = "\\s+";
}