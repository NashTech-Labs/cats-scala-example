package com.knoldus.eithert

import java.time.Instant
import java.util.UUID

import cats.data.EitherT
import cats.implicits._
import com.knoldus.eithert.AlbumService.AlbumServiceError
import com.knoldus.eithert.contracts.common.WithId
import com.knoldus.eithert.contracts.{ Album, AlbumStatus, User }
import com.knoldus.eithert.dao.AlbumDao

import scala.concurrent.{ ExecutionContext, Future }

class AlbumService(implicit val ec: ExecutionContext) {

  val dao: AlbumDao = new AlbumDao

  def create(
    name: String,
  )(implicit user: WithId[User]): Future[Either[AlbumServiceError, WithId[Album]]] = {

    def validateAlbumName: Either[AlbumServiceError, Unit] = {
      Either.cond(
        name.nonEmpty,
        (),
        AlbumServiceError.AlbumNameIsEmpty
      )
    }

    def createAlbum: Future[WithId[Album]] = {
      val now = Instant.now()
      Future.successful(
        WithId(
          Album(
            UUID.fromString(user.id),
            name,
            AlbumStatus.Saving,
            now,
            now
          ),
          UUID.randomUUID().toString
        )
      )
    }

    val result = for {
      _ <- EitherT.fromEither[Future](validateAlbumName)
      album <- EitherT.right[AlbumServiceError](createAlbum)
    } yield album

    result.value

  }

  def get(id: String)(implicit user: WithId[User]): Future[Either[AlbumServiceError, WithId[Album]]] = {
    val result = for {
      withIdOption <- EitherT.right[AlbumServiceError](dao.get(id))
      /**
        * If we have `Either[Left, Right]` and to convert it in
        * `EitherT[F[], Left, Right]` we use `EitherT.fromEither`
        */
      album <- EitherT(ensureEntityFound(withIdOption))
      _ <- EitherT(ensureCanRead(album, user))
    } yield album
    result.value
  }

  private final def ensureCanRead(album: WithId[Album], user: WithId[User]): Future[Either[AlbumServiceError, Unit]] = {
    Future.successful(
      Either.cond(
        album.entity.ownerId.toString == user.id,
        (),
        AlbumServiceError.AccessDenied
      )
    )
  }

  private final def ensureEntityFound(withIdOption: Option[WithId[Album]]): Future[Either[AlbumServiceError.AlbumNotFound.type, WithId[Album]]] = {
    /**
      * Here we have converting `Option` value to `EitherT`. `fromOption` takes two
      * parameter one `Option[T]` and other `Error value` (Left value) which we will
      * get if we get `None` from option value.
      */
    EitherT.fromOption[Future](withIdOption, AlbumServiceError.AlbumNotFound).value
  }

}

object AlbumService {

  sealed trait AlbumServiceError

  object AlbumServiceError {

    case object AlbumNameIsEmpty extends AlbumServiceError
    case object AlbumNotFound extends AlbumServiceError
    case object AccessDenied extends AlbumServiceError

  }

}
