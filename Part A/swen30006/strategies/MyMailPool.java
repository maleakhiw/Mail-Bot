/**
 * Author: Maleakhi Agung Wijaya
 * Student Number: 784091
 * Date: 15/03/2018
 */
package strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

/** Implements the interface mail pool and define the behaviour of the system in mail pool which are located on the ground 
 * level. Mainly focuses on the MailSelecting system to decide what mail items should go in to storage unit/ robot backpack.
 * 
 * Algorithm of MailPool: 
 * 1. The objective of the mailing pool is to minimise the system scoring function, hence create an ArrayList which are sorted 
 *    based on urgency and priority defined by the WeightComparator (i.e. priority 100 is more important to deliver first than
 *    priority 10 item, hence 100 should be in the head of ArrayList).
 * 2. The second consideration is regarding adding item from mail pool to storage tube, we want to add as many item as possible
 *    to the storage tube respecting the condition that the storage tube limit is 4 items and weak robot only carry maximum 
 *    2000.
 * 3. After putting all of the item in the tube, for efficiency we want to manipulate the storage tube (stack) such that the
 *    robot will deliver item sequentially from lower level item to higher level item, so that the robot doesn't go back and 
 *    forth.
 */
public class MyMailPool implements IMailPool{
	/** Instance Variables */
	/* The data structure chosen is array list that are sorted based on urgency defined in the WeightComparator class. The head 
	 * of the array list will be the higher priority that needed to be delivered first (i.e. priority 100 first before priority
	 * 10)
	 */
	private ArrayList<MailItem> nonPriorityPool;
	private ArrayList<MailItem> priorityPool;
	private WeightComparator comparator;
	
	/** Constant */
	private static final int MAX_TAKE = 4;
	public static final double EXPONENT = 1.1;
	public static final int POSITIVE = 1;
	public static final int NEUTRAL = 0;
	public static final int NEGATIVE = -1;
	public static final int HEAD = 0;
	
	/**
	 * Constructor for MyMailPool which are used to instantiate appropriate PriorityQueue
	 */
	public MyMailPool(){
		// Instantiate the compare object used to determine item who has highest priority, Comparator class are described below
		nonPriorityPool = new ArrayList<MailItem>();
		priorityPool = new ArrayList<MailItem>();
		comparator = new WeightComparator(); 
	}

	/**
	 * Used to add item to the appropriate pool (priority or non priority)
	 * @param mailItem - mail item containing the mail and some attributes of the mail such as destination, arrival time, etc.
	 */
	public void addToPool(MailItem mailItem) {
		// Check types of item being added and put to appropriate pool
		if(mailItem instanceof PriorityMailItem){
			priorityPool.add(mailItem);
			
			// Reverse sort based on urgency to make sure higher priority item delivered first
			Collections.sort(priorityPool, comparator);
		}
		else{
			// Reverse sort for the same reason as above
			nonPriorityPool.add(mailItem);
			Collections.sort(nonPriorityPool, comparator);
		}
	}
	
	/**
	 * Get the pool size of the non priority priority queue with elements that satisfy the current weight limit
	 * @param weightLimit - weight limit which the robot can carry
	 * @return size of the non priority pool which the robot can carry according to the weight limit
	 */
	private int getNonPriorityPoolSize(int weightLimit) {
		// Iterate through the non priority pq and calculate how many items that can be satisfied
		int size = 0;
		for (MailItem mail:nonPriorityPool) {
			if (mail.getWeight() <= weightLimit) {
				size ++;
			}
		}
		
		return size;
	}
	
	/**
	 * Get the pool size of the priority priority queue with elements that satisfy the current weight limit
	 * @param weightLimit - weight limit which the robot can carry
	 * @return size of the priority pool which the robot can carry according to the weight limit
	 */
	private int getPriorityPoolSize(int weightLimit){
		int size = 0;
		for (MailItem mail:priorityPool) {
			if (mail.getWeight() <= weightLimit) {
				size++;
			}
		}
		
		return size;
	}

	/**
	 * Used to get a non priority mail from the non priority pool array list, only get item that the robot can still take.
	 * @param weightLimit
	 * @return MailItem instance that the robot can take from non priority pool
	 */
	private MailItem getNonPriorityMail(int weightLimit){
		ArrayList<MailItem> mails = new ArrayList<MailItem>();
		MailItem mail;
		
		if(getNonPriorityPoolSize(weightLimit) > 0){
			/* Only take item that the robot can handle based on weight if the robot cannot handle then put on the array list 
			 * so that later can be put back to the mail pool.
			 */
			mail = nonPriorityPool.remove(HEAD); // take the most urgent item
			while (mail.getWeight() > weightLimit) {
				mails.add(mail);
				mail = nonPriorityPool.remove(HEAD);
			}
			
			// Put back all mail on the mails back to the pool
			for (MailItem m:mails) {
				nonPriorityPool.add(m);
			}
			
			// Sort back non priority pool to satisfy the sorting criteria
			Collections.sort(nonPriorityPool, comparator);

			return mail; // Return the mail that satisfy the weight criterion
		}
		else{
			return null;
		}
	}
	
