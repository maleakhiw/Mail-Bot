package strategies;

import java.util.Comparator;
import java.util.PriorityQueue;

import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailPool implements IMailPool{
	// My first job with Robotic Mailing Solutions Inc.!
	private PriorityQueue<MailItem> nonPriorityPool;
	private PriorityQueue<MailItem> priorityPool;
	
	private static final int MAX_TAKE = 4;
	public static final double EXPONENT = 1.1;
	
	public MyMailPool(){
		// Instantiate the compare object used to determine item who has highest priority
		WeightComparator comparator = new WeightComparator();
		nonPriorityPool = new PriorityQueue<MailItem>(comparator);
		priorityPool = new PriorityQueue<MailItem>(comparator);
	}

	public void addToPool(MailItem mailItem) {
		// Check whether it has a priority or not
		if(mailItem instanceof PriorityMailItem){
			// Add to priority items priority queue
			// Using PQ maintains efficiency as it doesn't sort everything. 
			// PQ only makes sure the head has highest priority and the rest will be sorted when necessary.
			priorityPool.add(mailItem);
		}
		else{
			// Add to nonpriority item priority queue
			nonPriorityPool.add(mailItem);
		}
	}
	
	private int getNonPriorityPoolSize(int weightLimit) {
		// This was easy until we got the weak robot
		// Oh well, there's not that many heavy mail items -- this should be close enough
		return nonPriorityPool.size();
	}
	
	private int getPriorityPoolSize(int weightLimit){
		// Same as above, but even less heavy priority items -- hope this works too
		return priorityPool.size();
	}

	private MailItem getNonPriorityMail(int weightLimit){
		if(getNonPriorityPoolSize(weightLimit) > 0){
			// Should I be getting the earliest one? 
			// Surely the risk of the weak robot getting a heavy item is small!
			return nonPriorityPool.remove();
		}
		else{
			return null;
		}
	}
	
	private MailItem getHighestPriorityMail(int weightLimit){
		if(getPriorityPoolSize(weightLimit) > 0){
			// How am I supposed to know if this is the highest/earliest?
			return priorityPool.remove();
		}
		else{
			return null;
		}
		
	}

	@Override
	public void fillStorageTube(StorageTube tube, boolean strong) {
		int max = strong ? Integer.MAX_VALUE : 2000; // max weight
		// Priority items are important;
		// if there are some, grab one and go, otherwise take as many items as we can and go
		try{
			// Start afresh by emptying undelivered items back in the pool
			while(!tube.isEmpty()) {
				addToPool(tube.pop());
			}
			// Check for a top priority item
			if (getPriorityPoolSize(max) > 0) {
				// Add priority mail item
				tube.addItem(getHighestPriorityMail(max));
				// Won't add any more - want it delivered ASAP
			}
			else{
				// Get as many nonpriority items as available or as fit
				while(tube.getSize() < MAX_TAKE && getNonPriorityPoolSize(max) > 0) {
					tube.addItem(getNonPriorityMail(max));
				}
			}
		}
		catch(TubeFullException e) {
			e.printStackTrace();
		}
	}
	

	private static class WeightComparator implements Comparator<MailItem> {

		@Override
		public int compare(MailItem item1, MailItem item2) {
			double priorityItem1 = calculateSystemPriority(item1);
			double priorityItem2 = calculateSystemPriority(item2);
			int compareValue = Double.compare(priorityItem1, priorityItem2);
			
			// Need the reverse since higher value function need to be popped first in the Priority Queue
			if (compareValue > 0) {
				return -1;
			}
			else if (compareValue < 0) {
				return 1;
			}
			else {
				return 0;
			}
		}
		
		private double calculateSystemPriority(MailItem item) {
			double firstTerm, secondTerm;
			if (item instanceof PriorityMailItem) {
				firstTerm = Math.pow((Clock.Time() - item.getArrivalTime()), 1.1);
				secondTerm = 1 + Math.sqrt(((PriorityMailItem)item).getPriorityLevel());
				return(firstTerm * secondTerm);
			}
			else {
				firstTerm = Math.pow((Clock.Time() - item.getArrivalTime()), 1.1);
				return(firstTerm);
			}
		}
	}

}
