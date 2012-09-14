import java.util.SortedSet;
import java.util.concurrent.RecursiveAction;

public class ForkWordSorter extends RecursiveAction implements WordHandler {

	private String[] sorted = null; // Den etterhvert sorterte arrayen
	private String[] sourceStrings = null; // Kildearrayet som vi henter ordene
											// våre fra
	private int start = 0; // Settes i konstruktør, startindex i kildearrayet
	private int end = 0; // Siste index i kildearrayet

	public ForkWordSorter(String[] sourceList, int start, int end) {
		this.sourceStrings = sourceList;
		this.start = start;
		this.end = end;
		//这里可以RUN
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

		SortedSet<String> list = new java.util.TreeSet<String>();
		for (int i = start; i < end; i++) {
			list.add(sourceStrings[i]);
		}
		sorted = new String[list.size()];
		list.toArray(sorted);
		//

		// sorted = new String[tt.length];
		// for(int i = 0;i<tt.length;i++){
		// sorted[i]=tt[i].toString();
		// }
		// System.arraycopy(tt, start, sorted, 0, end - start);

	}

	public String[] getWords() {
		return sorted;
	}

	@Override
	public void joinWork() throws InterruptedException {
		this.join();
	}
}
