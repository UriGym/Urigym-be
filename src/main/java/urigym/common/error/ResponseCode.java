package urigym.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {

    /**
     * Common Http Code
     */
    OK (200, "urigym.commom.error.http-message.200", "요청이 성공적으로 처리되었습니다."),
    CREATED (201, "urigym.commom.error.http-message.201", "성공적으로 생성되었습니다."),
    NO_CONTENT(204, "urigym.commom.error.http-message.204", "요청이 성공적으로 처리되었지만, 반환할 내용이 없습니다."),
    BAD_REQUEST (400, "urigym.commom.error.http-message.400", "잘못된 요청입니다."),
    NOT_FOUNT (404, "urigym.common.error.http-message.404", "찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "urigym.common.error.http-message.500", "서버 내부 오류가 발생하였습니다."),
    BAD_GATEWAY (502, "urigym.common.error.http-message.502", "잘못된 게이트웨이입니다."),
    SERVICE_UNAVAILABLE (503, "urigym.common.error-http-message.503", "서비스를 사용할 수 없습니다."),
    GATEWAY_TIMEOUT(504, "urigym.common.error-http-message.504", "게이트웨이 시간 초과입니다."),
    ;

//    private final int STATUS;
    private final int status;
//    private final String MESSAGECODE;
    private final String messageCode;
//    private final String DESCROPTION;
    private final String descroption;

}
