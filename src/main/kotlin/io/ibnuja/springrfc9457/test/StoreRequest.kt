package io.ibnuja.springrfc9457.test

import jakarta.validation.constraints.Min

data class StoreRequest(
	@field:Min(8)
	val username: String,
	@field:Min(8)
	val password: String
)
