package models

case class Usuario(
  id: Long,
  papel: String,
  nomeCompleto: String,
  email: String,
  avatarUrl: String
)