	/**
	 * Used to get a priority mail from the priority pool array list, only get item that the robot can still take.
	 * @param weightLimit
	 * @return MailItem instance that the robot can take from the priority pool
	 */
	private MailItem getHighestPriorityMail(int weightLimit){
		ArrayList<MailItem> mails = new ArrayList<MailItem>();
		MailItem mail;
		
		if(getPriorityPoolSize(weightLimit) > 0){
			/* Only take item that the robot can handle based on weight if the robot cannot handle then put on the array list 
			 * so that later can be put back to the mailpool.
			 */
			mail = priorityPool.remove(HEAD);
			while (mail.getWeight() > weightLimit) {
				mails.add(mail);
				mail = priorityPool.remove(HEAD);
			}
			
			// Put back all mail on the mails back to the priority queue
			for (MailItem m:mails) {
				priorityPool.add(m);
			}
			
			// Sort back priority pool
			Collections.sort(priorityPool, comparator);
			
			return mail; // Return the mail that satisfy the weight criterion
		}
		else{
			return null;
		}
	}

	/**
	 * Fill the storage tube of the robot, firstly by using the priority mail and deliver immediately, or if it is not possible
	 * , by filling at most 4 non priority item
	 * @param tube - Storage tube of the robot
	 * @param strong - Indicate strong or weak robot
	 */
	@Override
	public void fillStorageTube(StorageTube tube, boolean strong) {
		int max = strong ? Integer.MAX_VALUE : MyRobotBehaviour.WEAK_CARRY; // max weight
		int weight = max; // Keep track of the new weight limit that the robot can handle after putting things on the storage
		MailItem mail;
		
		// Priority items are important;
		// if there are some, grab one and go, otherwise take as many items as we can and go
		try{
			// Start afresh by emptying undelivered items back in the pool
			while(!tube.isEmpty()) {
				addToPool(tube.pop());
			}
			// Check for a top priority item
			if (getPriorityPoolSize(weight) > 0) {
				// Add priority mail item and deliver directly
				mail = getHighestPriorityMail(weight);
				tube.addItem(mail);
			}
			else {
				// Get as many nonpriority items as available or as fit
				while(tube.getSize() < MAX_TAKE && getNonPriorityPoolSize(weight) > 0) {
					mail = getNonPriorityMail(weight);
					tube.addItem(mail);
					
					// Decrement weight that weak robot can handle (strong is not affected) if put mail to storage
					weight -= mail.getWeight(); // after putting item into the storage weight limit of the weak robot decrease
				}
				sortStorageTube(tube); // sort storage tube to make sure first delivering the lower destination floor
			}
		}
		catch(TubeFullException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sort the stack, so that the robot will deliver it more efficiently, delivering first lower floor item
	 * tube - The tub to be sorted based on floor level
	 */
	private void sortStorageTube(StorageTube tube) {
		ArrayList<MailItem> mails = new ArrayList<MailItem>();
		MailItem mail;
		
		// Pop every item on the storage tube and reorder based on floor ordering, lower level should be on top of the stack
		while (!tube.isEmpty()) {
			mails.add(tube.pop());
		}
		
		// Order based on destination floor, get highest destination floor from the array list and put that on storage first
		while (!mails.isEmpty()) {
			// Get the highest floor and pu that on stack first
			mail = highestFloor(mails);
			try {
				tube.addItem(mail);
			} catch (TubeFullException e) {
				e.printStackTrace();
			}
			
			// Remove that item from the array list
			mails.remove(mail);
		}
	}
	
	/** Get highest destination floor
	 * @param items - ArrayList containing mail item and their information about floor
	 * @return mail item object of the highest floor
	 */
	private MailItem highestFloor(ArrayList<MailItem> items) {
		int currentHighest, checkHighest;
		MailItem currentHighestItem;
		
		// Initial
		currentHighest = items.get(HEAD).getDestFloor();
		currentHighestItem = items.get(HEAD);
		
		// Check which item need to be delivered to highest floor
		for (MailItem i:items) {
			checkHighest = i.getDestFloor();
			if (checkHighest > currentHighest) {
				currentHighest = checkHighest;
				currentHighestItem = i;
			}
		}
		
		return currentHighestItem; // return the item which need to be deliver to highest floor
	}
	
	/**
	 * Inner class used in the mail pool sorting process. Highest priority should be in front of the array list.
	 */
	private class WeightComparator implements Comparator<MailItem> {
		
		/**
		 * Used to determine the priority of mail items. Wanted to make sure that the head of the array list is items that
		 * has the most contribution to the system scoring, so that we can deliver first and hence getting overall lower
		 * system score.
		 * @param item1 - MailItem instance that we want to compare against item2
		 * @param item2 - MailItem instance that we want to compare against item1
		 * @return ordering processes
		 */
		@Override
		public int compare(MailItem item1, MailItem item2) {
			// If the item is both priority and 1 has 100, the other is 10, deliver 100 first
			if (item1 instanceof PriorityMailItem && item2 instanceof PriorityMailItem &&
					(((PriorityMailItem) item1).getPriorityLevel() != ((PriorityMailItem) item2).getPriorityLevel())) {
				return (((PriorityMailItem) item2).getPriorityLevel() - ((PriorityMailItem) item1).getPriorityLevel());
			}
			// The only option left is now between the same priority or between non prority item.
			// We want to prefer deliver lower level item first since it is faster
			else {
				int destination1 = item1.getDestFloor();
				int destination2 = item2.getDestFloor();
				// Deliver lower level item first since it is faster, if it has the same priority level
				if (destination1 != destination2) {
					return (destination1 - destination2);
				}
				// Now if the destination is the same, deliver most urgent item first based on arrival time
				else {
					int arrivalTime1 = item1.getArrivalTime();
					int arrivalTime2 = item2.getArrivalTime();
					
					// Consider lower weight first
					if (arrivalTime1 != arrivalTime2) {
						return (arrivalTime1 - arrivalTime2);
					}
					else {
						int weightItem1 = item1.getWeight();
						int weightItem2 = item2.getWeight();
						
						return (weightItem1 - weightItem2);
					}
				}
			}
		}
	}
}
