package com.knoldus.eithert

import java.util.UUID

import cats.data.EitherT
import cats.implicits._
import com.knoldus.eithert.AlbumService.AlbumServiceError
import com.knoldus.eithert.PictureService.PictureServiceError
import com.knoldus.eithert.contracts.common.WithId
import com.knoldus.eithert.contracts.{ Album, Picture, User }

import scala.concurrent.{ ExecutionContext, Future }

class PictureService(albumService: AlbumService)(
  implicit val ec: ExecutionContext
) {

  def create(
    albumId: String,
    pictureName: String,
    filePath: String,
    fileName: String,
    fileSize: String
  )(implicit user: WithId[User]): Future[Either[PictureServiceError, WithId[Picture]]] = {

    def savePicture(album: WithId[Album]): Future[WithId[Picture]] = {
      val picture = Picture(
        album.id,
        filePath,
        pictureName,
        None
      )
      Future.successful(
        WithId(
          picture,
          UUID.randomUUID().toString
        )
      )
    }

    val result = for {
      album <- EitherT(getAlbum(albumId, user))

      /**
        * Here we have fixed value returning from `savePicture` which is returning
        * Future of Picture with Id which is automatically always be a `Right` value
        * therefore we are using `EitherT.right` method which will change it's type to
        * `EitherT[Future, WithId[Picture], PictureServiceError]`.
        */
      pictureWithId <- EitherT.right[PictureServiceError](savePicture(album))
    } yield pictureWithId

    result.value

  }

  private def getAlbum(
    albumId: String,
    user: WithId[User],
    sharedResourceId: Option[String] = None
  ): Future[Either[PictureServiceError, WithId[Album]]] = {
    /**
      * From AlbumService when we will call get method it will return either album with id
      * or Album Service Error but here inorder to process we need to convert Album Service
      * Error to Picture Service Error for that we are using `leftMap` and converting
      * it using Pattern match.
      */
    albumService.get(albumId)(user).map {
      _.leftMap {
        case AlbumServiceError.AlbumNotFound => PictureServiceError.AlbumNotFound
        case AlbumServiceError.AccessDenied => PictureServiceError.AccessDenied
        case _ => throw new Exception()
      }
    }
  }

}

object PictureService {

  sealed trait PictureServiceError

  object PictureServiceError {

    object AlbumNotFound extends PictureServiceError
    object AccessDenied extends PictureServiceError

  }

}
