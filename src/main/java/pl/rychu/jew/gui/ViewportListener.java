package pl.rychu.jew.gui;

import java.awt.event.ComponentEvent;

/**
 * Created on 01.08.2017.
 */
public interface ViewportListener {

	void viewportResized(ComponentEvent e);

	void viewportHorizontalMove(int x);

}
