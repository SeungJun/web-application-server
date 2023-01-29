package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.GetHttpHeader;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            DataOutputStream dos = new DataOutputStream(out);
            // InputStreamReader를 한줄로 읽기 위해서 사용
            InputStreamReader sr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(sr);

            String line = br.readLine();

            // line이 null값이면 예외처리
            if(line == null) return;

//            while (!"".equals(line)){
//                log.debug("request : {}",line);
//                // 라인을 한 줄씩 읽어온다.
//                line = br.readLine();
//                System.out.printf(line);
//            }

            String url = GetHttpHeader.GetHttpUrl(line);
            log.debug("request : {}", url);
            byte[] body = "Hello World test".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
