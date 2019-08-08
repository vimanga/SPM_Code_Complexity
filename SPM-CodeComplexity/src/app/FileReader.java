package app;

import java.io.File;
import java.io.IOException;

public class FileReader {

	public static void main(String[] args) throws IOException {
		
		File f1=new File("/home/vimanga/eclipse-workspace/SPM/src/test.txt"); //Creation of File Descriptor for input file
	      
	      Calculate_Ci ci = new Calculate_Ci();
	      ci.calculateCi(f1);

	}

}
