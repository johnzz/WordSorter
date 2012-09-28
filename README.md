# Go/Java WordSorter Comparison

The Java code in this project was originally an assignment from the university. The goal was to write a Java program that accepted a list of words, and wrote them out in sorted order, using threads for speed. I used this project to see how Google's new language, Go, compared against Java with emphasis on LOC and performance. I'm glad to say that Go easily beats Java in both these cases. Go is also dramatically easier on system memory than Java.

## Use

Go project can be run by entering the Go directory and issuing the following: `go run main.go 128 sowpods.txt out.txt`
128 is the number of goroutines, sowpods.txt is the input and out.txt is the output.

Java project can be run by entering Java directory and compile using `javac Sort.java` then run by issuing `java Sort 16 sowpods.txt out.txt`

## Results

On my ThinkPad T430 I7 2.9G on Win7X64 16GRAM With 830SSD

C:\Work\Test\WordSorter\Java>java -Xmx1024m -Xms1024m -Xmn512m -Xss512k -XX:+UseParallelGC Sort 16 sowpods.txt out.txt

Loading contents of sowpods.txt... 46ms

Sorting... 141ms

Writing results to out.txt... 62ms

Using 16 threads, 267751 words was sorted in 249 milliseconds. 

Tianchi update the Java code, and the speed in 77ms.

So Tianchi writed a c solution, update the speed in 47ms. But not the fastest!

#C
The FastSort in c is 22ms, and Yewang take this Cpp in 34ms!

Thanks for YeWang and YanRan. All code run on XEON E5620 2.4G with 16 Thread, IO speed more than 500m/s 
