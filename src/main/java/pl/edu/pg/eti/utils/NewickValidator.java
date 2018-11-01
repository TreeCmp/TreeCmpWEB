package pl.edu.pg.eti.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.forester.io.parsers.PhylogenyParser;
import org.forester.io.parsers.nexus.NexusPhylogeniesParser;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import pl.edu.pg.eti.model.Newick;

import org.forester.io.parsers.util.ParserUtils;
import org.forester.phylogeny.Phylogeny;

public class NewickValidator {

	private Integer MIN_WINDOW_VAL = 2;
	private Integer MAX_WINDOW_VAL = 1000;
	private Integer MAX_TREES_TRESHOLD = 10;
	private Integer MAX_LEAVES_TRESHOLD = 1000;
	private Integer MAX_LEAVES_PROTOTYPE_METRIC_TRESHOLD = 100;


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
	
	public void validate(File comparisionFile, File refTreeFile, boolean limitedTreesSize) {
		if (comparisionFile != null) {
			this.comparisionFileLoc = comparisionFile.getAbsolutePath();
		}
		if (refTreeFile != null) {
			this.refTreeFileLoc = refTreeFile.getAbsolutePath();
		}
		validateMetrics();
		validateSelectedMode();
		validateWindowWidthInput();
		validateInput(limitedTreesSize);
	}
	
	private void validateMetrics() {
		if (newick.getRootedMetrics().length == 0 && newick.getUnrootedMetrics().length == 0) {
			ObjectError objError = new FieldError("newick", "hasMetrics", "At least one metric of any kind must be specified.");
			newickErrors.add(objError);
		}
	}
	
	private void validateInput(boolean limitedTreesSize) {
		if (newick.getInputType() != null && newick.getInputType().equals(NewickUtils.STRING_INPUT)) {
			validateInputString(newick.getNewickStringFirst(), "newickStringFirst", comparisionFileLoc, limitedTreesSize);
			
			if (newick.getComparisionMode().equals(NewickUtils.REF_TO_ALL_COMPARISION_MODE)) {
				validateInputString(newick.getNewickStringSecond(), "newickStringSecond", refTreeFileLoc, limitedTreesSize);
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

	private void validateWindowWidthInput() {
		if (newick.getComparisionMode().equals(NewickUtils.WINDOW_COMPARISION_MODE)) {
			if (newick.getWindowWidth() == null || !validateWindowWidth(newick.getWindowWidth())) {
				ObjectError objError = new FieldError("newick", "comparisionMode", "Window width must be between " + MIN_WINDOW_VAL + " and " + MAX_WINDOW_VAL);
				newickErrors.add(objError);
			}
		}
	}

	private boolean validateWindowWidth(Integer ww) {
		if (ww < MIN_WINDOW_VAL || ww > MAX_WINDOW_VAL) return false;
		return true;
	}

	private void validateInputString(String inputString, String fieldName, String fileLoc, boolean limitedTreesSize) {
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
            PhylogenyParser nexusParser = new NexusPhylogeniesParser();
            nexusParser.setSource(fileLoc);
			Phylogeny[] trees = nexusParser.parse();
			if (trees.length == 0) {
                trees = ParserUtils.readPhylogenies(fileLoc);
            }
            if(trees.length == 0) {
                objError = new FieldError("newick", fieldName, "Input format is not correct.");
                newickErrors.add(objError);
            }
			if (limitedTreesSize) {
				if (trees.length > MAX_TREES_TRESHOLD) {
					objError = new FieldError("newick", fieldName, "More than " + MAX_TREES_TRESHOLD + " trees are forbidden");
                    newickErrors.add(objError);
				}
				else {
                    for (Phylogeny tree : trees) {
                        /*if (newick.containsMetric("um") && tree.getNumberOfExternalNodes() > MAX_LEAVES_PROTOTYPE_METRIC_TRESHOLD) {
                            objError = new FieldError("newick", fieldName, "For UMAST metric trees with more than " + MAX_LEAVES_PROTOTYPE_METRIC_TRESHOLD + " leaves are forbidden");
                            newickErrors.add(objError);
                            break;
                        }*/
                        if (tree.getNumberOfExternalNodes() > MAX_LEAVES_TRESHOLD) {
                            objError = new FieldError("newick", fieldName, "Trees with more than " + MAX_LEAVES_TRESHOLD + " leaves are forbidden");
                            newickErrors.add(objError);
                            break;
                        }
                    }
                }
			}
        } catch (Exception e) {
			objError = new FieldError("newick", fieldName, "Input format is not correct.");
			newickErrors.add(objError);
		}
	}
}
