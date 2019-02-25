package com.knoldus.eithert.dao

import java.time.Instant
import java.util.UUID

import com.knoldus.eithert.contracts.common.WithId
import com.knoldus.eithert.contracts.{ Album, AlbumStatus }

import scala.concurrent.Future

class AlbumDao {

  def get(id: String): Future[Option[WithId[Album]]] = {
    Future.successful(
      Option(
        WithId(
          Album(
            UUID.randomUUID(),
            "fake-album",
            AlbumStatus.Active,
            Instant.now(),
            Instant.now()
          ),
          id
        )
      )
    )
  }

}
