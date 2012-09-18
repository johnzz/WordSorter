import java.util.Arrays;

/**
 * 
 */

/**
 * @author Jon
 *
 */
public class Test {

	/**
	 * 
	 */
	public Test() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test t = new Test();
		t.test();
	}
	
	public void test(){
		String[] a = {"cd","ab","ba","aa"};
		Arrays.sort(a,0,4);
		printString(a);
	}
	
	public void printString(String[] str){
		for(int i = 0;i<str.length;i++){
			System.out.print(str[i]+" ");
		}
	}

}
