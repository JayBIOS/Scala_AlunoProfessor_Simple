package models.daos

import slick.driver.JdbcProfile
import play.api.db.slick.HasDatabaseConfigProvider

trait DAO extends BDTabelas with HasDatabaseConfigProvider[JdbcProfile]
