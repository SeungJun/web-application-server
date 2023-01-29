package util;

import model.User;

import java.util.Map;

public class GetHttpHeader {

    // 클래스 이름을 어떻게 지어야 할지
    // 메소드의 반환값과 매개변수는 어떻게 받아야할지
    // 테스트 코드를 어떻게 작성할지
    public static String GetHttpUrl(String input){

            String []tokens=input.split(" ");
            //첫번째 위치 인덱스 index.html가져옴
            return tokens[1];
    }

    public static void GetJoin(String url){
        // ? 로 requestPath와 params를 나눈다.
        int index = url.indexOf("?");
        String requestPath = url.substring(0,index);
        String params = url.substring(index+1);
        // Utills의 parseQueryString을 이용해 구분
        Map<String,String> info = HttpRequestUtils.parseQueryString(params);

        User user = new User(info.get("userId"),info.get("password"),info.get("name"),info.get("email"));
    }

}
