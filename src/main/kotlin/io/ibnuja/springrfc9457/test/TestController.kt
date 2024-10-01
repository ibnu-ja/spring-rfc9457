package io.ibnuja.springrfc9457.test

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RequestMapping("/test")
@RestController
class TestController {
	@GetMapping
	fun index(): TestIndexResponse {
		return TestIndexResponse(
			"Hello World!!"
		)
	}

	@PostMapping
	fun store(@RequestBody @Validated request: StoreRequest) {
		throw NotImplementedError("TBA")
	}
}