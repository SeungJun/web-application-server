package util;

public class GetHttpHeader {

    // 클래스 이름을 어떻게 지어야 할지
    // 메소드의 반환값과 매개변수는 어떻게 받아야할지
    public static String GetHttpUrl(String input){

            String []tokens=input.split(" ");
            //첫번째 위치 인덱스 index.html가져옴
            return tokens[1];
    }
}
