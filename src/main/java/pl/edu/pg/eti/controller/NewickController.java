package pl.edu.pg.eti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.util.DateUtils;
import pl.edu.pg.eti.model.ComparisonMode;
import pl.edu.pg.eti.model.JsonTrees;
import pl.edu.pg.eti.model.Newick;
import pl.edu.pg.eti.model.WorkMode;
import pl.edu.pg.eti.utils.*;
import treecmp.config.ActiveMetricsSet;
import treecmp.config.IOSettings;

import javax.servlet.ServletContext;
import javax.validation.Valid;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

@Controller
@Scope("session")
public class NewickController  {

	private static final String METRICS = "-d";
	private static final String NORMALIZED_DISTANCES = "-N";
	private static final String PRUNE_COMPARED = "-P";
	private static final String INCLUDE_SUMMARY = "-I";
	private static final String ZERO_WEIGHTS_ALLOWED = "-W";
	private static final String BIFURCATING_TREES_ONLY = "-B";
	private static final String INPUT_FILE = "-i";
	private static final String OUTPUT_FILE = "-o";

	@Autowired
	ServletContext servletContext;

	@Value("${pl.pg.edu.eti.mode:SERVLET_CONTAINER}")
    private WorkMode workMode;

	private List<String> arguments = new ArrayList<String>();
	private static ConfigParser confParser = new ConfigParser();
	private HtmlUtils htmlUtils = new HtmlUtils();

	private NewickSplitter splitter;
	private NewickSplitter referencedTreesSplitter;
    private static String rawReport;
	private ComparisonMode comparisionMode;

	private static String configFile = "";
	private static String dataDir = "";

	private ModelMap lastModel;
	private Newick lastNewick;

