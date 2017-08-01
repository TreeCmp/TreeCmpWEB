package pl.gda.pg.eti.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import pl.gda.pg.eti.model.Newick;

import org.forester.io.parsers.util.ParserUtils;
import org.forester.phylogeny.Phylogeny;

public class NewickValidator {
	
	private String comparisionFileLoc;
	private String refTreeFileLoc;
	private Newick newick;
	private List<ObjectError> newickErrors;
	
	public NewickValidator(Newick newick) {
		this.newick = newick;
		newickErrors = new ArrayList<ObjectError>();
	}
	
	public List<ObjectError> getErrors() {
		return newickErrors;
	}
	
	public void validate(File comparisionFile, File refTreeFile) {
		if (comparisionFile != null) {
			this.comparisionFileLoc = comparisionFile.getAbsolutePath();
		}
		if (refTreeFile != null) {
			this.refTreeFileLoc = refTreeFile.getAbsolutePath();
		}
		validateMetrics();
		validateSelectedMode();
		validateInput();
	}
	
	private void validateMetrics() {
		if (newick.getRootedMetrics().length == 0 && newick.getUnrootedMetrics().length == 0) {
			ObjectError objError = new FieldError("newick", "hasMetrics", "At least one metric of any kind must be specified.");
			newickErrors.add(objError);
		}
	}
	
	private void validateInput() {
		if (newick.getInputType() != null && newick.getInputType().equals(NewickUtils.STRING_INPUT)) {
			validateInputString(newick.getNewickStringFirst(), "newickStringFirst", comparisionFileLoc);
			
			if (newick.getComparisionMode().equals(NewickUtils.BIPARTITE_COMPARISION_MODE)) {
				validateInputString(newick.getNewickStringSecond(), "newickStringSecond", refTreeFileLoc);
			}
		} else if (newick.getInputType() != null && newick.getInputType().equals(NewickUtils.FILE_INPUT)) {
		}
	}
	
	private void validateSelectedMode() {
		if (newick.getComparisionMode() == null || newick.getComparisionMode().isEmpty()) {
			ObjectError objError = new FieldError("newick", "comparisionMode", "At least one mode must be choosen.");
			newickErrors.add(objError);
		}
	}
	
	private void validateInputString(String inputString, String fieldName, String fileLoc) {
		ObjectError objError = null;
		if (inputString.isEmpty()) {
			objError = new FieldError("newick", fieldName, "Field cannot be empty.");
			newickErrors.add(objError);
			return;
		}
		
/*		if (inputString.length() > 5000 || inputString.length() < 2) {
			objError = new FieldError("newick", fieldName, "Newick input length must be between 2 and 5000 characters.");
			newickErrors.add(objError);
		}*/
		
		try {
			Phylogeny[] trees = ParserUtils.readPhylogenies(fileLoc);
		} catch (Exception e) {
			objError = new FieldError("newick", fieldName, "Input format is not correct.");
			newickErrors.add(objError);
		}
	}
}
