package com.knoldus.eithert.contracts

case class Picture(
  albumId: String,
  filePath: String,
  fileName: String,
  fileSize: Option[Long]
)
