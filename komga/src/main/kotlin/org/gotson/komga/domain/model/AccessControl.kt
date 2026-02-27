package org.gotson.komga.domain.model

import org.gotson.komga.language.lowerNotBlank

data class AccessControl(
  val userId: String,
  val roles: Set<UserRoles>,
  val sharedLibrariesIds: Set<String>,
  val sharedAllLibraries: Boolean,
  val restrictions: List<ContentRestrictions> = emptyList(),
) {
  @delegate:Transient
  val isAdmin: Boolean by lazy {
    roles.contains(UserRoles.ADMIN)
  }

  fun canAccessAllLibraries(): Boolean = sharedAllLibraries || isAdmin

  fun getAuthorizedLibraryIds(libraryIds: Collection<String>?): Collection<String>? =
    when {
      !canAccessAllLibraries() && libraryIds != null -> libraryIds.intersect(sharedLibrariesIds)
      !canAccessAllLibraries() && libraryIds == null -> sharedLibrariesIds
      libraryIds != null -> libraryIds
      else -> null
    }

  fun canAccessLibrary(libraryId: String): Boolean = canAccessAllLibraries() || sharedLibrariesIds.any { it == libraryId }

  fun canAccessLibrary(library: Library): Boolean = canAccessAllLibraries() || sharedLibrariesIds.any { it == library.id }

  fun isContentAllowed(
    ageRating: Int? = null,
    sharingLabels: Set<String> = emptySet(),
  ): Boolean = restrictions.all { it.isContentAllowed(ageRating, sharingLabels) }

  companion object {
    fun fromUser(user: KomgaUser): AccessControl =
      AccessControl(
        userId = user.id,
        roles = user.roles,
        sharedLibrariesIds = user.sharedLibrariesIds,
        sharedAllLibraries = user.sharedAllLibraries,
        restrictions = listOf(user.restrictions).takeIf { user.restrictions.isRestricted } ?: emptyList(),
      )

    fun fromUserAndApiKey(
      user: KomgaUser,
      apiKey: ApiKey,
    ): AccessControl {
      val owner = fromUser(user)
      val key = fromApiKey(apiKey)

      val effectiveRoles = owner.roles.intersect(key.roles)
      val ownerLibraryIds = owner.getAuthorizedLibraryIds(null)
      val keyLibraryIds = key.getAuthorizedLibraryIds(null)
      val effectiveLibraryIds =
        when {
          ownerLibraryIds == null && keyLibraryIds == null -> null
          ownerLibraryIds == null -> keyLibraryIds
          keyLibraryIds == null -> ownerLibraryIds
          else -> ownerLibraryIds.intersect(keyLibraryIds)
        }

      val effectiveRestrictions = (owner.restrictions + key.restrictions).ifEmpty { emptyList() }

      return AccessControl(
        userId = user.id,
        roles = effectiveRoles,
        sharedLibrariesIds = effectiveLibraryIds?.toSet() ?: emptySet(),
        sharedAllLibraries = effectiveLibraryIds == null,
        restrictions = effectiveRestrictions,
      )
    }

    private fun fromApiKey(apiKey: ApiKey): AccessControl =
      AccessControl(
        userId = apiKey.userId,
        roles = apiKey.roles,
        sharedLibrariesIds = apiKey.sharedLibrariesIds,
        sharedAllLibraries = apiKey.sharedAllLibraries,
        restrictions = listOf(apiKey.restrictions).takeIf { apiKey.restrictions.isRestricted } ?: emptyList(),
      )
  }
}

fun ContentRestrictions.isContentAllowed(
  ageRating: Int? = null,
  sharingLabels: Set<String> = emptySet(),
): Boolean {
  val labels = sharingLabels.lowerNotBlank().toSet()

  val ageAllowed =
    if (ageRestriction?.restriction == AllowExclude.ALLOW_ONLY)
      ageRating != null && ageRating <= ageRestriction.age
    else
      null

  val labelAllowed =
    if (labelsAllow.isNotEmpty())
      labelsAllow.intersect(labels).isNotEmpty()
    else
      null

  val allowed =
    when {
      ageAllowed == null -> labelAllowed != false
      labelAllowed == null -> ageAllowed != false
      else -> ageAllowed != false || labelAllowed != false
    }
  if (!allowed) return false

  val ageDenied =
    if (ageRestriction?.restriction == AllowExclude.EXCLUDE)
      ageRating != null && ageRating >= ageRestriction.age
    else
      false

  val labelDenied =
    if (labelsExclude.isNotEmpty())
      labelsExclude.intersect(labels).isNotEmpty()
    else
      false

  return !ageDenied && !labelDenied
}
