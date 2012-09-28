/**
 * 
 */
package me;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FastSort {
	private Worker[] worker = new Worker[THREADS];// 线程数
	private ExecutorService es = Executors.newFixedThreadPool(THREADS);
	private final static int THREADS = 4;
	private byte[] AllWords = new byte[1024 * 1024 * 3];// 大约不超过3M
	private int AllSize = 0;// 总共的字数
	private int WordsSize = 0;// 总词数
	private static final byte KRY_R = 13;// '\r'
	private static final byte KEY_N = 10;// '\n'

	public static void main(String[] args) {
		FastSort test = new FastSort();

		long time1 = System.currentTimeMillis();
		test.readFile("sowpods.txt");
		long time2 = System.currentTimeMillis();
		System.out.println("load file cost:" + (time2 - time1) + " ms.");
		test.sort();
		long time3 = System.currentTimeMillis();
		System.out.println("sort cost:" + (time3 - time2) + " ms.");
		long time4 = System.currentTimeMillis();
		System.out.println("write file cost:" + (time4 - time3) + " ms.");
		System.out.println("All finish cost:" + (time4 - time1) + " ms.");
	}

	public void sort() {

		// for (int i = 0; i < THREADS; i++) {
		// Future<Integer> af = es.submit(worker[i]);
		// }
		//
		// Future<Integer> cf = executor.submit(c);
		// output();
		// cf.get();
		this.checkFile();
	}

	/**
	 * 直接读取文件
	 * 
	 * @throws IOException
	 */
	public void readFile(String fileName) {
		for (int i = 0; i < THREADS; i++) {
			worker[i] = new Worker();
		}
		try {
			FileInputStream input = new FileInputStream(fileName);
			BufferedInputStream bufinput = new BufferedInputStream(input);
			AllSize = bufinput.read(AllWords);
			bufinput.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 检查分隔符，分组
	 */
	public void checkFile() {
		boolean fistLine = true;
		int cancelSize = 0;
		StringBuffer sb = new StringBuffer();
		int 线程工作区 = 0;
		int 词索引 = 0;
		for (int i = 0; i < this.AllSize; i++) {
			if (this.AllWords[i] == KRY_R) {
				// 忽略掉
				continue;
			}
			if (fistLine) {
				// 首行为词数
				if (this.AllWords[i] != KEY_N) {
					sb.append((char) AllWords[i]);
					continue;
				} else {
					// 统计词数
					WordsSize = Integer.parseInt(sb.toString());
					cancelSize = i;
					fistLine = false;
					for (int j = 0; j < THREADS - 1; j++) {
						// 初始化工作类的数据区
						worker[j].keysA = new long[WordsSize / THREADS];
						worker[j].keysB = new int[WordsSize / THREADS];
					}
					worker[THREADS - 1].keysA = new long[WordsSize
							- (THREADS - 1) * (WordsSize / THREADS)];
					worker[THREADS - 1].keysB = new int[WordsSize
							- (THREADS - 1) * (WordsSize / THREADS)];
					continue;
				}
			} else {
				// 后续为词体，忽略多余的词
				// abc/n
				if (this.AllWords[i] != KEY_N) {
					if (前位 > 0) {
						tmpA += (this.AllWords[i] - 96) << (前位 * 5);
						前位--;
					} else {
						tmpB += (this.AllWords[i] - 96) << (后位 * 5);
						后位--;
					}
				} else {
					// 一个词
					worker[线程工作区].keysA[词索引] = tmpA;
					worker[线程工作区].keysB[词索引] = tmpB;
					词索引++;
					if (词索引 >= worker[线程工作区].keysA.length) {
						线程工作区++;
						词索引 = 0;
					}
					前位 = Max前位;// 恢复
					后位 = Max后位;
					tmpA = 0;
					tmpB = 0;
				}
			}
		}
	}

	private int 前位 = Max前位;
	private int 后位 = Max后位;// 含回车符号

	private static final int Max前位 = 12;
	private static final int Max后位 = 4;
	private long tmpA = 0;
	private int tmpB = 0;

	public void output() {
		// try {
		// int ai, bi, i, alength, blength, astart, bstart, flag;
		// for (ai = bi = 0; ai < a.wordsCount && bi < b.wordsCount;) {
		// alength = a.length[ai];
		// blength = b.length[bi];
		// astart = a.start[ai];
		// bstart = b.start[bi];
		// flag = 0;
		// for (i = 0; i < alength && i < blength; i++) {
		// if (a.bytes[astart + i] > b.bytes[bstart + i]) {
		// flag = 1;
		// break;
		// } else if (a.bytes[astart + i] < b.bytes[bstart + i]) {
		// flag = -1;
		// break;
		// }
		// }
		// if (flag < 0 || i == alength) {
		// ret[retIndex++] = 0;
		// ai++;
		// } else {
		// ret[retIndex++] = 1;
		// bi++;
		// }
		// }
		// while (ai < a.wordsCount) {
		// ret[retIndex++] = 0;
		// ai++;
		// }
		// while (bi < b.wordsCount) {
		// ret[retIndex++] = 1;
		// bi++;
		// }
		// finish = true;
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	class Worker implements Callable<Integer> {

		public long[] keysA = null;// 00000 00000 每5个位表示一个字符
		public int[] keysB = null; // 00000 00000 每5个位表示一个字符

		@Override
		public Integer call() throws Exception {
			Arrays.sort(keysA);
			return null;
		}

		// byte[] Words = new byte[1024 * 1024 * 3];
		// int[] start = new int[maxn];
		// int[] nextStart = new int[maxn];
		// int[] length = new int[maxn];
		// int[] nextLength = new int[maxn];
		// int[][] indexBegin = new int[8][1 << 15];
		// int byteCount, wordsCount = 1, level;
		// byte startByte = 'a' - 1;
		//
		// public void loadFile() {
		// try {
		// for (int i = 0; i < byteCount; i++) {
		// bytes[i] -= startByte;
		// if (bytes[i] < 0) {
		// start[wordsCount] = i + 2;
		// length[wordsCount - 1] = start[wordsCount]
		// - start[wordsCount - 1] - 2;
		// wordsCount++;
		// bytes[++i] -= startByte;
		// }
		// }
		// wordsCount--;
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		//
		// public void buildIndexBegin() {
		// int i, j, byteIndex, sum, nextSum, lastIndex = wordsCount - 1;
		// for (i = j = 0; i < byteCount;) {
		// byteIndex = 0;
		// if (bytes[i] > 0) {
		// byteIndex |= bytes[i] << 10;
		// if (bytes[i + 1] > 0) {
		// byteIndex |= bytes[i + 1] << 5;
		// if (bytes[i + 2] > 0) {
		// byteIndex |= bytes[i + 2];
		// indexBegin[j][byteIndex]++;
		// i += 3;
		// j++;
		// } else {
		// indexBegin[j][byteIndex]++;
		// i += 4;
		// j = 0;
		// }
		// } else {
		// indexBegin[j][byteIndex]++;
		// i += 3;
		// j = 0;
		// }
		// } else {
		// i += 2;
		// j = 0;
		// }
		// }
		// for (i = 0; i < 8; i++) {
		// sum = 0;
		// for (j = (1 << 15) - 1; j >= 0; j--) {
		// nextSum = sum + indexBegin[i][j];
		// indexBegin[i][j] = lastIndex - sum;
		// sum = nextSum;
		// }
		// indexBegin[i][0] = lastIndex - sum;
		// if (sum == 0) {
		// level = i;
		// return;
		// }
		// }
		// }
		//
		// public void sortChars() {
		// int i, k, kk, byteIndex;
		//
		// int[] tempArray;
		// for (k = (level - 1) * 3, kk = level; k >= 0; k -= 3) {
		// kk--;
		//
		// for (i = wordsCount - 1; i >= 0; i--) {
		// if (length[i] > k) {
		// byteIndex = bytes[start[i] + k] << 10;
		// if (bytes[start[i] + k + 1] > 0) {
		// byteIndex |= bytes[start[i] + k + 1] << 5;
		// if (bytes[start[i] + k + 2] > 0) {
		// byteIndex |= bytes[start[i] + k + 2];
		// }
		// }
		// nextStart[indexBegin[kk][byteIndex]] = start[i];
		// nextLength[indexBegin[kk][byteIndex]] = length[i];
		// indexBegin[kk][byteIndex]--;
		// } else {
		// nextStart[indexBegin[kk][0]] = start[i];
		// nextLength[indexBegin[kk][0]] = length[i];
		// indexBegin[kk][0]--;
		// }
		// }
		//
		// tempArray = start;
		// start = nextStart;
		// nextStart = tempArray;
		//
		// tempArray = length;
		// length = nextLength;
		// nextLength = tempArray;
		// }
		// }
		//
		// public void output() {
		// for (int i = 0; i < byteCount; i++) {
		// bytes[i] += startByte;
		// }
		// }
		//
		// public void print() {
		// for (int i = 0; i < 10; i++) {
		// for (int j = 0; j < length[i] + 2; j++) {
		// System.out.print((char) bytes[start[i] + j]);
		// }
		// }
		// }
		//
		// public Integer call() {
		// loadFile();
		// buildIndexBegin();
		// sortChars();
		// output();
		// // print();
		// return 0;
		// }
		// }
		//
		// class WriterWorker implements Callable<Integer> {
		// static byte[] outputBytes = new byte[3000000];
		// static int outputIndex = 0;
		//
		// public Integer call() throws Exception {
		// int i = 0, ai = 0, bi = 0;
		// while (i < FastSort.retIndex || !FastSort.finish) {
		// while (i < FastSort.retIndex) {
		// if (FastSort.ret[i] == 0) {
		// System.arraycopy(FastSort.a.bytes,
		// FastSort.a.start[ai], outputBytes, outputIndex,
		// FastSort.a.length[ai] + 2);
		// outputIndex += FastSort.a.length[ai++] + 2;
		// } else {
		// System.arraycopy(FastSort.b.bytes,
		// FastSort.b.start[bi], outputBytes, outputIndex,
		// FastSort.b.length[bi] + 2);
		// outputIndex += FastSort.b.length[bi++] + 2;
		// }
		// i++;
		// }
		// }
		// FileOutputStream writer = new FileOutputStream("out5.txt");
		// int pLength = 1024 * 16;
		// for (i = pLength; i < outputIndex; i += pLength) {
		// writer.write(outputBytes, i - pLength, pLength);
		// }
		// writer.write(outputBytes, i - pLength, outputIndex - i + pLength);
		// writer.flush();
		// writer.close();
		// return null;
		// }
	}
}