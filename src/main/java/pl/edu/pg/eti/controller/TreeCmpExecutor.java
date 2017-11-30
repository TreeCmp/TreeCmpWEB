package pl.edu.pg.eti.controller;

import java.io.*;

import treecmp.command.Command;
import treecmp.commandline.CommandLineParser;
import treecmp.common.ReportUtils;
import treecmp.common.TimeDate;
import treecmp.common.TreeCmpException;
import treecmp.config.ActiveMetricsSet;
import treecmp.config.ConfigSettings;
import treecmp.config.IOSettings;
import treecmp.io.ResultWriter;
import treecmp.io.TreeReader;
import treecmp.metric.Metric;

public class TreeCmpExecutor {

	private String[] args;
	private String configFile;
	private String dataDir;

	public TreeCmpExecutor(String[] args, String configFile, String dataDir) {
		this.args = args;
		this.configFile = configFile;
		this.dataDir = dataDir;
	}

	public void Execute() {

		String runtimePathTemp = treecmp.Main.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		if (runtimePathTemp.indexOf('+') != -1) {
			System.out
					.println("Unsupported character in TreeCmp path!\n"
							+ "Please move TreeCmp application to a location that does not contain plus (+) character.");
			return;
		}

		try {
			ConfigSettings.initConfig(configFile, dataDir);
		} catch (FileNotFoundException ex) {
			//can not find the configuration file
			System.out.println(ex.getMessage());
			return;
		}
		
		Command cmd = null;
		try {
			cmd=CommandLineParser.run(args);	
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		if (cmd != null) {
			IOSettings settings = IOSettings.getIOSettings();
			// init data if needed
			if (settings.isRandomComparison()) {
				System.out.println(TimeDate.now()
						+ ": Start of reading random statistics.");
				for (Metric m : ActiveMetricsSet.getActiveMetricsSet()
						.getActiveMetrics()) {
					m.initData();
				}
				System.out.println(TimeDate.now()
						+ ": End of reading random statistics.");
			}

			TreeReader reader = new TreeReader(settings.getInputFile());
			// scanning all content of the input file

			System.out.println(System.getProperty("user.dir"));
			
			if (reader.open() == -1) {
				// an error occured during reading the input file
				return;
			}

			System.out.println(TimeDate.now()
					+ ": Start of scanning input file: "
					+ settings.getInputFile());
			int numberOfTrees = reader.scan();
			reader.close();
			System.out.println(TimeDate.now()
					+ ": End of scanning input file: "
					+ settings.getInputFile());
			System.out.println(TimeDate.now() + ": " + numberOfTrees
					+ " valid trees found in file: " + settings.getInputFile());

			reader.setStep(settings.getIStep());
			cmd.setReader(reader);

			ResultWriter out = new ResultWriter();
			out.isWriteToFile(true);
			out.setFileName(settings.getOutputFile());
			cmd.setOut(out);

			ReportUtils.update();

			try {
				cmd.run();
			} catch (TreeCmpException ex) {
				System.out.println(ex.getError());
			}
		}
	}
}
