package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AccessControlTest {
  @Test
  fun `given owner and api key roles when building access control then roles are intersected`() {
    val owner =
      KomgaUser(
        email = "user@example.org",
        password = "password",
        roles = setOf(UserRoles.FILE_DOWNLOAD, UserRoles.PAGE_STREAMING),
      )
    val apiKey =
      ApiKey(
        userId = owner.id,
        key = "key",
        comment = "test",
        roles = setOf(UserRoles.FILE_DOWNLOAD),
      )

    val access = AccessControl.fromUserAndApiKey(owner, apiKey)

    assertThat(access.roles).containsExactlyInAnyOrder(UserRoles.FILE_DOWNLOAD)
  }

  @Test
  fun `given owner and api key library scopes when building access control then libraries are intersected`() {
    val owner =
      KomgaUser(
        email = "user@example.org",
        password = "password",
        sharedAllLibraries = false,
        sharedLibrariesIds = setOf("A", "B"),
      )
    val apiKey =
      ApiKey(
        userId = owner.id,
        key = "key",
        comment = "test",
        roles = setOf(UserRoles.FILE_DOWNLOAD, UserRoles.PAGE_STREAMING),
        sharedAllLibraries = false,
        sharedLibrariesIds = setOf("B", "C"),
      )

    val access = AccessControl.fromUserAndApiKey(owner, apiKey)

    assertThat(access.canAccessAllLibraries()).isFalse
    assertThat(access.getAuthorizedLibraryIds(null)).containsExactly("B")
  }

  @Test
  fun `given owner and api key restrictions when building access control then content checks are combined with and`() {
    val owner =
      KomgaUser(
        email = "user@example.org",
        password = "password",
        restrictions =
          ContentRestrictions(
            labelsAllow = setOf("teen"),
          ),
      )
    val apiKey =
      ApiKey(
        userId = owner.id,
        key = "key",
        comment = "test",
        restrictions =
          ContentRestrictions(
            ageRestriction = AgeRestriction(10, AllowExclude.ALLOW_ONLY),
          ),
      )

    val access = AccessControl.fromUserAndApiKey(owner, apiKey)

    assertThat(access.isContentAllowed(ageRating = 9, sharingLabels = setOf("teen"))).isTrue
    assertThat(access.isContentAllowed(ageRating = 12, sharingLabels = setOf("teen"))).isFalse
    assertThat(access.isContentAllowed(ageRating = 9, sharingLabels = setOf("adult"))).isFalse
  }
}
