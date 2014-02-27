package ca.idi.tecla.sdk;

import android.os.Bundle;

public class SwitchEvent {

	/**
	 * Intent string used to broadcast switch events. The
	 * type of event will be packaged as an extra using
	 * the {@link #EXTRA_SWITCH_CHANGES} and {@link #EXTRA_SWITCH_STATES} extras.
	 */
	public static final String ACTION_SWITCH_EVENT_RECEIVED = "ca.idi.tecla.sdk.action.SWITCH_EVENT_RECEIVED";
	public static final String EXTRA_SWITCH_CHANGES = "ca.idi.tecla.sdk.extra.SWITCH_CHANGES";
	public static final String EXTRA_SWITCH_STATES = "ca.idi.tecla.sdk.extra.SWITCH_STATES";
	public static final String EXTRA_SWITCH_ACTIONS = "ca.idi.tecla.sdk.extra.SWITCH_ACTIONS";
	
	public static final int ACTION_NEXT = 1;
	public static final int ACTION_PREV = 2;
	public static final int ACTION_CANCEL = 3;
	public static final int ACTION_SELECT = 4;

	// MASKS FOR READING SWITCH STATES
	public static final int MASK_SWITCH_J1 = 0x01; //Forward / Up
	public static final int MASK_SWITCH_J2 = 0x02; //Back / Down
	public static final int MASK_SWITCH_J3 = 0x04; //Left
	public static final int MASK_SWITCH_J4 = 0x08; //Right
	public static final int MASK_SWITCH_E1 = 0x10;
	public static final int MASK_SWITCH_E2 = 0x20;

	public static final int SWITCH_STATES_DEFAULT = 0x3F;
	public static final int SWITCH_CHANGES_DEFAULT = 0x00;

	private static int switch_changes;
	private static int switch_states;
	
	public SwitchEvent(Bundle bundle) {
		switch_changes = bundle.getInt(EXTRA_SWITCH_CHANGES);
		switch_states = bundle.getInt(EXTRA_SWITCH_STATES);
	}
	
	public SwitchEvent(int states, int changes) {
		switch_changes = states;
		switch_states = changes;
	}
	
	public int getSwitchChanges() {
		return switch_changes;
	}
	
	public int getSwitchStates() {
		return switch_states;
	}
	
	public static boolean isPressed(int bitmask) {
		if ((switch_changes & bitmask) == bitmask) {
			// Switch changed
			if ((switch_states & bitmask) != bitmask) {
				// Switch pressed
				return true;
			}
		}
		return false;
	}
		
	public boolean isAnyPressed() {
		if (isPressed(MASK_SWITCH_E1)
				|| isPressed(MASK_SWITCH_E2)
				|| isPressed(MASK_SWITCH_J1)
				|| isPressed(MASK_SWITCH_J2)
				|| isPressed(MASK_SWITCH_J3)
				|| isPressed(MASK_SWITCH_J4)
				) {
			return true;
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
	
	public void setReleased(int mask) {
		switch_changes = mask;
		switch_states = SWITCH_STATES_DEFAULT;
	}
	
	public String toString() {
		if (switch_changes == MASK_SWITCH_J1) return "switch_j1";
		if (switch_changes == MASK_SWITCH_J2) return "switch_j2";
		if (switch_changes == MASK_SWITCH_J3) return "switch_j3";
		if (switch_changes == MASK_SWITCH_J4) return "switch_j4";
		if (switch_changes == MASK_SWITCH_E1) return "switch_e1";
		if (switch_changes == MASK_SWITCH_E2) return "switch_e2";
		return null;
	}
		
}
