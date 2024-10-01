package io.ibnuja.springrfc9457

import jakarta.servlet.ServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeAttribute
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@RestControllerAdvice
class CustomExceptionHandler(
	private val serverProperties: ServerProperties
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@ExceptionHandler(
		WebExchangeBindException::class, MethodArgumentNotValidException::class
	)
	fun validation(e: Exception, request: ServletRequest): ProblemDetail {
		val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED, "Validation error")
		logger.error("An unexpected error has occurred", e)

		if (shouldIncludeAttribute(serverProperties.error.includeBindingErrors, request, "errors")) {
			val validationErrors = when (e) {
				is WebExchangeBindException -> toMap(e.bindingResult)
				is MethodArgumentNotValidException -> toMap(e.bindingResult)
				else -> emptyMap()
			}
			problemDetail.setProperty("errors", validationErrors)
		}
		return problemDetail.apply {
			addProblemDetail(e, request)
		}
	}

	@ExceptionHandler(java.lang.Exception::class)
	fun handleCommonException(e: Exception?, request: ServletRequest): ProblemDetail {
		val problemDetail =
			ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error has occurred")
		logger.error("An unexpected error has occurred", e)
		return problemDetail.apply { addProblemDetail(e, request) }
	}

	private fun ProblemDetail.addProblemDetail(e: Exception?, request: ServletRequest) {
		val includeStackTrace = shouldIncludeAttribute(serverProperties.error.includeStacktrace, request, "trace")
		val includeMessage = shouldIncludeAttribute(serverProperties.error.includeMessage, request, "message")

		if (e != null) {
			if (includeStackTrace) {
				this.setProperty("stack-trace", e.javaClass)
			}
			if (includeMessage) {
				this.setProperty("message", e.message)
			}
		}
		this.setProperty("requestId", request.requestId)
	}

	// Generalized method for checking the inclusion of error attributes
	private fun shouldIncludeAttribute(
		includeAttribute: IncludeAttribute, request: ServletRequest, paramName: String
	): Boolean {
		return when (includeAttribute) {
			IncludeAttribute.ALWAYS -> true
			IncludeAttribute.ON_PARAM -> getBooleanParameter(request, paramName)
			IncludeAttribute.NEVER -> false
			else -> false
		}
	}

	private fun getBooleanParameter(request: ServletRequest, paramName: String): Boolean {
		val parameter: String? = request.getParameter(paramName)
		return parameter != null && !parameter.equals("false", ignoreCase = true)
	}

	private fun <T : Any> toMap(obj: T): Map<String, Any?> {
		return (obj::class as KClass<T>).memberProperties.associate { prop ->
			prop.name to prop.get(obj)?.let { value ->
				if (value::class.isData) {
					toMap(value)
				} else {
					value
				}
			}
		}
	}
}
