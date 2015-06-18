package pl.rychu.jew;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.gui.GuiMain;



public class CmdLineMain {

	private static final Logger log = LoggerFactory.getLogger(CmdLineMain.class);

	// -------------------

	public static void main(final String... args) {
		Options options = createOptions();

		CommandLine cmdline = null;
		try {
			cmdline = new DefaultParser().parse(options, args, true);
		} catch (ParseException e) {
			System.err.println("error in commandline: "+e.getMessage());
			log.error("error parsing commandline", e);
		}

		if (cmdline==null || cmdline.hasOption("help")
		 || (!cmdline.iterator().hasNext() && cmdline.getArgList().isEmpty())) {
			printHelp(options);
		} else {
			String filename = null;
			try {
				filename = getFilename(cmdline);
				checkFile(filename);
				GuiMain.runGuiAsynchronously(filename);
			} catch (Exception e) {
				System.err.println("error: "+e.getMessage());
				log.error("error parsing commandline", e);
			}
		}
	}

	// -------------------

	private static Options createOptions() {
		Options options = new Options();

		options.addOption("?", "help", false, "prints this help");

		return options;
	}

	private static void printHelp(Options options) {
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp("java -jar ... [opts] [filename]", options);
	}

	private static String getFilename(CommandLine cmdline) {
		List<String> argList = cmdline.getArgList();
		if (argList.size() != 1) {
			throw new IllegalArgumentException("invalid commandline: "+argList);
		}
		return argList.get(0);
	}

	private static void checkFile(String filename) {
		File file = new File(filename);
		if (!file.exists()) {
			throw new IllegalStateException("file \""+filename+"\" does not exist");
		}
		if (!file.isFile()) {
			throw new IllegalStateException("file \""+filename+"\" is not a normal file");
		}
	}

}