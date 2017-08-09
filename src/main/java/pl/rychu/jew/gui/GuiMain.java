package pl.rychu.jew.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.jew.gui.hi.HiConfigProviderPer;
import pl.rychu.jew.gui.panels.InfoPanel;
import pl.rychu.jew.gui.panels.StatusPanel;
import pl.rychu.jew.gui.pars.*;
import pl.rychu.jew.linedec.LineDecoderCfg;
import pl.rychu.jew.logaccess.LogAccess;
import pl.rychu.jew.logaccess.LogAccessFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class GuiMain {

	private static final Logger log = LoggerFactory.getLogger(GuiMain.class);

	public static void runGuiAsynchronously(String filename
	 , boolean isWindows, String initFilter) {
		SwingUtilities.invokeLater(() -> {
			JFrame mainFrame = createFrame(filename, isWindows, initFilter);
			mainFrame.setVisible(true);
		});
	}

	private static JFrame createFrame(String filename
	 , boolean isWindows, String initFilter) {
		ParsConfigProvider parsConfigProvider = new ParsConfigProviderPer();
		ParsConfig parsConfig = parsConfigProvider.get();
		ParsConfigEntry pce = parsConfig.get(0);
		LineDecoderCfg lineDecoderCfg = cfgParsConfig(pce);

		final LogAccess logAccess = LogAccessFile.create(filename
		 , isWindows, lineDecoderCfg);

		final JFrame mainFrame = new JFrame("jew");

		mainFrame.setLayout(new BorderLayout());

		final ListModelLog model = ListModelLog.create(logAccess);

		final LogViewPanel logViewPanel = LogViewPanel.create(model, initFilter);
		logViewPanel.setHiConfigProvider(new HiConfigProviderPer());

		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		mainFrame.add(topPanel, BorderLayout.NORTH);

		StatusPanel statusPanel = StatusPanel.create();
		mainFrame.add(statusPanel, BorderLayout.SOUTH);

		final InfoPanel infoPanel = InfoPanel.create(logViewPanel);
		topPanel.add(infoPanel, BorderLayout.CENTER);

		logViewPanel.setMessageConsumer(statusPanel);
		logViewPanel.addListSelectionListener(infoPanel);
		model.addCyclicModelListener(infoPanel);
		model.addPanelModelChangeListener(infoPanel);
		model.addCyclicModelListener(logViewPanel);
		TitleHandler titleHandler = new TitleHandler(mainFrame, filename);
		model.addCyclicModelListener(titleHandler);

		final JScrollPane scrollPane = new JScrollPane(logViewPanel);
		scrollPane.setPreferredSize(new Dimension(900, 600));
		scrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.fastWheelScrolling", true);
		scrollPane.getViewport().addComponentListener(new ComponentLsn(logViewPanel));
		scrollPane.getHorizontalScrollBar().addAdjustmentListener(e -> logViewPanel
		 .viewportHorizontalMove(e.getValue()));
		mainFrame.add(scrollPane, BorderLayout.CENTER);

		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle("Java log viEW");

		Image image = loadImage();
		if (image != null) {
			mainFrame.setIconImage(image);
		}

		Map<KeyStroke, Action> actions = new HashMap<>();
		{
			actions.put(KeyStroke.getKeyStroke('I'), new AbstractAction("frame.titleToggle") {
				private static final long serialVersionUID = 4836966177487679465L;
				@Override
				public void actionPerformed(ActionEvent e) {
					titleHandler.toggleTitleMode();
				}
			});
			actions.put(KeyStroke.getKeyStroke('P', InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), new
			 AbstractAction("jew.pars.dialog") {
				private static final long serialVersionUID = -8360999122789471879L;
				@Override
				public void actionPerformed(ActionEvent e) {
					ParsConfig pc = parsConfigProvider.get();
					String currentLine = logViewPanel.getCurrentLineContent();
					ParsDialog dialog = new ParsDialog((JFrame) logViewPanel.getTopLevelAncestor(), pc,
					 currentLine, parsConfigProvider::put, pce -> logAccess.reconfig(cfgParsConfig(pce)));
					// execution continues here after closing the dialog
					dialog.dispose();
				}
			});
		}
		setKeysAndActions(scrollPane, actions);

		return mainFrame;
	}

	private static Image loadImage() {
		LocalDate date = LocalDate.now();
		boolean isDay = date.getDayOfMonth()==28 && date.getMonth()== Month.OCTOBER;
		String iconStr = isDay ? "/yep.png" : "/pile_mag_128.png";
		InputStream is = GuiMain.class.getResourceAsStream(iconStr);
		if (is == null) {
			return null;
		}
		try {
			return ImageIO.read(is);
		} catch (IOException e) {
			log.warn("cannot load image");
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				log.warn("cannot close image stream", e);
			}
		}
	}

	private static void setKeysAndActions(JComponent c, Map<KeyStroke, Action> actions) {
		ActionMap actionMap = new ActionMap();
		ActionMap oldActionMap = c.getActionMap();
		actionMap.setParent(oldActionMap);
		c.setActionMap(actionMap);

		InputMap inputMap = new InputMap();
		InputMap oldInputMap = c.getInputMap();
		inputMap.setParent(oldInputMap);
		c.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);

		actions.forEach((keyStroke, action) -> {
			Object key = action.getValue(Action.NAME);
			actionMap.put(key, action);
			inputMap.put(keyStroke, key);
		});
	}

	private static LineDecoderCfg cfgParsConfig(ParsConfigEntry pce) {
		Pattern pattern = pce.getCompiledPatternOrNull();
		return pattern == null ? null : new LineDecoderCfg(pattern, pce.getGroupTime(), pce
		 .getGroupLevel(), pce.getGroupClass(), pce.getGroupThread(), pce.getGroupMessage());
	}

	// ==============

	private static class TitleHandler implements CyclicModelListener {

		private final JFrame mainFrame;
		private final String filename;
		private final String path;
		private int mode = 0;

		private long linesSource = -1;
		private long linesFiltered = -1;

		private TitleHandler(JFrame mainFrame, String path) {
			this.mainFrame = mainFrame;
			this.path = path;
			this.filename = getFilename(path);
		}

		@Override
		public void linesAddedStart(int numberOfLinesAdded, long totalLines) {
			linesFiltered = totalLines;
			computeAndSetTitle();
		}

		@Override
		public void linesAddedEnd(int numberOfLinesAdded, long totalLines) {
			linesFiltered = totalLines;
			computeAndSetTitle();
		}

		@Override
		public void listReset(boolean sourceReset) {}

		@Override
		public void sourceChanged(long number) {
			linesSource = number;
			computeAndSetTitle();
		}

		void toggleTitleMode() {
			mode = (mode + 1) % 10;
			computeAndSetTitle();
		}

		private void computeAndSetTitle() {
			mainFrame.setTitle(computeTitle(linesSource, linesFiltered));
		}

		private String computeTitle(long linesSource, long linesFiltered) {
			String linesSrcStr = linesSource < 0 ? "-" : Long.toString(linesSource);
			String linesFltStr = linesFiltered < 0 ? "-" : Long.toString(linesFiltered);
			switch (mode) {
				case 0: return linesSrcStr+" - "+path;
				case 1: return linesSrcStr+" - "+filename;
				case 2: return linesSrcStr;
				case 3: return filename+" - "+linesSrcStr;
				case 4: return filename;
				case 5: return linesFltStr+" - "+path;
				case 6: return linesFltStr+" - "+filename;
				case 7: return linesFltStr;
				case 8: return filename+" - "+linesFltStr;
				case 9: return filename;
				default:
					return linesSrcStr;
			}
		}

		private String getFilename(String fullPath) {
			int index = Math.max(fullPath.lastIndexOf('/'), fullPath.lastIndexOf('\\'));
			return index>=0 ? fullPath.substring(index+1) : fullPath;
		}
	}

	private static class ComponentLsn implements ComponentListener {
		private final ViewportListener viewportListener;

		private ComponentLsn(ViewportListener viewportListener) {
			this.viewportListener = viewportListener;
		}

		@Override
		public void componentResized(ComponentEvent e) {
			viewportListener.viewportResized(e);
		}

		@Override
		public void componentMoved(ComponentEvent e) {}

		@Override
		public void componentShown(ComponentEvent e) {}

		@Override
		public void componentHidden(ComponentEvent e) {}
	}

}
