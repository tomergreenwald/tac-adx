import java.io.FileInputStream;
import java.io.IOException;

import tau.tac.adx.parser.Auctions.DataBundle;

public class Test {

	public static void main(String[] args) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(
				"C:\\Users\\Tomer\\git\\tac-adx\\AdX\\resources\\log.protobuf");
		long pre = System.currentTimeMillis();
		DataBundle parseFrom = DataBundle.parseFrom(fileInputStream);
		long post = System.currentTimeMillis();
		System.out.println(post - pre);
	}

}
