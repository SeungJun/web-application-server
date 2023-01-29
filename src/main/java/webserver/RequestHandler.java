package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.GetHttpHeader;
import util.HttpRequestUtils;

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


            log.debug("request : {}", line);
            


            // 읽어온 HTTP 요청 정보에서 첫번째 줄에서 요청 URL을 가져온다.
            String url = GetHttpHeader.GetHttpUrl(line);
            log.debug("request : {}", url);

            // GET /user/create?userId=t&password=t&name=t&email=t%40g HTTP/1.1
            // 정보가 위의 주석처럼 넘어오면 회원 가입 요청이다
            if(url.startsWith("/user/create")){
                // ? 로 requestPath와 params를 나눈다.
                int index = url.indexOf("?");
                String requestPath = url.substring(0,index);
                String params = url.substring(index+1);
                // Utills의 parseQueryString을 이용해 구분
                Map<String,String> info = HttpRequestUtils.parseQueryString(params);

                User user = new User(info.get("userId"),info.get("password"),info.get("name"),info.get("email"));
                log.debug("request : {}",user);

            }
            
            // 요청 URL에 해당하는 파일을 wdbapp 디렉토리에서 읽어 전달한다.
            byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
//            log.debug("request : {}", body);
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
