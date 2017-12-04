package pl.edu.pg.eti.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import javax.servlet.ServletContext;
import javax.validation.Valid;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.core.io.ClassPathResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;

import org.thymeleaf.util.DateUtils;
import pl.edu.pg.eti.model.JsonTrees;
import pl.edu.pg.eti.model.Mode;
import pl.edu.pg.eti.model.Newick;
import pl.edu.pg.eti.utils.ConfigParser;
import pl.edu.pg.eti.utils.HtmlUtils;
import pl.edu.pg.eti.utils.NewickSplitter;
import pl.edu.pg.eti.utils.NewickUtils;
import pl.edu.pg.eti.utils.NewickValidator;

@Controller
@Scope("session")
@EnableAutoConfiguration
public class NewickController {

	private static String METRICS = "-d";
	private static String NORMALIZED_DISTANCES = "-N";
	private static String PRUNE_COMPARED = "-P";
	private static String INCLUDE_SUMMARY = "-I";
	private static String INPUT_FILE = "-i";
	private static String OUTPUT_FILE = "-o";

	@Autowired
	ServletContext servletContext;

	private List<String> arguments = new ArrayList<String>();
	private static ConfigParser confParser = new ConfigParser();
	private HtmlUtils htmlUtils = new HtmlUtils();

	private NewickSplitter splitter;
	private NewickSplitter referencedTreesSplitter;
    private static String rawReport;
	private Mode comparisionMode;

	private static String configFile = "";
	private static String dataDir = "";

	public static void main(String[] args) throws Exception {
		CopyDataAndConfigFilesToTemporary();
		SpringApplication.run(NewickController.class, args);
		ShowMessageAboutSuccessfulStartup();
	}

