import java.util.Arrays;
import java.util.SortedSet;
import java.util.concurrent.RecursiveAction;

public class ForkWordSorter extends RecursiveAction {

	private String[] sorted = null; // Den etterhvert sorterte arrayen
	private String[] sourceStrings = null; // Kildearrayet som vi henter ordene
											// våre fra
	private int start = 0; // Settes i konstruktør, startindex i kildearrayet
	private int end = 0; // Siste index i kildearrayet
	private int threadPerCount = 0;

	public ForkWordSorter(String[] sourceList, int start, int end,
			int threadPerCount) {
		this.sourceStrings = sourceList;
		this.start = start;
		this.end = end;
		this.threadPerCount = threadPerCount;
		sorted = new String[end - start];
	}

	@Override
	protected void compute() {
		// SortedStringsList sorter = new SortedStringsList();
		//
		// for (int i = start; i < end; i++) {
		// sorter.add(sourceStrings[i]);
		// }
		//
		// sorted = sorter.toArray();

		// sorted = new String[end - start];
		// System.arraycopy(sourceStrings, start, sorted, 0, end - start);
		// Arrays.sort(sorted);

		if ((end - start) <= this.threadPerCount) {
			SortedSet<String> list = new java.util.TreeSet<String>();
			for (int i = start; i < end; i++) {
				list.add(sourceStrings[i]);
			}
			// sorted = new String[list.size()];
			list.toArray(sorted);
			// Arrays.sort(sourceStrings, start, end);
			// System.arraycopy(sourceStrings, start, sorted, 0, end - start);
			// String tem = null;
			// for(int i =start;i<end;i++){
			// for(int j = start;j<end;j++){
			// if(sourceStrings[i].compareTo(sourceStrings[i+1])>0){
			// tem = sourceStrings[i+1];
			// sourceStrings[i+1] = sourceStrings[i];
			// sourceStrings[i] = tem;
			// }
			// }
			// }
			return;
		} else {
			int mid = start + (end - start) / 2;
			ForkWordSorter left = new ForkWordSorter(sourceStrings, start, mid,
					threadPerCount);
			ForkWordSorter right = new ForkWordSorter(sourceStrings, mid, end,
					threadPerCount);
			ForkWordSorter.invokeAll(left, right);
			this.mergeWords(left.getWords(), right.getWords());
			return;
		}
		//

		// sorted = new String[tt.length];
		// for(int i = 0;i<tt.length;i++){
		// sorted[i]=tt[i].toString();
		// }
		// System.arraycopy(tt, start, sorted, 0, end - start);

	}

	public void mergeWords(String[] a, String b[]) {
		int PostA = 0;// [][]
		int PostB = 0;// [][]
		sorted = new String[end - start];
		for (int i = 0; i < (end - start); i++) {
			if (PostA == a.length && PostB < b.length) {
				// A已经录完
				sorted[i] = b[PostB];
				PostB++;
				continue;
			} else if (PostB == b.length && PostA < a.length) {
				sorted[i] = a[PostA];
				PostA++;
				continue;
			}
			if (a[PostA].compareTo(b[PostB]) < 0) {
				sorted[i] = a[PostA];
				PostA++;
			} else {
				sorted[i] = b[PostB];
				PostB++;
			}
		}
	}

	public String[] getWords() {
		return sorted;
	}
}
