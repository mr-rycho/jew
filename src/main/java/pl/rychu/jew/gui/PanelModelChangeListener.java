package pl.rychu.jew.gui;

import pl.rychu.jew.filter.LogLineFilterChain;

public interface PanelModelChangeListener {

	void panelChanged();

	void modelChanged(LogLineFilterChain filterChain);

}
