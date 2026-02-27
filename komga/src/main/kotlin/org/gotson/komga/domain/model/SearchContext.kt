package org.gotson.komga.domain.model

class SearchContext private constructor(
  val userId: String?,
  val restrictions: List<ContentRestrictions>,
  val libraryIds: Collection<String>?,
) {
  constructor(user: KomgaUser?) :
    this(
      user?.id,
      listOfNotNull(user?.restrictions?.takeIf { it.isRestricted }),
      user?.getAuthorizedLibraryIds(null),
    )

  constructor(accessControl: AccessControl?) :
    this(
      accessControl?.userId,
      accessControl?.restrictions ?: emptyList(),
      accessControl?.getAuthorizedLibraryIds(null),
    )

  companion object {
    fun empty() = SearchContext(null, emptyList(), null)

    fun ofAnonymousUser() = SearchContext("UNUSED", emptyList(), null)
  }
}
