package org.egov.chat.xternal.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@Slf4j
public class FileStoreTest {


    @Test
    public void test() throws IOException {

        FileStore fileStore = new FileStore();

//        fileStore.downloadAndStore("http://www.pdf995.com/samples/pdf.pdf",
//            "pb.amritsar", "rainmaker-pgr");

        File tempFile = fileStore.getFileAt("http://www.pdf995.com/samples/pdf.pdf");

//        File file = new File(FileStoreTest.class.getClassLoader() + "/tmp/qwe/asd.pdf");

        log.info(tempFile.getAbsolutePath());

//        tempFile.delete();
    }

}