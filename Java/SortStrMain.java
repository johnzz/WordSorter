import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SortStrMain {

	public static void main(String[] args) throws Exception {

		long startTime = System.currentTimeMillis();
		long trackTime = System.currentTimeMillis();

		/*
		 * 按照字母表分组，目的：并行排序，无需二次处理
		 */
		int capacity = 27;
		List<List<String>> store = new ArrayList<List<String>>(capacity);
		for (int i = 0; i < capacity; i++) {
			store.add(new ArrayList<String>(40960));
		}

		trackTime = System.currentTimeMillis();

		/*
		 * 读取文件
		 */
		File file = new File("sowpods.txt");
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileChannel channel = raf.getChannel();
		MappedByteBuffer buff = channel.map(FileChannel.MapMode.READ_WRITE, 0,
				3000000);
		buff.load();
		int size = buff.limit();
		byte[] dst = new byte[size];
		buff.get(dst);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(dst)));
		String line = null;
		while ((line = reader.readLine()) != null) {
			char[] content = line.toCharArray();
			int c = content[0];
			int index = 0;
			if (c >= 97) {
				index = c - 97;
			}
			store.get(index).add(line);
		}

		System.out.println("read file and calculate group, costTime = "
				+ (System.currentTimeMillis() - trackTime));

		trackTime = System.currentTimeMillis();

		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				capacity);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 3000,
				TimeUnit.SECONDS, queue);
		List<Future<List<String>>> futures = new ArrayList<Future<List<String>>>();
		for (final List<String> list : store) {
			futures.add(executor.submit(new Callable<List<String>>() {
				@Override
				public List<String> call() throws Exception {
					Collections.sort(list);
					return list;
				}
			}));
		}
		File outfile = new File("out.txt");
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outfile)));
		for (Future<List<String>> f : futures) {
			List<String> sortedList = f.get();
			for (String str : sortedList) {
				out.write(str);
			}
		}
		out.close();

		/*
		 * costTime = 284
		 */
		// RandomAccessFile outraf = new RandomAccessFile(outfile, "rw");
		// FileChannel outchannel = outraf.getChannel();
		// MappedByteBuffer outbuff =
		// outchannel.map(FileChannel.MapMode.READ_WRITE, 0, 3000000);
		// for(Future<List<String>> f : futures) {
		// List<String> sortedList = f.get();
		// for(String str : sortedList) {
		// outbuff.put(str.getBytes());
		// }
		// }

		System.out.println("sort and write file, costTime = "
				+ (System.currentTimeMillis() - trackTime));

		System.out.println("sort total costTime = "
				+ (System.currentTimeMillis() - startTime));

	}

}