	private static void CopyDataAndConfigFilesToTemporary() {

		String actualDate = DateUtils.createNow(Locale.getDefault()).getTime().toString().replace(" ", "_").replace(":", "_");
		Path tempDirPath = null;
		try {
			tempDirPath = Files.createTempDirectory("treeCmp_" + actualDate + "_" );
		} catch (IOException e) {
			e.printStackTrace();
		}

		String configFileExternalForm =  NewickController.class.getClassLoader().getResource("static/config/config.xml").toExternalForm();
		if (configFileExternalForm.startsWith("jar:")) {

			//String tempDirString = System.getProperty("java.io.tmpdir");
			configFile = tempDirPath + "/config.txt";

			// copy config file to temp directory
			try {
				InputStream configFileStream = new ClassPathResource("static/config/config.xml").getInputStream();
				//Files.delete(Paths.get(tempConfigString));
				File tempConfigFile = new File(configFile);
				OutputStream tempConfigFileStream = new FileOutputStream(tempConfigFile);
				int read;
				byte[] bytes = new byte[1024];
				while ((read = configFileStream.read(bytes)) != -1) {
					tempConfigFileStream.write(bytes, 0, read);
				}
				configFileStream.close();
				tempConfigFile.deleteOnExit();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			dataDir = tempDirPath + "/data";

			// copy data files to temp directory

			new File(dataDir).mkdir();
			for (String metricFile : confParser.getAvailableMetricsFiles()) {
				try {
					InputStream dataFileStream = new ClassPathResource("static/data/" + metricFile).getInputStream();
					File file = new File(dataDir + "/" + metricFile);
					OutputStream out = new FileOutputStream(file);
					int read;
					byte[] bytes = new byte[1024];
					while ((read = dataFileStream.read(bytes)) != -1) {
						out.write(bytes, 0, read);
					}
					dataFileStream.close();
					file.deleteOnExit();
				} catch (IOException ex) {
				}
			}
		}
	}

	private static void ShowMessageAboutSuccessfulStartup() {
		//String port = environment.getProperty("local.server.port");
		System.out.println();
		System.out.println("********************************");
		System.out.println("Application started successfully");
		System.out.println("********************************");
		System.out.println();
		System.out.println("Description:");
		System.out.format("The application GUI is available at: http://localhost:%s/TreeCmp/WEB", "<port_number>");
	}

	@RequestMapping(value = "/WEB", method = RequestMethod.GET)
	public ModelAndView getNewick(Model model) {

		try {
			InputStream configFileStream = new ClassPathResource("static/config/config.xml").getInputStream();
			confParser.setMetricConfigFileStream(configFileStream);
			confParser.clearAndSetAvailableMetrics();
			configFileStream.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		model.addAttribute("rootedMetrics", confParser.getAvailableRootedMetricsWithCmd());
		model.addAttribute("unrootedMetrics", confParser.getAvailableUnrootedMetricsWithCmd());

		return new ModelAndView("inputform", "newickStringNew", new Newick());
	}

	@RequestMapping(value = "/report", method = RequestMethod.POST)
	public ModelAndView report(
			@ModelAttribute("newickStringNew") @Valid Newick newick,
			BindingResult bindingResult,
			ModelMap model) throws IOException {

		NewickUtils nu = new NewickUtils();

		if (arguments != null) {
			arguments.clear();
		}

		File inputFile = nu.createTempFileWithGivenContent(newick.getNewickStringFirst());
		File refTreeFile = null;

		if (newick.getComparisionMode().equals(NewickUtils.WINDOW_COMPARISION_MODE)) {
			arguments.add(NewickUtils.WINDOW_COMPARISION_MODE);
			comparisionMode = Mode.WINDOW;
			arguments.add(String.format("%s", newick.getWindowWidth()));
			String normalizedFirstNewick = NewickUtils.GetNormalizedNewickString(newick.getNewickStringFirst());
			splitter = new NewickSplitter(normalizedFirstNewick);
		}
		else if (newick.getComparisionMode().equals(NewickUtils.MATRIX_COMPARISION_MODE)) {
			arguments.add(NewickUtils.MATRIX_COMPARISION_MODE);
			comparisionMode = Mode.MATRIX;
			String normalizedFirstNewick = NewickUtils.GetNormalizedNewickString(newick.getNewickStringFirst());
			splitter = new NewickSplitter(normalizedFirstNewick);
		}
		else if (newick.getComparisionMode().equals(NewickUtils.OVERLAPPING_PAIR_COMPARISION_MODE)) {
			arguments.add(NewickUtils.OVERLAPPING_PAIR_COMPARISION_MODE);
			comparisionMode = Mode.OVERLAPPING_PAIR;
			String normalizedFirstNewick = NewickUtils.GetNormalizedNewickString(newick.getNewickStringFirst());
			splitter = new NewickSplitter(normalizedFirstNewick);
		}
		else if (newick.getComparisionMode().equals(NewickUtils.REF_TO_ALL_COMPARISION_MODE)) {
			arguments.add(NewickUtils.REF_TO_ALL_COMPARISION_MODE);
			comparisionMode = Mode.REF_TO_ALL;
			refTreeFile = nu.createTempFileWithGivenContent(newick.getNewickStringSecond());

			String normalizedFirstNewick = NewickUtils.GetNormalizedNewickString(newick.getNewickStringFirst());
			splitter = new NewickSplitter(normalizedFirstNewick);
			String normalizedSecondNewick = NewickUtils.GetNormalizedNewickString(newick.getNewickStringSecond());
			referencedTreesSplitter = new NewickSplitter(normalizedSecondNewick);

			arguments.add(String.format("%s", refTreeFile.getAbsolutePath()));
		}

		NewickValidator newickVal = new NewickValidator(newick);

		newickVal.validate(inputFile, refTreeFile);

		if (!newickVal.getErrors().isEmpty()) {
			for (ObjectError objErr : newickVal.getErrors()) {
				bindingResult.addError(objErr);
			}
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute("rootedMetrics", confParser.getAvailableRootedMetricsWithCmd());
			model.addAttribute("unrootedMetrics", confParser.getAvailableUnrootedMetricsWithCmd());

			return new ModelAndView("inputform", "newickStringNew", newick);
		} else {
			addMetricsToArguments(newick);

			// Input output files section

			arguments.add((String.format("%s", INPUT_FILE)));
			arguments.add((String.format("%s", inputFile.getAbsolutePath())));

			String outputFilePath = String.format("%s.out", inputFile.getAbsolutePath());

			arguments.add((String.format("%s", OUTPUT_FILE)));
			arguments.add(outputFilePath);

			// end section

			// Optional options section -N -P -I

			if (newick.isNormalizedDistances()) {
				arguments.add((String.format("%s", NORMALIZED_DISTANCES)));
			}

			if (newick.isPruneTrees()) {
				arguments.add(String.format("%s", PRUNE_COMPARED));
			}
			Boolean includeSummary = false;

			if (newick.isIncludeSummary()) {
				arguments.add(String.format("%s", INCLUDE_SUMMARY));
				includeSummary = true;
			}

			String[] argumentsToArray = new String[arguments.size()];
			arguments.toArray(argumentsToArray);
			TreeCmpExecutor executor;
			try {

				if (configFile == "") {
					configFile = this.getClass().getClassLoader().getResource("static/config/config.xml").getPath();
				}
				if (dataDir == "") {
					dataDir = this.getClass().getClassLoader().getResource("static/data").getPath();
				}

				executor = new TreeCmpExecutor(argumentsToArray, configFile, dataDir);
				executor.Execute();

				inputFile.delete();
			} catch (Exception e) {
				//e.printStackTrace();
			}

			Scanner outputFileScanner = new Scanner(new File(outputFilePath));

			if(outputFileScanner.hasNext()) {
                rawReport = outputFileScanner.useDelimiter("\\Z").next();
				String myReport = htmlUtils.GenerateReportTable(rawReport, includeSummary);
				model.addAttribute("report", myReport);
			}
			else {
				model.addAttribute("report", "<br/><h3><center>Program return a blank report</center></h3><p><center><h4>Probably inputted trees have some different leaves. Please check the leaves labels or use \"Prune trees\" option.</h4></p></center><br/> ");
			}
			outputFileScanner.close();

			return new ModelAndView("report", model);
		}
	}

	@RequestMapping(value="/rawReport", method=RequestMethod.POST)
    public @ResponseBody String rawReport() {
        return rawReport;
    }

	@RequestMapping(value = "/trees/", method = RequestMethod.GET)
	public ModelAndView visualizeTrees(@RequestParam Map<String, String> treesIds, Model model) {
		try	{
			int firstTreeId = Integer.parseInt(treesIds.get("firstTreeId"));
			model.addAttribute("firstTreeId", firstTreeId);

			int secondTreeId = Integer.parseInt(treesIds.get("secondTreeId"));
			model.addAttribute("secondTreeId", secondTreeId);

			String shortTable = htmlUtils.GenerateShortenedReport(rawReport, firstTreeId, secondTreeId);
			model.addAttribute("shortTable", shortTable);
		}
		catch(Exception e) {
		}

		return new ModelAndView("trees", "JsonTrees", new JsonTrees());
	}

	@RequestMapping(value="/trees", method=RequestMethod.POST)
	public @ResponseBody JsonTrees provideTree(@RequestBody final JsonTrees tree) {
		if(comparisionMode == Mode.REF_TO_ALL) {
			tree.firstTreeNewick = splitter.GetTree(tree.secondTreeId-1);
			tree.secondTreeNewick = referencedTreesSplitter.GetTree(tree.firstTreeId-1);
		}
		else {
			tree.firstTreeNewick = splitter.GetTree(tree.firstTreeId-1);
			tree.secondTreeNewick = splitter.GetTree(tree.secondTreeId-1);
		}

		return tree;
	}

	private void addMetricsToArguments(Newick newick) {
		arguments.add(String.format("%s", METRICS));

		StringBuilder sb = new StringBuilder();

		for (String rootedMetric : newick.getRootedMetrics()) {
			sb.append(String.format("%s ", rootedMetric));
		}
		for (String unrootedMetric : newick.getUnrootedMetrics()) {
			sb.append(String.format("%s ", unrootedMetric));
		}

		//Remove additional white space at the end
		sb.deleteCharAt(sb.length() - 1);

		arguments.add(sb.toString());
	}
}