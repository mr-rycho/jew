package pl.rychu.jew.gui;

import java.awt.FlowLayout;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;



public class StatusPanel extends JPanel implements MessageConsumer {

	private static final long serialVersionUID = -6532902484947042348L;

	private static final int MESSAGE_DUR = 5;

	private final JLabel label;

	private final boolean useQueue;

	private Long prevTs = null;

	private Queue<String> messages = new LinkedList<>();

	// ----------

	private StatusPanel(boolean useQueue) {
		super();

		this.useQueue = useQueue;

		setLayout(new FlowLayout(FlowLayout.LEFT));

		label = new JLabel("Press F1 for help");

		add(label);
	}

	public static StatusPanel create() {
		return create(false);
	}

	public static StatusPanel create(boolean useQueue) {
		StatusPanel result = new StatusPanel(useQueue);

		new Thread(result.new Timer(), "status-panel-thread").start();

		return result;
	}
	// ----------

	@Override
	public void enqueueMessage(String text) {
		if (prevTs==null || !useQueue) {
			setLabelAndCounter(text);
		} else {
			messages.offer(text);
		}
	}

	private void setLabelAndCounter(String text) {
		label.setText(text);
		prevTs = System.currentTimeMillis();
	}

	private void clearLabel() {
		label.setText(" ");
		prevTs = null;
	}

	private void timerTick() {
		if (prevTs!=null && System.currentTimeMillis()>prevTs+1000L*MESSAGE_DUR) {
			pollAndSetOrClear();
		}
	}

	private void pollAndSetOrClear() {
		if (messages.isEmpty()) {
			clearLabel();
		} else {
			setLabelAndCounter(messages.poll());
		}
	}

	// ==============

	private class Timer implements Runnable {
		@Override
		public void run() {
			while (true) {
				timerTickAsync();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private void timerTickAsync() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					timerTick();
				}
			});
		}
	}

}