	private static void CopyDataAndConfigFilesToTemporary() {

		String actualDate = DateUtils.createNow(Locale.getDefault()).getTime().toString().replace(" ", "_").replace(":", "_");
		Path tempDirPath = null;
		try {
			tempDirPath = Files.createTempDirectory("treeCmp_" + actualDate + "_" );
		} catch (IOException e) {
			e.printStackTrace();
		}


// --- Kopiowanie config.xml ---
        Path configFilePath = tempDirPath.resolve("config.xml");
        try (InputStream configFileStream = new ClassPathResource("static/config/config.xml").getInputStream()) {
            Files.copy(configFileStream, configFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Config file copied to: " + configFilePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

// --- Przygotowanie katalogu data ---
        Path dataDirPath = tempDirPath.resolve("data");
        try {
            Files.createDirectories(dataDirPath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

// --- Kopiowanie plików z static/data ---
        for (String metricFile : confParser.getAvailableMetricsFiles()) {
            Path targetFile = dataDirPath.resolve(metricFile);
            try (InputStream dataFileStream = new ClassPathResource("static/data/" + metricFile).getInputStream()) {
                Files.copy(dataFileStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Data file copied: " + targetFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

// Jeżeli potrzebujesz ścieżki do TreeCmpExecutor lub innych klas jako String:
        String configFile = configFilePath.toString();
        String dataDir = dataDirPath.toString();

    }

	private static void ShowMessageAboutSuccessfulStartup() {
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
		Newick newick = null;
		if (lastNewick == null) {
			newick = new Newick();
		}
		else {
			newick = lastNewick;
		}

		return new ModelAndView("inputform", "newickStringNew", newick);
	}

	@RequestMapping(value = "/report", method = RequestMethod.GET)
	public ModelAndView report2(
			@ModelAttribute("newickStringNew") @Valid Newick newick,
			BindingResult bindingResult,
			ModelMap model) throws IOException {
		model = lastModel;
		return new ModelAndView("report", model);
	}

    @RequestMapping(value = "/report", method = RequestMethod.POST)
    public ModelAndView report(
            @ModelAttribute("newickStringNew") @Valid Newick newick,
            BindingResult bindingResult,
            ModelMap model) throws IOException {

        NewickUtils nu = new NewickUtils();

        if (arguments != null) arguments.clear();

        // --- 1. Ustawienia opcji ---
        if (newick.isNormalizedDistances()) arguments.add(NORMALIZED_DISTANCES); else arguments.remove(NORMALIZED_DISTANCES);
        if (newick.isPruneTrees()) arguments.add(PRUNE_COMPARED); else arguments.remove(PRUNE_COMPARED);
        Boolean includeSummary = newick.isIncludeSummary();
        if (includeSummary) arguments.add(INCLUDE_SUMMARY); else arguments.remove(INCLUDE_SUMMARY);
        IOSettings.getIOSettings().setZeroValueWeights(newick.isZeroWeightsAllowed());
        IOSettings.getIOSettings().setBifurcatingOnly(newick.isBifurcationTreesOnly());

        // --- 2. Tworzenie pliku wejściowego .newick ---
        File inputFile = nu.createTempFileWithGivenContent(newick.getNewickStringFirst());
        System.out.println(">>> Creating file at: " + inputFile.getAbsolutePath());
        File refTreeFile = null;

        // --- 3. Tryb porównania ---
        String normalizedFirstNewick = NewickUtils.GetNormalizedNewickString(newick.getNewickStringFirst());
        switch (newick.getComparisionMode()) {
            case NewickUtils.WINDOW_COMPARISION_MODE:
                arguments.add(NewickUtils.WINDOW_COMPARISION_MODE);
                comparisionMode = ComparisonMode.WINDOW;
                arguments.add(String.valueOf(newick.getWindowWidth()));
                splitter = new NewickSplitter(normalizedFirstNewick);
                break;
            case NewickUtils.MATRIX_COMPARISION_MODE:
                arguments.add(NewickUtils.MATRIX_COMPARISION_MODE);
                comparisionMode = ComparisonMode.MATRIX;
                splitter = new NewickSplitter(normalizedFirstNewick);
                break;
            case NewickUtils.OVERLAPPING_PAIR_COMPARISION_MODE:
                arguments.add(NewickUtils.OVERLAPPING_PAIR_COMPARISION_MODE);
                comparisionMode = ComparisonMode.OVERLAPPING_PAIR;
                splitter = new NewickSplitter(normalizedFirstNewick);
                break;
            case NewickUtils.REF_TO_ALL_COMPARISION_MODE:
                arguments.add(NewickUtils.REF_TO_ALL_COMPARISION_MODE);
                comparisionMode = ComparisonMode.REF_TO_ALL;
                refTreeFile = nu.createTempFileWithGivenContent(newick.getNewickStringSecond());
                splitter = new NewickSplitter(normalizedFirstNewick);
                String normalizedSecondNewick = NewickUtils.GetNormalizedNewickString(newick.getNewickStringSecond());
                referencedTreesSplitter = new NewickSplitter(normalizedSecondNewick);
                arguments.add(refTreeFile.getAbsolutePath());
                break;
            default:
                throw new IllegalArgumentException("Unknown comparison mode: " + newick.getComparisionMode());
        }

        // --- 4. Przygotowanie config.xml i folderu data w /tmp ---
        Path tempDirPath = Paths.get(System.getProperty("java.io.tmpdir"));
        Path configFilePath = tempDirPath.resolve("config.xml");
        try (InputStream configStream = getClass().getClassLoader()
                .getResourceAsStream("static/config/config.xml")) {
            if (configStream == null) throw new FileNotFoundException("Config file not found in classpath!");
            Files.copy(configStream, configFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

        Path dataDirPath = tempDirPath.resolve("data");
        Files.createDirectories(dataDirPath);

        for (String metricFile : confParser.getAvailableMetricsFiles()) {
            Path targetFile = dataDirPath.resolve(metricFile);
            try (InputStream dataFileStream = getClass().getClassLoader()
                    .getResourceAsStream("static/data/" + metricFile)) {
                if (dataFileStream == null) {
                    System.err.println("Metric file not found in classpath: " + metricFile);
                    continue;
                }
                Files.copy(dataFileStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // --- 5. Aktualizacja metryk ---
        try (InputStream configFileStream = getClass().getClassLoader()
                .getResourceAsStream("static/config/config.xml")) {
            if (configFileStream != null) {
                confParser.setMetricConfigFileStream(configFileStream);
                confParser.clearAndSetAvailableMetrics();
            }
        }

        ActiveMetricsSet AMSet = ActiveMetricsSet.getActiveMetricsSet();
        AMSet.clearAllMetrics();
        AMSet.setActiveMetricsSet(newick.getRootedMetrics());
        AMSet.setActiveMetricsSet(newick.getUnrootedMetrics());

        // --- 6. Walidacja Newick ---
        NewickValidator newickVal = new NewickValidator(newick);
        newickVal.validate(inputFile, refTreeFile, workMode != WorkMode.STANDALONE);
        if (!newickVal.getErrors().isEmpty()) {
            newickVal.getErrors().forEach(bindingResult::addError);
            model.addAttribute("rootedMetrics", confParser.getAvailableRootedMetricsWithCmd());
            model.addAttribute("unrootedMetrics", confParser.getAvailableUnrootedMetricsWithCmd());
            return new ModelAndView("inputform", "newickStringNew", newick);
        }

        addMetricsToArguments(newick);

        // --- 7. Przygotowanie executor ---
        arguments.add(INPUT_FILE);
        arguments.add(inputFile.getAbsolutePath());
        String outputFilePath = inputFile.getAbsolutePath() + ".out";
        arguments.add(OUTPUT_FILE);
        arguments.add(outputFilePath);

        try {
            TreeCmpExecutor executor = new TreeCmpExecutor(
                    arguments.toArray(new String[0]),
                    configFilePath.toString(),
                    dataDirPath.toString()
            );
            executor.Execute();
            inputFile.delete();
        } catch (Exception e) {
            bindingResult.addError(new FieldError("newick", "generalError", e.getMessage()));
            model.addAttribute("rootedMetrics", confParser.getAvailableRootedMetricsWithCmd());
            model.addAttribute("unrootedMetrics", confParser.getAvailableUnrootedMetricsWithCmd());
            return new ModelAndView("inputform", "newickStringNew", newick);
        }

        // --- 8. Odczyt outputu ---
        File outputFile = new File(outputFilePath);
        if (!outputFile.exists()) {
            throw new IOException("Expected output file not found: " + outputFilePath);
        }

        try (Scanner outputFileScanner = new Scanner(outputFile)) {
            String rawReport = outputFileScanner.useDelimiter("\\Z").next();
            String myReport = htmlUtils.GenerateReportTable(rawReport, includeSummary);
            model.addAttribute("report", myReport);
        }

        lastModel = model;
        lastNewick = newick;

        return new ModelAndView("report", model);
    }



    @RequestMapping(value="/rawReport", method=RequestMethod.POST)
    public @ResponseBody String rawReport() {
        return rawReport;
    }

	@RequestMapping(value = "/trees", method = RequestMethod.GET)
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
		if(comparisionMode == ComparisonMode.REF_TO_ALL) {
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
