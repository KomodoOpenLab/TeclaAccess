package ca.idi.tekla.ime;

public class SwitchEventManager {

// receive broadcast from switch event provider 
	/*
	 * Check who has control of the events (current profile)
	 * Send message to the process
	 */
	
// receive message (maybe broadcast) from SDK for taking/giving control
	/*
	 * Receive message for whether process is taking or giving control
	 * 		if taking{
	 * 			if current ID exists
	 * 			{
	 * 				current profile is received profile
	 * 			}
	 * 			else if (current ID doesn't exist){
	 * 				new profile is created
	 * 				new profile is stored 
	 * 				current profile is the new profile
	 * 			}
	 * 		}
	 * 		else if giving control{
	 * 			current profile is default profile
	 * 		}
	 * 
	 */
	

// check who has control of the switch events
	/*
	 * return profile ID 
	 */

	
// send switch event (X) to ID (Y)
	/*
	 * call method in
	 * 
	 */

}

