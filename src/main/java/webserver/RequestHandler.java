package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.GetHttpHeader;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);



    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        // Socket을 통하여 클라이언트와 서버가 통신한다
        // 클라이언트가 서버에게 보낸 정보를 inputstream을 통해 입력 받고 서버가 클라이언트에게 보내는것은 outputstream을 이용한다.
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            // InputStreamReader를 한줄로 읽기 위해서 사용
            // 요청을 받을때 UTF-8로 인코딩 해주기
            InputStreamReader sr = new InputStreamReader(in,"UTF-8");
            BufferedReader br = new BufferedReader(sr);

            String line = br.readLine();

            // line이 null값이면 예외처리
            if (line == null) return;

            String[] tokens = line.split(" ");
            boolean logined = false;

            // body의 데이터의 길이를 저장할 변수
            int contentLength = 0;
            while (!"".equals(line)) {
                log.debug("header : {}", line);
                // 라인을 한 줄씩 읽어온다.
                line = br.readLine();
                if(line.contains("Cookie")){
                    // 헤더중 쿠키의 값을 가져와 로그인 여부를 확인


                    logined  = isLogin(line);
                }
                // POST 방식으로 동작 할 시 헤더에 포함되는 내용으로 본문데이터에 대한 길이가 담겨있다.
                if (line.contains("Content-Length")) {
                    // 헤더중 content-length가 포함 되어 있으면 저장
                    contentLength = getContentLength(line);
                }
            }

            log.debug("request : {}", line);


            // line을 계속 사용해서 더이상 토큰으로 구분 할 수없다.
            // 읽어온 HTTP 요청 정보에서 첫번째 줄에서 요청 URL을 가져온다.
//            String url = GetHttpHeader.GetHttpUrl(line);
//            log.debug("request : {}", url);

            String url = tokens[1];

            // GET /user/create?userId=t&password=t&name=t&email=t%40g HTTP/1.1
            // 정보가 위의 주석처럼 넘어오면 회원 가입 요청이다
            if ("/user/create".equals(url)) {
                String body = IOUtils.readData(br, contentLength);
                // Utills의 parseQueryString을 이용해 구분
                Map<String, String> info = HttpRequestUtils.parseQueryString(body);

                User user = new User(info.get("userId"), info.get("password"), info.get("name"), info.get("email"));
                log.debug("User : {}", user);
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos,"/index.html");
                DataBase.addUser(user);
            } else if ("/user/login".equals(url)) {
                String body = IOUtils.readData(br, contentLength);
                // Utills의 parseQueryString을 이용해 구분
                Map<String, String> info = HttpRequestUtils.parseQueryString(body);
                // login Id 소문자
                User user = DataBase.findUserById(info.get("userId"));
                if(user == null){
                    responseResource(out,"/user/login_failed.html");
                    return;
                }
                if(user.getPassword().equals(info.get("password"))){
                    DataOutputStream dos = new DataOutputStream(out);
                    response302LoginSuccessHeader(dos);
                }else {
                    responseResource(out,"/user/login_failed.html");
                }
            } else if ("/user/list".equals(url)) {
                // 로그인 상태가 아니라면 로그인 화면으로 이동
                if(!logined){
                    responseResource(out, "/user/login.html");
                    return;
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border ='1'>");
                for(User user : users){
                    sb.append("<tr>");
                    sb.append("<td>" + user.getUserId() + "</td>");
                    sb.append("<td>" + user.getName() + "</td>");
                    sb.append("<td>" + user.getEmail() + "</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
                // 요청 URL에 해당하는 파일을 wdbapp 디렉토리에서 읽어 전달한다.
                byte[] body = sb.toString().getBytes();
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }else if (url.endsWith(".css")) {

                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200CssHeader(dos, body.length);
                responseBody(dos, body);
            }
            else {
                responseResource(out, url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // 응답을 해주는 것
    // 상태코드 200은 성공을 의미
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            // 상태라인
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            // 응답 헤더
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

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " +url +" \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseResource(OutputStream out, String url) throws IOException{
        // 요청 URL에 해당하는 파일을 wdbapp 디렉토리에서 읽어 전달한다.
        DataOutputStream dos = new DataOutputStream(out);

        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
//            log.debug("request : {}", body);
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            // Content type을 cc로 바꿔준다.
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private int getContentLength(String line){
        // 헤더중 content-length가 포함 되어 있으면 저장
        String[] temp = line.split(":");
        return Integer.parseInt(temp[1].trim());
    }

    private boolean isLogin(String line){
        // 헤더중 쿠키의 값을 가져와 로그인 여부를 확인
        String[] temp = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(temp[1].trim());
        String value = cookies.get("logined");
        if( value == null){
            return false;
        }
        return  Boolean.parseBoolean(value);
    }
}
