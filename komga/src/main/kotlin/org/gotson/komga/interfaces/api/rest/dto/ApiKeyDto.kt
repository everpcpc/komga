package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ApiKey
import org.gotson.komga.language.toUTCZoned
import java.time.ZonedDateTime

data class ApiKeyDto(
  val id: String,
  val userId: String,
  val key: String,
  val comment: String,
  val roles: Set<String>,
  val sharedAllLibraries: Boolean,
  val sharedLibrariesIds: Set<String>,
  val labelsAllow: Set<String>,
  val labelsExclude: Set<String>,
  val ageRestriction: AgeRestrictionDto?,
  val createdDate: ZonedDateTime,
  val lastModifiedDate: ZonedDateTime,
)

fun ApiKey.toDto() =
  ApiKeyDto(
    id = id,
    userId = userId,
    key = key,
    comment = comment,
    roles = roles.map { it.name }.toSet(),
    sharedAllLibraries = sharedAllLibraries,
    sharedLibrariesIds = sharedLibrariesIds,
    labelsAllow = restrictions.labelsAllow,
    labelsExclude = restrictions.labelsExclude,
    ageRestriction = restrictions.ageRestriction?.toDto(),
    createdDate = createdDate.toUTCZoned(),
    lastModifiedDate = lastModifiedDate.toUTCZoned(),
  )

fun ApiKeyDto.redacted() = copy(key = "*".repeat(6))
