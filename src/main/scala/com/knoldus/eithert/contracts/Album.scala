package com.knoldus.eithert.contracts

import java.time.Instant
import java.util.UUID

case class Album (
  ownerId: UUID,
  name: String,
  status: AlbumStatus,
  created: Instant,
  updated: Instant
)
