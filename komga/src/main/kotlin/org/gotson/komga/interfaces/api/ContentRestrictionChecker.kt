package org.gotson.komga.interfaces.api

import org.gotson.komga.domain.model.AccessControl
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class ContentRestrictionChecker(
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val bookRepository: BookRepository,
) {
  /**
   * Convenience function to check for content restriction.
   * This will retrieve data from repositories if needed.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestriction(
    komgaUser: KomgaUser,
    book: BookDto,
  ) = checkContentRestriction(AccessControl.fromUser(komgaUser), book)

  fun checkContentRestriction(
    accessControl: AccessControl,
    book: BookDto,
  ) {
    if (!accessControl.canAccessLibrary(book.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    if (accessControl.restrictions.isNotEmpty())
      seriesMetadataRepository.findById(book.seriesId).let {
        if (!accessControl.isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }
  }

  /**
   * Convenience function to check for content restriction.
   * This will retrieve data from repositories if needed.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestriction(
    komgaUser: KomgaUser,
    book: Book,
  ) = checkContentRestriction(AccessControl.fromUser(komgaUser), book)

  fun checkContentRestriction(
    accessControl: AccessControl,
    book: Book,
  ) {
    if (!accessControl.canAccessLibrary(book.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    if (accessControl.restrictions.isNotEmpty())
      seriesMetadataRepository.findById(book.seriesId).let {
        if (!accessControl.isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }
  }

  /**
   * Convenience function to check for content restriction.
   * This will retrieve data from repositories if needed.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestriction(
    komgaUser: KomgaUser,
    bookId: String,
  ) = checkContentRestriction(AccessControl.fromUser(komgaUser), bookId)

  fun checkContentRestriction(
    accessControl: AccessControl,
    bookId: String,
  ) {
    if (!accessControl.canAccessAllLibraries()) {
      bookRepository.getLibraryIdOrNull(bookId)?.let {
        if (!accessControl.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
    if (accessControl.restrictions.isNotEmpty())
      bookRepository.getSeriesIdOrNull(bookId)?.let { seriesId ->
        seriesMetadataRepository.findById(seriesId).let {
          if (!accessControl.isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  /**
   * Convenience function to check for content restriction.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestriction(
    komgaUser: KomgaUser,
    series: SeriesDto,
  ) = checkContentRestriction(AccessControl.fromUser(komgaUser), series)

  fun checkContentRestriction(
    accessControl: AccessControl,
    series: SeriesDto,
  ) {
    if (!accessControl.canAccessLibrary(series.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    if (!accessControl.isContentAllowed(series.metadata.ageRating, series.metadata.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
  }
}
