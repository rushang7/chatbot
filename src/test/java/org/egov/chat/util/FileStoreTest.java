package org.egov.chat.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class FileStoreTest {


    @Test
    public void test() throws IOException {

//        FileStore fileStore = new FileStore();
//
//        fileStore.downloadAndStore("http://www.pdf995.com/samples/pdf.pdf",
//            "pb.amritsar", "rainmaker-pgr");
//
//        File tempFile = fileStore.getFileAt("http://www.pdf995.com/samples/pdf.pdf");
//
//        File file = new File(FileStoreTest.class.getClassLoader() + "/tmp/qwe/asd.pdf");
//
//        log.info(tempFile.getAbsolutePath());
//
//        tempFile.delete();
    }


    @Test
    public void fileBase64TransformTest() throws IOException {

//        String filename = "/home/rushang/Downloads/Sandbox user guide.pdf";
//        File file = new File(filename);

//        FileInputStream fileInputStream = new FileInputStream(file);
//
//        File f2 = new File(filename + ".enc");
//        FileOutputStream fileOutputStream = new FileOutputStream(f2);
//        OutputStream outputStream = Base64.getEncoder().wrap(fileOutputStream);
//
//        int _byte;
//        while ((_byte = fileInputStream.read()) != -1)
//        {
//            outputStream.write(_byte);
//        }
//
//        outputStream.close();

//        String asd = new String(Base64.getEncoder().encode(FileUtils.readFileToByteArray(file)));
//
//        System.out.println(asd.length());

//        FileInputStream fileInputStream1 = new FileInputStream(f2);

//        String string = new String( FileUtils.readFileToByteArray(f2) );
//
//        System.out.println(string.length());

//        f2.delete();

    }

    @Test
    public void readEncodedFile() throws IOException {
//        String filename = "/home/rushang/Downloads/Sandbox user guide.pdf.enc";
//
//        System.out.println(FileUtils.sizeOf(new File(filename)));

    }

}