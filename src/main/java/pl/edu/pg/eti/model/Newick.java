package pl.edu.pg.eti.model;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.support.ErrorMessage;

////////////////////////////////////
//////// Input settings connected///
/////////to NEWICK FORMAT//////////
///////////////////////////////////
public class Newick {

	private String newickStringFirst;

	private String newickErrorFirst;

	private String newickStringSecond;

	private String newickErrorSecond;

	private String generalError;
	
	private String comparisionMode = "";

	private Integer windowWidth;

	private String inputType = "stringInput";
	
	private boolean hasMetrics;

	private boolean normalizedDistances;

	private boolean pruneTrees;

	private boolean includeSummary;

	private boolean zeroWeightsAllowed;

	private boolean bifurcationTreesOnly;

	/* METRICS */
	private String[] rootedMetrics;
	private String[] unrootedMetrics;
	
	private String report;
	
	public void setNewickStringFirst(String newickStringFirst) {
		this.newickStringFirst = newickStringFirst;
	}
	
	public String getNewickStringFirst() {
		return newickStringFirst;
	}

	//public void setnewickErrorFirst(String newickErrorFirst) { this.newickErrorFirst = newickErrorFirst; }

	public String getnewickErrorFirst() { return newickErrorFirst; }

	public String getNewickStringSecond() {
		return newickStringSecond;
	}

	public void setNewickStringSecond(String newickStringSecond) {
		this.newickStringSecond = newickStringSecond;
	}

	//public void setNewickErrorSecond(String newickErrorSecond) { this.newickErrorSecond = newickErrorSecond; }

	public String getNewickErrorSecond() { return newickErrorSecond; }

	//public void setGeneralError(String generalError) { this.generalError = generalError; }

	public String getGeneralError() { return generalError; }

	public String[] getRootedMetrics() {
		return rootedMetrics;
	}

	public void setRootedMetrics(String[] rootedMetrics) {
		this.rootedMetrics = rootedMetrics;
	}

	public String[] getUnrootedMetrics() {
		return unrootedMetrics;
	}

	public void setUnrootedMetrics(String[] unrootedMetrics) {
		this.unrootedMetrics = unrootedMetrics;
	}

	public boolean containsMetric(String metric) {
		for (String m:this.unrootedMetrics) {
			if (m.equals(metric)) {
				return true;
			}
		}
		for (String m:this.rootedMetrics) {
			if (m.equals(metric)) {
				return true;
			}
		}
		return false;
	}

	public boolean isHasMetrics() {
		if (unrootedMetrics != null && rootedMetrics != null) {
			return unrootedMetrics.length > 0 || rootedMetrics.length > 0;
		} else {
			return false;
		}
	}

	public boolean isNormalizedDistances() {
		return normalizedDistances;
	}

	public void setNormalizedDistances(boolean normalizedDistances) {
		this.normalizedDistances = normalizedDistances;
	}

	public boolean isIncludeSummary() {
		return includeSummary;
	}

	public void setIncludeSummary(boolean includeSummary) { this.includeSummary = includeSummary; }

	public boolean isZeroWeightsAllowed() { return zeroWeightsAllowed; }

	public void setZeroWeightsAllowed(boolean zeroWeightsAllowed) { this.zeroWeightsAllowed = zeroWeightsAllowed; }

	public boolean isBifurcationTreesOnly() { return bifurcationTreesOnly; }

	public void setBifurcationTreesOnly(boolean bifurcationTreesOnly) { this.bifurcationTreesOnly = bifurcationTreesOnly; }

	public boolean isPruneTrees() {
		return pruneTrees;
	}

	public void setPruneTrees(boolean pruneTrees) {
		this.pruneTrees = pruneTrees;
	}
	
	public void setReport(String report) {
		this.report = report;
	}
	
	public String getReport() {
		return report;
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) { this.inputType = inputType; }

	public String getComparisionMode() { return comparisionMode; }

	public void setComparisionMode(String comparisionMode) { this.comparisionMode = comparisionMode; }

	public Integer getWindowWidth() { return windowWidth; }

	public void setWindowWidth(Integer windowWidth) { this.windowWidth = windowWidth; }
}
