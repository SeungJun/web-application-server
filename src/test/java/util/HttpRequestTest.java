package util;

import org.junit.Test;
import webserver.HttpRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest {
    private  String testDirectory = "./src/test/resources";

    // 테스트하는데 오류발생
    @Test
    public void request_GET() throws Exception{
        InputStream in = new FileInputStream(new File(testDirectory)+"/Http_GET.txt");
        HttpRequest request = new HttpRequest(in);

        assertEquals("GET",request.getMethod());
        assertEquals("/user/create",request.getPath());
        assertEquals("keep-alive",request.getHeader("Connection"));
        assertEquals("javajigi",request.getParam("userId"));
    }

    @Test
    public void request_POST() throws Exception{
        InputStream in = new FileInputStream(new File(testDirectory)+"/Http_POST.txt");
        HttpRequest request = new HttpRequest(in);

        assertEquals("POST",request.getMethod());
        assertEquals("/user/create",request.getPath());
        assertEquals("keep-alive",request.getHeader("Connection"));
        assertEquals("javajigi",request.getParam("userId"));
    }
}
