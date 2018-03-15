/**
 * Author: Maleakhi Agung Wijaya
 * Student Number: 784091
 * Date: 15/03/2018
 */
package strategies;
import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;

/**
 * Defines the behaviour of the strong and weak robot, such as when to get back to the mail pool.
 */
public class MyRobotBehaviour implements IRobotBehaviour {
	/** Instance Variables */
	private boolean newPriority; // Used if we are notified that a priority item has arrived 
	private boolean strong; // Used to identify which type of robot 
	private int newPriorityLevel; // Used to compare the new priority level with the one on the tube
	
	/** Constant */
	public static final int WEAK_CARRY = 2000;
	
	/**
	 * Constructor for MyRobotBehaviour
	 * @param strong true if the robot is strong, false if the robot is weak
	 */
	public MyRobotBehaviour(boolean strong) {
		this.strong = strong;
		newPriority = false;
	}
	
	/**
	 * Initialise state in support of other methods
	 */
	public void startDelivery() {
		newPriority = false; // toggle off
	}
	
	/**
	 * Notify robot that a priority mail is arrived and has been put in the mail pool.
	 * @param priority - level of priority 10/100
	 * @param weight - weight of the particular mail
	 */
	@Override
    public void priorityArrival(int priority, int weight) {
    	// Only notify strong robot and weak robot if the weak robot can carry the weight
    	if (strong == true) {
    		newPriority = true;
    		this.newPriorityLevel = priority;
    	}
    	else if ((strong == false) && (weight <= WEAK_CARRY)) {
    		newPriority = true;
    		this.newPriorityLevel = priority;
    	}
    	// Else, don't notify
    	else {
    		newPriority = false;
    	}
    }
 
	/**
	 * Decide if the robot should return to the mail room.
	 * @param tube - the storage tube that the robot carry
	 * @return true if the robot needed to return, false otherwise
	 */
	@Override
	public boolean returnToMailRoom(StorageTube tube) {
		if (tube.isEmpty()) {
			return true; // Empty tube means we are returning anyway
		} else {
			// Return if we don't have a priority item and a new one came in
			MailItem item = tube.peek();
			Boolean priority = (item instanceof PriorityMailItem);
			return ((!priority && newPriority) || 
					(priority && newPriority && (newPriorityLevel > ((PriorityMailItem)item).getPriorityLevel())));
		}
	}
	
}
