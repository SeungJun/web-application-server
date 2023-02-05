package util;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RequestLineTest {
    @Test
    public void crate_mothod(){
        RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals("GET",line.getMethod());

       line = new RequestLine("POST /index.html HTTP/1.1");
        assertEquals("POST",line.getMethod());
    }

    @Test
    public void crate_path_and_params(){
        RequestLine line = new RequestLine("GET /user/create?userId=javajigi&password=pass HTTP/1.1");
        assertEquals("GET",line.getMethod());
        assertEquals("/user/create",line.getPath());
        Map<String,String> params = line.getParams();

        assertEquals(2,params.size());
    }
}
