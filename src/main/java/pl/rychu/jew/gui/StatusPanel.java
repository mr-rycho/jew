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

	private JLabel label;

	private int counter = 0;

	private Queue<String> messages = new LinkedList<>();

	// ----------

	private StatusPanel() {
		super();

		setLayout(new FlowLayout(FlowLayout.LEFT));

		label = new JLabel(" ");

		add(label);
	}

	public static StatusPanel create() {
		StatusPanel result = new StatusPanel();

		new Thread(result.new Timer()).start();

		return result;
	}
	// ----------

	@Override
	public void enqueueMessage(String text) {
		if (counter == 0) {
			setLabelAndCounter(text);
		} else {
			messages.offer(text);
		}
	}

	private void setLabelAndCounter(String text) {
		label.setText(text);
		counter = MESSAGE_DUR;
	}

	private void clearLabel() {
		label.setText(" ");
		counter = 0;
	}

	private void timerTick() {
		if (counter > 0) {
			counter--;
			if (counter == 0) {
				pollAndSetOrClear();
			}
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
