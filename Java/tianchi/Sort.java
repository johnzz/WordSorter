/**
 * 
 */
package tianchi;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class SortTerminatorWorker implements Callable<Integer>{
	int			maxn = 300000;
	byte[]		bytes = new byte[maxn<<4];
	byte[] 		outputBytes = new byte[maxn<<4];
	int[] 		start = new int[maxn];
	int[] 		nextStart = new int[maxn];
	int[] 		length = new int[maxn];
	int[] 		nextLength = new int[maxn];
	int[][] 	indexBegin = new int[8][1<<15];
	int 		byteCount, wordsCount = 1, level;
	byte		startByte = 'a'-1;


	public void loadFile(){
		try{
			for(int i = 0; i < byteCount; i++){
				bytes[i] -= startByte;
				if(bytes[i] < 0){
					start[wordsCount] = i+2;
					length[wordsCount-1] = start[wordsCount] - start[wordsCount-1] - 2;
					wordsCount++;
					bytes[++i] -= startByte;
				}
			}
			wordsCount--;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void buildIndexBegin(){
		int i, j, byteIndex, sum, nextSum, lastIndex = wordsCount - 1;
		for(i = j = 0; i < byteCount;){
			byteIndex = 0;
			if(bytes[i] > 0){
				byteIndex |= bytes[i]<<10;
				if(bytes[i+1] > 0){
					byteIndex |= bytes[i+1]<<5;
					if(bytes[i+2] > 0){
						byteIndex |= bytes[i+2];
						indexBegin[j][byteIndex]++;
						i += 3;
						j++;
					}else{
						indexBegin[j][byteIndex]++;
						i += 4;
						j = 0;
					}
				}else{
					indexBegin[j][byteIndex]++;
					i += 3;
					j = 0;
				}
			}else{
				i += 2;
				j = 0;
			}
		}
		for(i = 0; i < 8; i++){
			sum = 0;
			for(j = (1<<15)-1; j >= 0; j--){
				nextSum = sum + indexBegin[i][j];
				indexBegin[i][j] = lastIndex - sum;
				sum = nextSum;
			}
			indexBegin[i][0] = lastIndex - sum;
			if(sum == 0){
				level = i;
				return;	
			}
		}
	}

    public void sortChars(){
        int i, k, kk, byteIndex;
        
        int[] tempArray;
        for(k = (level-1)*3, kk = level; k >= 0; k -= 3){        	
            kk--;

            for(i = wordsCount-1; i >= 0; i--){ 
                if(length[i] > k){
                	byteIndex = bytes[start[i]+k]<<10;
                	if(bytes[start[i]+k+1] > 0){
                		byteIndex |= bytes[start[i]+k+1]<<5;
                		if(bytes[start[i]+k+2] > 0){
                			byteIndex |= bytes[start[i]+k+2];
                		}
                	}
                    nextStart[indexBegin[kk][byteIndex]] = start[i];
                    nextLength[indexBegin[kk][byteIndex]] = length[i];
                    indexBegin[kk][byteIndex]--;
                }else{
                    nextStart[indexBegin[kk][0]] = start[i];
                    nextLength[indexBegin[kk][0]] = length[i];	                    
                    indexBegin[kk][0]--;
                }
            }
            
            tempArray = start;
            start = nextStart;
            nextStart = tempArray;
            
            tempArray = length;
            length = nextLength;
            nextLength = tempArray;
        }
    }
    
    public void output(){
    	for(int i = 0; i < byteCount; i++){
    		bytes[i] += startByte;
    	}
    }
    
    public void print(){
    	for(int i = 0; i < 10; i++){
    		for(int j = 0; j < length[i] + 2; j++){
    			System.out.print((char)bytes[start[i]+j]);
    		}
    	}
    }
	public Integer call(){
		loadFile();
		buildIndexBegin();
		sortChars();
		output();
		//print();
		return 0;
	}
}
class WriterWorker implements Callable<Integer>{
	static byte[] outputBytes = new byte[3000000];
	static int outputIndex = 0;
	public Integer call() throws Exception {
		int i = 0, ai = 0, bi = 0;
		while(i < Sort.retIndex || !Sort.finish){
			while(i < Sort.retIndex){
				if(Sort.ret[i] == 0){
					System.arraycopy(Sort.a.bytes, Sort.a.start[ai], outputBytes, outputIndex, Sort.a.length[ai] + 2);
					outputIndex += Sort.a.length[ai++] + 2;
				}else{
					System.arraycopy(Sort.b.bytes, Sort.b.start[bi], outputBytes, outputIndex, Sort.b.length[bi] + 2);
					outputIndex += Sort.b.length[bi++] + 2;
				}
				i++;
			}
		}
		FileOutputStream writer = new FileOutputStream("out5.txt");
		int pLength = 1024*16;
		for(i = pLength; i < outputIndex; i += pLength){
			writer.write(outputBytes, i-pLength, pLength);
		}
		writer.write(outputBytes, i-pLength, outputIndex-i+pLength);
		writer.flush();
		writer.close();
		return null;
	}
}
/**
 * @author tianchi.gzt
 *
 */
public class Sort{
	static int maxn = 300000;
	static SortTerminatorWorker a = new SortTerminatorWorker();
	static SortTerminatorWorker b = new SortTerminatorWorker();
	static WriterWorker c = new WriterWorker();

	public static void loadFile(){
		FileInputStream input = null;
		try{
			input = new FileInputStream("sowpods.txt");
			a.byteCount = input.read(a.bytes);
			a.bytes[a.byteCount] = a.bytes[a.byteCount+1] = -1;
			int x = a.byteCount/2, y;
			for(;a.bytes[x] != '\n'; x++);
			x++;
			y = a.byteCount-x;
			System.arraycopy(a.bytes, x, b.bytes, 0, a.byteCount-x);
			a.byteCount = x;
			b.byteCount = y;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	static boolean finish = false;
	static byte[] ret = new byte[maxn<<4];
	static int retIndex = 0;
	public static void output(){
		try{
			int ai, bi, i, alength, blength, astart, bstart, flag;
			for(ai = bi = 0; ai < a.wordsCount && bi < b.wordsCount; ){
				alength = a.length[ai];
				blength = b.length[bi];
				astart = a.start[ai];
				bstart = b.start[bi];
				flag = 0;
				for(i = 0; i < alength && i < blength; i++){
					if(a.bytes[astart+i] > b.bytes[bstart+i]){
						flag = 1;
						break;
					}else if(a.bytes[astart+i] < b.bytes[bstart+i]){
						flag = -1;
						break;
					}
				}
				if(flag < 0 || i == alength){
					ret[retIndex++] = 0;
					ai++;
				}else{
					ret[retIndex++] = 1;
					bi++;
				}
			}
			while(ai < a.wordsCount){
				ret[retIndex++] = 0;
				ai++;
			}
			while(bi < b.wordsCount){
				ret[retIndex++] = 1;
				bi++;
			}
			finish = true;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	static ExecutorService executor = Executors.newFixedThreadPool(3);
	public static void main(String[] args){
		try{
			long startTime = System.nanoTime();
			loadFile();
			System.out.println("load file finish : " + (System.nanoTime()-startTime)/1000000 + " MS.");
			Future<Integer> af = executor.submit(a);
			Future<Integer> bf = executor.submit(b);
			af.get();
			bf.get();		
			System.out.println("sort finish : " + (System.nanoTime()-startTime)/1000000 + " MS.");
			Future<Integer> cf = executor.submit(c);
			output();
			cf.get();
			System.out.println("all finish : " + (System.nanoTime()-startTime)/1000000 + " MS.");
			executor.shutdown();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}