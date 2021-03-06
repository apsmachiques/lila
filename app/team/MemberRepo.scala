package lila
package team

import com.novus.salat._
import com.novus.salat.dao._
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.Imports._

import scalaz.effects._
import org.joda.time.DateTime

// db.team_member.ensureIndex({team:1})
// db.team_member.ensureIndex({user:1})
// db.team_member.ensureIndex({date: -1})
private[team] final class MemberRepo(collection: MongoCollection)
    extends SalatDAO[Member, String](collection) {

  def userIdsByTeamId(teamId: String): IO[List[String]] = io {
    (collection.find(teamIdQuery(teamId), DBObject("user" -> true)) map { obj ⇒
      obj.getAs[String]("user")
    }).flatten.toList
  }

  def teamIdsByUserId(userId: String): IO[List[String]] = io {
    (collection.find(userIdQuery(userId), DBObject("team" -> true)) map { obj ⇒
      obj.getAs[String]("team")
    }).flatten.toList
  }

  def removeByteamId(teamId: String): IO[Unit] = io {
    remove(teamIdQuery(teamId))
  }

  def removeByUserId(userId: String): IO[Unit] = io {
    remove(userIdQuery(userId))
  }

  def exists(teamId: String, userId: String): IO[Boolean] = io {
    collection.find(idQuery(teamId, userId)).limit(1).size != 0
  }

  def idQuery(teamId: String, userId: String) = DBObject("_id" -> id(teamId, userId))
  def id(teamId: String, userId: String) = Member.makeId(teamId, userId)
  def teamIdQuery(teamId: String) = DBObject("team" -> teamId)
  def userIdQuery(userId: String) = DBObject("user" -> userId)
  def sortQuery(order: Int = -1) = DBObject("date" -> order)

  def add(teamId: String, userId: String): IO[Unit] = io {
    insert(Member(team = teamId, user = userId))
  }

  def remove(teamId: String, userId: String): IO[Unit] = io {
    remove(idQuery(teamId, userId))
  }
}
