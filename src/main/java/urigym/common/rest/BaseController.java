package urigym.common.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import urigym.common.error.ResponseCode;

public class BaseController {

    protected ResponseEntity response(ResponseCode code){
        return response(code, null, null);
    }

    protected ResponseEntity response(ResponseCode code, Object data){
        return response(code, null, data);
    }

    protected ResponseEntity response(ResponseCode code, MultiValueMap<String, String> headers, Object data){
        var body = Response.builder()
                .code(code.name())
                .message(code.getMessageCode())
                .data(data)
                .build();

        return new ResponseEntity<>(body, headers, code.getStatus());
    }

    protected ResponseEntity errorResponse(ResponseCode code){
        return errorResponse(code, null);
    }

    protected ResponseEntity errorResponse(ResponseCode code, MultiValueMap<String, String> headers){
        var body = Response.builder()
                .code(code.name())
                .message(code.getMessageCode())
                .data(null)
                .hasError(true)
                .build();
        return new ResponseEntity<>(body, headers, code.getStatus());
    }

}
