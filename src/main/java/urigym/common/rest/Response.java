package urigym.common.rest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Response {

    private String code;
    private String message;
    private Object data;
    private boolean hasError;
    private boolean fieldError;

}
