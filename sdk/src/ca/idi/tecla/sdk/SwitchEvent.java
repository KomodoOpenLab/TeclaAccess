/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tecla.sdk;

import android.os.Bundle;

public class SwitchEvent {
	
	/**
	 * Intent string used to broadcast switch events. The
	 * type of event will be packaged as an extra using
	 * the {@link #EXTRA_SWITCH_CHANGES} and {@link #EXTRA_SWITCH_STATES} extras.
	 */
	public static final String ACTION_SWITCH_EVENT_RECEIVED = "ca.idi.tekla.sdk.action.SWITCH_EVENT_RECEIVED";
	public static final String EXTRA_SWITCH_CHANGES = "ca.idi.tekla.sdk.extra.SWITCH_CHANGES";
	public static final String EXTRA_SWITCH_STATES = "ca.idi.tekla.sdk.extra.SWITCH_STATES";
	
	// MASKS FOR READING SWITCH STATES
	public static final int SWITCH_J1 = 0x01; //Forward / Up
	public static final int SWITCH_J2 = 0x02; //Back / Down
	public static final int SWITCH_J3 = 0x04; //Left
	public static final int SWITCH_J4 = 0x08; //Right
	public static final int SWITCH_E1 = 0x10;
	public static final int SWITCH_E2 = 0x20;

	private int switch_changes, switch_states;
	
	public SwitchEvent(Bundle bundle) {
		switch_changes = bundle.getInt(EXTRA_SWITCH_CHANGES);
		switch_states = bundle.getInt(EXTRA_SWITCH_STATES);
	}
	
	public int getSwitchChanges() {
		return switch_changes;
	}
	
	public int getSwitchStates() {
		return switch_states;
	}
	
	public boolean isPressed(int bitmask) {
		if ((switch_changes & bitmask) == bitmask) {
			// Switch changed
			if ((switch_states & bitmask) != bitmask) {
				// Switch pressed
				return true;
			}
		}
		return false;
	}
		
	public boolean isReleased(int bitmask) {
		if ((switch_changes & bitmask) == bitmask) {
			// Switch changed
			if ((switch_states & bitmask) == bitmask) {
				// Switch released
				return true;
			}
		}
		return false;
	}
		
}
