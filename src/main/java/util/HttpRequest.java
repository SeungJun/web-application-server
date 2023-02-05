package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private HttpMethod method;
    private String path;

    // 헤더 정보를 담는것
    private Map<String, String> headers = new HashMap<String, String>();
    // 내을 담는것
    private Map<String, String> params = new HashMap<String, String>();
    private RequestLine requestLine;

    public HttpRequest(InputStream in) {
        try {
            // 요청받은 값을 인코딩 후 입력
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            // 한줄 읽기
            String line = br.readLine();
            if (line == null) {
                return;
            }
            requestLine = new RequestLine(line);
            line = br.readLine();

            // 한줄씩 읽어 헤더에 넣어준다
            while (!line.equals("")){
                log.debug("header : {}",line);
                // : 기준으로 파라미터와 내용이 들어가 있다
                String[] tokens = line.split(":");
                // 헤더에 분리한 값을 넣어준다
                headers.put(tokens[0].trim(),tokens[1].trim());
                line = br.readLine();
            }

            if ("POST".equals(getMethod())) {
                String body = IOUtils.readData(br,
                        Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            } else {
                params  = requestLine.getParams();
            }
        }
        // 더 상위버전으로 에러 처리
        catch (IOException io) {
            log.error(io.getMessage());
        }

    }

//    private void processRequestLine(String requestLine) {
//        log.debug("requset line : {}", requestLine);
//        String[] tokens = requestLine.split(" ");
//        // 메소드 : POST, GET 방식인지 확인
//        method = tokens[0];
//
//
//        // 이부분이 왜 POST 인지
//        if ("POST".equals(method)) {
//            path = tokens[1];
//            return;
//        }
//
//        // ?를 기준으로 메소드와 파라미터가 나누어진다.
//        int index = tokens[1].indexOf("?");
//        // 왜 -1인지?
//        if (index == -1) {
//            path = tokens[1];
//
//        } else {
//            path = tokens[1].substring(0, index);
//            params = HttpRequestUtils.parseQueryString(tokens[1].substring(index + 1));
//        }
//    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParam(String name) {
        return params.get(name);
    }
}
