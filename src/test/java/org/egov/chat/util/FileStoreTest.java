package org.egov.chat.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.mime.MimeTypeException;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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


    HttpHeaders getDefaultHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authentication", "Bearer s6iyrz5y8rPApBQ2gQ3oog==");
        return headers;
    }


    @Test
    public void testGetFileFromKarix() {
//        RestTemplate restTemplate = new RestTemplate();
//
//        File file = new File("download.jpeg");
//        String fileLink = "https://rcmmedia.instaalerts.zone/services/media/download?filedata=h05OM%2Bxdf16s" +
//                "%2FNzUqY5JoMWfPfloimADOeXOJIoaDaE5mceypXTzO4aZTV82Mvz7KmaA7kUC7SvtOm%2F6vYiNUg%3D%3D";
//
//        HttpHeaders httpHeaders = getDefaultHttpHeaders();
//
//        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
//
//        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(fileLink, HttpMethod.GET, httpEntity, byte[].class);
//
//        log.info(String.valueOf(responseEntity.getStatusCode()));
    }

    @Test
    public void testDownloadFromKarixUsingBasicHttp() throws IOException, MimeTypeException {

//        URL url = new URL("https://rcmmedia.instaalerts.zone/services/media/download?filedata=h05OM%2Bxdf16s%2FNzUqY5JoMWfPfloimADOeXOJIoaDaE5mceypXTzO4aZTV82Mvz7KmaA7kUC7SvtOm%2F6vYiNUg%3D%3D");
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
//        conn.setRequestProperty("Authentication", "Bearer s6iyrz5y8rPApBQ2gQ3oog==");
//        conn.setDoOutput(true);
//
//        System.out.println( conn.getResponseCode() + " " + conn.getContentLength() + " " + conn.getContentType());
//
//        String ext = MimeTypes.getDefaultMimeTypes().forName(conn.getContentType()).getExtension();
//
//        InputStream inputStream = conn.getInputStream();
//
//        File file = File.createTempFile("chatbot", ext);
//        FileOutputStream fileOutputStream = new FileOutputStream(file);
//
//        IOUtils.copy(inputStream, fileOutputStream);
//
//        System.out.println(file.length());
//        System.out.println(file.getName());
//        System.out.println(file.getAbsolutePath());
//
//        file.delete();
    }

}