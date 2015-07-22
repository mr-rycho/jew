package pl.rychu.jew;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.conf.LoggerType;
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
			try {
				String filename = getFilename(cmdline);
				checkFile(filename);
				boolean isWindows = isWindows(cmdline);
				log.debug("working operating system: {}", isWindows ? "windows" : "linux");
				LoggerType loggerType = getLoggerType(cmdline);
				log.debug("using logger type: {}", loggerType);
				GuiMain.runGuiAsynchronously(filename, isWindows, loggerType);
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
		options.addOption(null, "system", true, "sets system: LINUX, WINDOWS, AUTO");
		options.addOption(null, "logger", true, "sets logger: WILDFLY_STD");

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

	private static boolean isWindows(CommandLine cmdLine) {
		String sysOptStr = cmdLine.getOptionValue("system");
		SystemOption sysOpt = getSystemOption(sysOptStr);
		switch (sysOpt) {
			case LINUX: return false;
			case WINDOWS: return true;
			case AUTO: return detectWindows();
			default: throw new IllegalStateException("not supported: "+sysOpt);
		}
	}

	private static SystemOption getSystemOption(String sysOptStr) {
		try {
			sysOptStr = sysOptStr==null || sysOptStr.isEmpty()
			 ? SystemOption.AUTO.name() : sysOptStr;
			return SystemOption.valueOf(sysOptStr.toUpperCase());
		} catch (Exception e) {
			throw new IllegalArgumentException("bad option: \""+sysOptStr+"\". "
			 +"available: "+Arrays.asList(SystemOption.values()));
		}
	}

	private static boolean detectWindows() {
		String windir = System.getenv("windir");
		return windir!=null && !windir.isEmpty();
	}

	// ---

	private static LoggerType getLoggerType(CommandLine cmdLine) {
		String logOptStr = cmdLine.getOptionValue("logger");
		return getLoggerType(logOptStr);
	}

	private static LoggerType getLoggerType(String logOptStr) {
		logOptStr = logOptStr==null||logOptStr.isEmpty()
		 ? LoggerType.WILDFLY_STD.name() : logOptStr;
		try {
			return LoggerType.valueOf(logOptStr.toUpperCase());
		} catch (Exception e) {
			throw new IllegalArgumentException("bad logger: \""+logOptStr+"\". "
			 +"available: "+Arrays.asList(LoggerType.values()));
		}
	}

	// ====================

	private static enum SystemOption {
		LINUX, WINDOWS, AUTO;
	}

}
