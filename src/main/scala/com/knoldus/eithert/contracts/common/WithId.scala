package com.knoldus.eithert.contracts.common

case class WithId[+T](entity: T, id: String)
