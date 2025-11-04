// 경로: src/main/java/com/teamfiv5/fiv5/global/exception/code/BaseCode.java
package com.teamfiv5.fiv5.global.exception.code;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}