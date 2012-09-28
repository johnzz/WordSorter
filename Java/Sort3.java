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
	int[][] 	indexBegin = new int[10][1<<10];
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
		for(i = j = 0; i < byteCount; i += 2){
			byteIndex = 0;
			if(bytes[i] > 0){
				byteIndex += bytes[i]<<5;
				if(bytes[i+1] > 0){
					byteIndex += bytes[i+1];
				}else{
					i--;
				}
				indexBegin[j++][byteIndex]++;
			}else{
				j = 0;
			}
		}
		for(i = 0; i < 10; i++){
			sum = 0;
			for(j = (1<<10)-1; j >= 0; j--){
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
        for(k = (level-1)*2; k >= 0; k -= 2){        	
            kk = k>>1;

            for(i = wordsCount-1; i >= 0; i--){                
                if(length[i] > k){
                	byteIndex = bytes[start[i]+k]<<5;
                    if(bytes[start[i]+k+1] > 0){
                    	byteIndex += bytes[start[i]+k+1];
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
	public Integer call(){
		loadFile();
		buildIndexBegin();
		sortChars();
		output();
		return 0;
	}
}

public class Sort3{
	static int maxn = 300000;
	static SortTerminatorWorker a = new SortTerminatorWorker();
	static SortTerminatorWorker b = new SortTerminatorWorker();

	public static void loadFile(){
		try{
			FileInputStream input = new FileInputStream("sowpods.txt");
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
		}
	}
	static byte[] outputBytes = new byte[maxn<<4];
	static int outputIndex = 0;
	static boolean finish = false;
	public static void output(){
		try{
			FileOutputStream writer = new FileOutputStream("out3.txt");
			int ai, bi, i, j, alength, blength, astart, bstart, flag, aValue, bValue;
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
				flag = alength - blength;
				if(flag < 0){
					System.arraycopy(a.bytes, astart, outputBytes, outputIndex, alength+2);					
					outputIndex += alength+2;
					ai++;
				}else{
					System.arraycopy(b.bytes, bstart, outputBytes, outputIndex, blength+2);
					outputIndex += blength+2;
					bi++;
				}
			}
			while(ai < a.wordsCount){
				System.arraycopy(a.bytes, a.start[ai], outputBytes, outputIndex, a.length[ai]+2);
				outputIndex += a.length[ai]+2;
				ai++;
			}
			while(bi < b.wordsCount){
				System.arraycopy(b.bytes, b.start[bi], outputBytes, outputIndex, b.length[bi]+2);
				outputIndex += b.length[bi]+2;
				bi++;
			}
			int pLength = 1024*16;
			for(i = 0; i < outputIndex; i += pLength){
				writer.write(outputBytes, i, pLength);
			}
			writer.write(outputBytes, i-pLength, outputIndex-i+pLength);
			writer.flush();
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	static ExecutorService executor = Executors.newFixedThreadPool(2);
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
			output();
			System.out.println("all finish : " + (System.nanoTime()-startTime)/1000000 + " MS.");
			executor.shutdown();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}