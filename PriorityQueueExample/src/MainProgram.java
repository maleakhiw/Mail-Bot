import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MainProgram {

	public static void main(String[] args) {
		IntegerComparator comparator = new IntegerComparator();
		PriorityQueue<Integer> queue = new PriorityQueue<>(comparator);
		
		queue.add(10);
		queue.add(12);
		queue.add(2);
		queue.add(19);
		queue.add(1);
		queue.add(3);
		queue.add(0);
		queue.add(100);
		queue.add(-100);
		queue.add(-1);
		queue.add(50);
		System.out.println(queue);
		System.out.println(queue.remove());
		System.out.println(queue.remove());
		System.out.println(queue);
		
		queue.clear();
		queue.poll();
		for (Integer i:queue) {
			System.out.println(i);
		}

	}
	
	private static class IntegerComparator implements Comparator<Integer> {

		@Override
		public int compare(Integer lhs, Integer rhs) {
			if (lhs < rhs) {
				return 1;
			}
			else if (lhs > rhs) {
				return -1;
			}
			else {
				return 0;
			}
		}
		
	}

}
