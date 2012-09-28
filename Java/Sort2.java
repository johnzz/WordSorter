import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class SortWorker implements Callable<Integer> {
	static int startChar = 'a' - 1;
	int begin, end, wordsCount, charCount, level;
	int[] start, nextStart, length, nextLength;
	int[][] indexBegin;
	char[] chars;

	public SortWorker(int begin, int end, int level) {
		this.begin = begin;
		this.end = end;
		this.level = level;
		this.wordsCount = Sort2.wordsCount;
		this.charCount = Sort2.charCount;
		this.start = Sort2.start;
		this.nextStart = Sort2.nextStart;
		this.length = Sort2.length;
		this.nextLength = Sort2.nextLength;
		this.chars = Sort2.chars;
		this.indexBegin = Sort2.indexBegin;
	}

	public Integer call() throws Exception {
		int i, charIndex, kk, k;
		kk = level;
		k = kk << 1;
		for (i = wordsCount - 1; i >= 0; i--) {
			if (length[i] > k) {
				charIndex = (chars[start[i] + k] - startChar) << 5;
				if (chars[start[i] + k + 1] > startChar) {
					charIndex += chars[start[i] + k + 1] - startChar;
				}
				if (charIndex >= begin && charIndex <= end) {
					nextStart[indexBegin[kk][charIndex]] = start[i];
					nextLength[indexBegin[kk][charIndex]] = length[i];
					indexBegin[kk][charIndex]--;
				}
			} else if (begin == 0) {
				nextStart[indexBegin[kk][0]] = start[i];
				nextLength[indexBegin[kk][0]] = length[i];
				indexBegin[kk][0]--;
			}
		}
		return 0;
	}
}

public class Sort2 {
	private static int maxn = 1024*1024*3;
	static int[] start = new int[maxn];
	static int[] nextStart = new int[maxn];
	static int[] length = new int[maxn];
	static int[] nextLength = new int[maxn];
	static int[][] indexBegin = new int[20][1 << 10];
	static char[] chars = new char[maxn];
	static int wordsCount = 1, charCount, level;
	static int startChar = 'a' - 1;

	static final ExecutorService executor = Executors.newFixedThreadPool(4);

	public static void loadFile() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"sowpods.txt"));
//			reader.readLine();
			charCount = reader.read(chars);

			for (int i = 0; i < charCount; i++) {
				if (chars[i] == '\n') {
					start[wordsCount] = i + 1;
					length[wordsCount - 1] = start[wordsCount]
							- start[wordsCount - 1] - 2;
					wordsCount++;
				}
			}
			length[wordsCount - 1] = charCount - start[wordsCount - 1];
			wordsCount--;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void buildIndexBegin() {
		int i = 0, j = 0, k = 0, sum = 0, nextSum = 0, lastIndex = wordsCount - 1;
		for (i = j = 0; i < charCount; i += 2) {
			k = 0;
			if (chars[i] > startChar) {
				k = (chars[i] - startChar) << 5;
			} else {
				j = 0;
				continue;
			}

			if (chars[i + 1] > startChar) {
				k += chars[i + 1] - startChar;
			} else {
				i--;
			}
			indexBegin[j++][k]++;
		}
		for (i = 0; i < 10; i++) {
			sum = 0;
			for (j = (1 << 10) - 1; j >= 0; j--) {
				nextSum = sum + indexBegin[i][j];
				indexBegin[i][j] = lastIndex - sum;
				sum = nextSum;
			}
			indexBegin[i][0] = lastIndex - sum;
			if (sum == 0) {
				Sort2.level = i;
				return;
			}
		}
	}

	public static void sortChars() {
		try {
			Future<Integer> af, bf;
			SortWorker aw, bw;
			int[] tempArray;
			for (int k = level - 1; k >= 0; k--) {
				aw = new SortWorker(0, 1000, k);
				bw = new SortWorker(1001, 1 << 10, k);
				af = executor.submit(aw);
				bf = executor.submit(bw);

				af.get();
				bf.get();

				tempArray = start;
				start = nextStart;
				nextStart = tempArray;

				tempArray = length;
				length = nextLength;
				nextLength = tempArray;
			}
			executor.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void output() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"out2.txt"));
			for (int i = 0; i < wordsCount; i++) {
				writer.write(chars, start[i], length[i] + 2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();
			loadFile();
			long loadedTime = System.currentTimeMillis();
			System.out.println("load file cost : " + (loadedTime - startTime)
					+ "ms");
			buildIndexBegin();
			long buildedTime = System.currentTimeMillis();
			// System.out.println("build indexBegin cost : "
			// + (buildedTime - loadedTime) + "ms");
			sortChars();
			long sortedTime = System.currentTimeMillis();
			System.out
					.println("sort cost: " + (sortedTime - loadedTime) + "ms");
			output();
			long writedTime = System.currentTimeMillis();
			System.out.println("write file cost : " + (writedTime - sortedTime)
					+ "ms");
			System.out.println("All cost:" + (writedTime - startTime) + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
