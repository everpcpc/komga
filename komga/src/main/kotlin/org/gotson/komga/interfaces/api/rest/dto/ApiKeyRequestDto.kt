package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class ApiKeyRequestDto(
  @get:NotBlank
  val comment: String,
  val roles: Set<String>? = null,
  @get:Valid
  val sharedLibraries: SharedLibrariesUpdateDto? = null,
  @get:Valid
  val ageRestriction: AgeRestrictionUpdateDto? = null,
  val labelsAllow: Set<String>? = null,
  val labelsExclude: Set<String>? = null,
)
