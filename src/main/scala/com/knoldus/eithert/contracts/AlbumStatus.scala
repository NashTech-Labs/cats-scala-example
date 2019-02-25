package com.knoldus.eithert.contracts

sealed trait AlbumStatus

object AlbumStatus {

  case object Saving extends AlbumStatus

  case object Uploading extends AlbumStatus

  case object Active extends AlbumStatus

  case object Failed extends AlbumStatus

}
