package io.ibnuja.springrfc9457

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandler: ResponseEntityExceptionHandler() {
}