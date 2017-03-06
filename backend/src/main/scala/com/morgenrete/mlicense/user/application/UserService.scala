package com.morgenrete.mlicense.user.application

import java.time.{Instant, ZoneOffset}
import java.util.UUID

import com.morgenrete.mlicense.common.Utils
import com.morgenrete.mlicense.user.UserId
import com.morgenrete.mlicense.user.domain.{BasicUserData, LoginInput, RegistrationInput, User}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by rwadowski on 3/5/17.
  */
class UserService(userDao: UserDao)(implicit ec: ExecutionContext) {

  def findById(userId: UserId): Future[Option[BasicUserData]] = {
    userDao.findBasicDataById(userId)
  }

  def registerNewUser(in: RegistrationInput): Future[UserRegisterResult] = {
    val login = in.login
    val email = in.email
    val password = in.password
    def checkUserExistence(): Future[Either[String, Unit]] = {
      val existingLoginFuture = userDao.findByLowerCasedLogin(login)
      val existingEmailFuture = userDao.findByEmail(email)

      for {
        existingLoginOpt <- existingLoginFuture
        existingEmailOpt <- existingEmailFuture
      } yield {
        existingLoginOpt.map(_ => Left("Login already in use!")).orElse(
          existingEmailOpt.map(_ => Left("E-mail already in use!"))
        ).getOrElse(Right((): Unit))
      }
    }

    def registerValidData() = checkUserExistence().flatMap {
      case Left(msg) => Future.successful(UserRegisterResult.UserExists(msg))
      case Right(_) =>
        val salt = Utils.randomString(128)
        val now = Instant.now().atOffset(ZoneOffset.UTC)
        val userAddResult = userDao.add(User.withRandomUUID(login, email.toLowerCase, password, salt, now))
        userAddResult.map(_ => UserRegisterResult.Success)
    }

    UserRegisterValidator.validate(login, email, password).fold(
      msg => Future.successful(UserRegisterResult.InvalidData(msg)),
      _ => registerValidData()
    )
  }

  def authenticate(loginInput: LoginInput): Future[Option[BasicUserData]] = {
    val login = loginInput.login
    val nonEncryptedPassword = loginInput.password
    userDao.findByLoginOrEmail(login).map(userOpt =>
      userOpt.filter(u => User.passwordsMatch(nonEncryptedPassword, u)).map(BasicUserData.fromUser))
  }
}

sealed trait UserRegisterResult

object UserRegisterResult {

  case class InvalidData(msg: String) extends UserRegisterResult

  case class UserExists(msg: String) extends UserRegisterResult

  case object Success extends UserRegisterResult

}

object UserRegisterValidator {
  private val ValidationOk = Right(())
  val MinLoginLength = 3

  def validate(login: String, email: String, password: String): Either[String, Unit] =
    for {
      _ <- validLogin(login.trim).right
      _ <- validEmail(email.trim).right
      _ <- validPassword(password.trim).right
    } yield ()

  private def validLogin(login: String) = if (login.length >= MinLoginLength) ValidationOk else Left("Login is too short!")

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private def validEmail(email: String) = if (emailRegex.findFirstMatchIn(email).isDefined) ValidationOk else Left("Invalid e-mail!")

  private def validPassword(password: String) = if (password.nonEmpty) ValidationOk else Left("Password cannot be empty!")
}