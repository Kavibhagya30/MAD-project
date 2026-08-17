package com.piieradication.agent.data.remote.dto

data class UserDto(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    val address: AddressDto?,
    val company: CompanyDto?
)

data class AddressDto(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String
)

data class CompanyDto(
    val name: String,
    val catchPhrase: String
)
