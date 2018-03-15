/**
 * Author: Maleakhi Agung Wijaya
 * Student Number: 784091
 * Date: 15/03/2018
 */
package strategies;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

/** Implements the interface mail pool and define the behaviour of the system in mail pool which are located on the ground 
 * level. Mainly focuses on the MailSelecting system to decide what mail items should go in to storage unit/ robot backpack.
 */
public class MyMailPool implements IMailPool{
	/** Private Variables */
	/* The data structure for the mail selecting system is a priority queue. The reason for using priority queue is that 
	 * we want to optimise the scoring system by delivering item which contribute to high system scoring function first. When
	 * this data structure is implemented, it will prioritise giving the robots item with high priority/urgency first which 
	 * will then optimise the overall result. Another reason of choosing Priority Queue is as it is better than other data 
	 * structures, such as ArrayList, LinkedList. PQ is more efficient because it creates a heap and just do partial sort every 
	 * time. Consequenctly, PQ doesn't do unnecessary process of sorting everything every single time.
	 */
	private PriorityQueue<MailItem> nonPriorityPool;
	private PriorityQueue<MailItem> priorityPool;
	
	/** Constant */
	private static final int MAX_TAKE = 4;
	public static final double EXPONENT = 1.1;
	public static final int POSITIVE = 1;
	public static final int NEUTRAL = 0;
	public static final int NEGATIVE = -1;
	public static final int GROUND_FLOOR = 1;
	
	/**
	 * Constructor for MyMailPool which are used to instantiate appropriate PriorityQueue
	 */
	public MyMailPool(){
		// Instantiate the compare object used to determine item who has highest priority, Comparator class are described below
		WeightComparator comparator = new WeightComparator();
		nonPriorityPool = new PriorityQueue<MailItem>(comparator);
		priorityPool = new PriorityQueue<MailItem>(comparator);
	}

	/**
	 * Used to add item to the appropriate pool (priority or non priority)
	 * @param mailItem - mail item containing the mail and some attributes of the mail such as destination, arrival time, etc.
	 */
	public void addToPool(MailItem mailItem) {
		// Check types of item being added and put to appropriate pool
		if(mailItem instanceof PriorityMailItem){
			priorityPool.add(mailItem);
		}
		else{
			nonPriorityPool.add(mailItem);
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
				size ++;
			}
		}
		
		return size;
	}

	/**
	 * Used to get a non priority mail from the non priority pool PQ, only get item that the robot can still take.
	 * @param weightLimit
	 * @return MailItem instance that the robot can take from non priority pool
	 */
	private MailItem getNonPriorityMail(int weightLimit){
		ArrayList<MailItem> mails = new ArrayList<MailItem>();
		MailItem mail;
		
		if(getNonPriorityPoolSize(weightLimit) > 0){
			/* Only take item that the robot can handle based on weight if the robot cannot handle then put on the array list 
			 * so that later can be put back to the Priority Queue.
			 */
			mail = nonPriorityPool.poll();
			while (mail.getWeight() > weightLimit) {
				mails.add(mail);
				mail = nonPriorityPool.poll();
			}
			
			// Put back all mail on the mails back to the priority queue
			for (MailItem m:mails) {
				nonPriorityPool.add(m);
			}
			
			return mail; // Return the mail that satisfy the weight criterion
		}
		else{
			return null;
		}
	}
	
	/**
	 * Used to get a priority mail from the priority pool PQ, only get item that the robot can still take.
	 * @param weightLimit
	 * @return MailItem instance that the robot can take from the priority pool
	 */
	private MailItem getHighestPriorityMail(int weightLimit){
		ArrayList<MailItem> mails = new ArrayList<MailItem>();
		MailItem mail;
		
		if(getPriorityPoolSize(weightLimit) > 0){
			/* Only take item that the robot can handle based on weight if the robot cannot handle then put on the array list 
			 * so that later can be put back to the Priority Queue.
			 */
			mail = priorityPool.poll();
			while (mail.getWeight() > weightLimit) {
				mails.add(mail);
				mail = priorityPool.poll();
			}
			
			// Put back all mail on the mails back to the priority queue
			for (MailItem m:mails) {
				priorityPool.add(m);
			}
			
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
		int max = strong ? Integer.MAX_VALUE : 2000; // max weight
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
		currentHighest = items.get(NEUTRAL).getDestFloor();
		currentHighestItem = items.get(NEUTRAL);
		
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
	 * Inner class used in the Priority Queue for the order of sorting. This class is used to determine priority for items in 
	 * the priority queue.
	 */
	private static class WeightComparator implements Comparator<MailItem> {
		
		/**
		 * Used to determine the priority of mail items. Wanted to make sure that the head of the priority queue is items that
		 * has the most contribution to the system scoring, so that we can deliver first and hence getting overall lower
		 * system score.
		 * @param item1 - MailItem instance that we want to compare against item2
		 * @param item2 - MailItem instance that we want to compare against item1
		 * @return since we want to do reverse sorted (highest contributor on the head of PQ), then -1 if item1 > item2, 0 if
		 * item1 == item2, 1 if item1 < item2.
		 */
		@Override
		public int compare(MailItem item1, MailItem item2) {
			double priorityItem1 = calculateItemPriority(item1);
			double priorityItem2 = calculateItemPriority(item2);
			int compareValue = Double.compare(priorityItem1, priorityItem2);
			
			// Need the reverse since we want the head of the PQ to be the largest contributor for the scoring system
			if (compareValue > 0) {
				return NEGATIVE;
			}
			else if (compareValue < 0) {
				return POSITIVE;
			}
			else {
				return NEUTRAL;
			}
		}
		
		/**
		 * Used to calculate the priority level of an item that are used in priority queue. Since the objective is to minimise
		 * the system function, this algorithm order item based on their contribution to the system function. This algorithm
		 * are then used in max heap format which the priority queue will return first item that has the highest contribution
		 * to the system scoring function. As a result, it effectively optimise the system scoring function.
		 * @param item - MailItem instance that we want to calculate it's priority to be used to order it in the priority queue
		 * @return contribution of the item
		 */
		private double calculateItemPriority(MailItem item) {
			double firstTerm, secondTerm;
			// Calculation based on priority item, for non priority term there is no multiplication factor as priority = 0
			if (item instanceof PriorityMailItem) {
				/* first term is based on the formula given on the assignment, with the destination time calculated based on
				 * the floor assuming going up 1 floor take 1 time and waiting time of the item currently. As for the second 
				 * term it's quite straight forward from the formula
				 */
				firstTerm = Math.pow((item.getDestFloor() - GROUND_FLOOR + Clock.Time() - item.getArrivalTime()), EXPONENT);
				secondTerm = 1 + Math.sqrt(((PriorityMailItem) item).getPriorityLevel());
				return (firstTerm * secondTerm);
			}
			else {
				/* first term is based on the formula given on the assignment, with the destination time calculated based on
				 * the floor assuming going up 1 floor take 1 time and waiting time of the item currently. As for the second 
				 * term it's quite straight forward from the formula
				 */
				firstTerm = Math.pow((item.getDestFloor() - GROUND_FLOOR + Clock.Time() - item.getArrivalTime()), EXPONENT);
				secondTerm = 1; // since priority is 0
				return (firstTerm * secondTerm);
			}
		}
	}
}
