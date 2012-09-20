import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 
 */

/**
 * @author Jon
 * 
 */
public class MySort {
	private String[] words = null;
	private int numberOfWords;
	private int numberOfThread;

	public int getNumberOfWords() {
		return numberOfWords;
	}

	public void setNumberOfWords(int numberOfWords) {
		this.numberOfWords = numberOfWords;
	}

	private ThreadSetTree[] tt = null;

	/**
	 * 
	 */
	public MySort() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MySort test = new MySort();
		test.init(4);// 开4个线程干活
		test.getFile("sowpods.txt");
		test.writeWordsToOutput("out.txt");
		System.exit(0);
	}

	/**
	 * 初始化排序线程
	 */
	private void init(int threads) {
		numberOfThread = threads;
		tt = new ThreadSetTree[numberOfThread];
		for (int i = 0; i < threads; i++) {
			tt[i] = new ThreadSetTree();
			tt[i].start();
		}
	}

	public static void print(Object msg) {
		System.out.print(msg.toString());
	}

	// public long sort(int size) {
	// Date start = new Date();
	// Arrays.sort(words, 0, size);
	// Date end = new Date();
	// long times = end.getTime() - start.getTime();
	// print("排序" + size + "个词耗时:" + times + "\n");
	// return times;
	// }

	public long sortSet(int size) {
		Date start = new Date();
		for (int i = 0; i < size; i++) {
			// list.add(words[i]);
		}
		// sorted = new String[size];
		// list.toArray(sorted);
		Date end = new Date();
		long times = end.getTime() - start.getTime();
		print("排序" + size + "个词耗时:" + times + "\n");
		return times;
	}

	public boolean getFile(String inputFile) {
		print("Loading contents of " + inputFile + "... ");
		Date start = new Date();
		StringBuilder firstLine = new StringBuilder();
		StringBuilder lines = new StringBuilder(200);
		try {
			int bufferSize = 200 * 1024;
			ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
			FileInputStream in = new FileInputStream(inputFile);// 指定目标文件
			FileChannel channel = in.getChannel(); // 从文件中获取一个通道
			byte[] bytes = new byte[buffer.capacity()];
			boolean readFirstLine = false;
			// int word_num = 0;
			int ThreadNum = 0;
			int worker = 0;
			while (channel.read(buffer) != -1) {
				buffer.flip();
				int len = buffer.remaining();
				buffer.get(bytes, 0, len);
				int index = 0;
				// 首行读取单词数
				if (!readFirstLine) {
					for (int i = 0; i < len; i++) {
						char readChar = (char) bytes[i];
						if (readChar == '\r') { // ignorer \r tegn
						} else {
							if (readChar == '\n') {
								readFirstLine = true;
								index = i + 1;
								// 已经知道多少行了
								words = new String[Integer.parseInt(firstLine
										.toString())];
								numberOfWords = words.length;
								break;
							}
							firstLine.append(readChar);
						}
					}
				}
				for (int i = index; i < len; i++) {
					char readChar = (char) bytes[i];
					if (readChar == '\r') {
					} else if (readChar == '\n') {
						// words[word_num] = lines.toString();
						worker = ThreadNum % numberOfThread;
						ThreadNum++;
						tt[worker].insertKey(lines.toString());
						lines.setLength(0);
						// word_num++;
					} else {
						lines.append(readChar);
					}

				}

				buffer.clear();
			}
		} catch (IOException e) {
			return false;
		}
		// 开始归并
		for (int i = 0; i < this.numberOfThread/2; i++) {
			tt[i].setAnSet(tt[i+2].getSortedKeys());
			tt[i].finishAdding();
		}
		//检查是否归并完毕
		for(int i = 0;i<numberOfThread/2;i++){
			tt[i].isFinishMerge();//阻塞等待
		}
		
		
		
		

		Date end = new Date();
		long timeDiff = end.getTime() - start.getTime();
		print(timeDiff + "ms\n");
		print("一共" + numberOfWords + "个词\n");
		return true;
	}

	// public void mergeKeys(){
	// System.out.print("Merge results to ... ");
	// Date start = new Date();
	// int[] indetTT = new int[numberOfThread];
	// String[][] keys = new String[numberOfThread][];
	// for(int kk =0;kk<numberOfThread;kk++){
	// tt[kk].getSortedKeys().toArray(keys[kk]);
	// }
	//
	//
	// Date end = new Date();
	// long timeDiff = end.getTime() - start.getTime();
	// System.out.println(timeDiff + "ms\n");
	// }

	public boolean writeWordsToOutput(String outputFile) {
		System.out.print("Writing results to " + outputFile + "... ");
		Date start = new Date();

		if (words.length != numberOfWords) { // Sjekker om vi har sortert riktig
			System.out
					.println("Sorted list does not contain expected number of words!");
			return false;
		}
		String[] AA = tt[0].allKeys;
		String[] BB = tt[1].allKeys;
		int indexA = 0;
		int indexB = 0;
		int bufferSize = 200 * 1024;
		int index = 0;
		try {
			RandomAccessFile out = new RandomAccessFile(outputFile, "rw");// 指定目标文件
			FileChannel channel = out.getChannel(); // 从文件中获取一个通道
			ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize + 200);
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < words.length; i++) {
				if(indexA>=AA.length&&indexB<BB.length){
					buf.append(AA[indexB]);
					indexB++;
					continue;
				}
				if(indexB>=BB.length&&indexA<AA.length){
					buf.append(AA[indexA]);
					indexA++;
					continue;
				}
				if(AA[indexA].compareTo(BB[indexB])<0){
					buf.append(AA[indexA]);
					indexA++;
				}else{
					buf.append(AA[indexB]);
					indexB++;
				}
				// buf.append();
				// buf.append(rt);
				// keys[0-3][...]
				// for(int kk =0;kk<numberOfThread;kk++){
				// if(keys[kk][indetTT[kk]].compareTo(anotherString))
				// }
				if (buf.length() >= bufferSize) {
					buffer = channel.map(FileChannel.MapMode.READ_WRITE, index,
							buf.length());
					buffer.put(buf.toString().getBytes());
					index = index + buf.length();
					buf.setLength(0);
					buffer.clear();
				}
			}
			buffer = channel.map(FileChannel.MapMode.READ_WRITE, index,
					buf.length());
			buffer.put(buf.toString().getBytes());
			channel.close();
			buffer.clear();

			// FileOutputStream output = new FileOutputStream(outputFile);
			// BufferedOutputStream bw = new BufferedOutputStream(output);
			// for (int i = 0; i < words.length; i++) {
			// String outputWord = (i == words.length - 1) ? words[i]
			// : words[i] + "\n";
			// bw.write(outputWord.getBytes());
			// }
			// bw.flush();
			// output.flush();
			// bw.close();
			// output.close();

		} catch (IOException e) {
			return false;
		}

		Date end = new Date();
		long timeDiff = end.getTime() - start.getTime();
		System.out.println(timeDiff + "ms\n");
		return true;
	}

	class ThreadSetTree extends Thread {
		private static final int String = 0;
		private SortedSet<String> set = new TreeSet<String>();
		private BlockingQueue<String> list = new ArrayBlockingQueue<String>(
				100000);

		public void run() {
			while (adding) {
				try {
					String key = list.take();
					if (key.length() > 1) {
						set.add(key);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			allKeys = this.mergeKeys();
			finished.add("finish");
		}

		private boolean adding = true;
		private BlockingQueue<String> finished = new ArrayBlockingQueue<String>(1);

		/**
		 * 外部添加内容进来
		 * 
		 * @param key
		 */
		public void insertKey(String key) {
			list.add(key);
		}

		public void insertKeys(Set<String> keys) {
			list.addAll(keys);
		}

		public SortedSet<String> getSortedKeys() {
			return this.set;
		}

		public void setAnSet(Set<String> an) {
			this.an = an;
		}

		public String[] getAllKeys() {
			return allKeys;
		}

		private Set an;

		private String[] allKeys;

		public String[] mergeKeys() {
			int sizeA = set.size();
			int sizeB = an.size();
			String[] a = new String[sizeA];
			this.set.toArray(a);
			String[] b = new String[sizeB];
			an.toArray(b);
			int size = sizeA + sizeB;
			String[] c = new String[size];
			sizeA = 0;
			sizeB = 0;
			for (int i = 0; i < size; i++) {
				if (sizeA >= a.length && sizeB < b.length) {
					c[i] = b[sizeB];
					sizeB++;
					continue;
				}
				if (sizeB >= b.length && sizeA < a.length) {
					c[i] = a[sizeA];
					sizeA++;
					continue;
				}
				if (a[sizeA].compareTo(b[sizeB]) < 0) {
					c[i] = a[sizeA];
					sizeA++;
				} else {
					c[i] = b[sizeB];
					sizeB++;
				}
			}
			return c;
		}

		public void finishAdding() {
			this.adding = false;
			this.list.add("");// 跳出
		}
		
		/**
		 * 该方法会阻塞，直到完成为止
		 */
		public void isFinishMerge(){
			try {
				finished.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
