package com.morgenrete.mlicense.license.application

import java.util.UUID

import com.morgenrete.mlicense.license.domain.Application
import com.morgenrete.mlicense.test.{FlatSpecWithDb, TestHelpersWithDb}
import com.morgenrete.mlicense.user.domain.User
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.Matchers

/**
  * Created by rwadowski on 21.03.17.
  */
class ApplicationDaoSpec extends FlatSpecWithDb with StrictLogging with Matchers with TestHelpersWithDb {
  behavior of "ApplicationDao"

  val user: User = newRandomUser()

  override def beforeEach(): Unit = {
    super.beforeEach()
    userDao.add(user).futureValue
    for(i <- 1 to randomIds.size) {
      val name = "name_" + i
      applicationDao.add(Application(randomIds(i - 1), name, user.id)).futureValue
    }
  }

  it should "add new application" in {
    //Given
    val name = "AppName"
    val app = newApplication(name, user.id)

    //When
    applicationDao.add(app).futureValue

    //Then
    applicationDao.findById(app.id).futureValue should be ('defined)
  }

  it should "delete application by id" in {
    //Given
    val name = "AppName"
    val app = newApplication(name, user.id)

    //When
    applicationDao.add(app).futureValue

    //Then
    applicationDao.delete(app.id).futureValue
    applicationDao.findById(app.id).futureValue should not be ('defined)
  }

  it should "delete application by name" in {
    //Given
    val name = "AppName"
    val app = newApplication(name, user.id)

    //When
    applicationDao.add(app).futureValue

    //Then
    applicationDao.findByName(app.name).futureValue should be ('defined)
  }

  it should "update application that belongs to user" in {
    //Given
    val name = "AppName"
    val newName = "NewAppName"
    val app = newApplication(name, user.id)
    val modifiedApp = Application(app.id, newName, user.id)

    //When
    applicationDao.add(app).futureValue
    val res = applicationDao.update(modifiedApp).futureValue

    //Then
    applicationDao.findById(app.id).futureValue should be ('defined)
    applicationDao.findById(app.id).futureValue shouldEqual Some(modifiedApp)
    res shouldEqual Some(modifiedApp)
  }

  it should "not update application that not belongs to user" in {
    //Given
    val name = "AppName"
    val newName = "NewAppName"
    val app = newApplication(name, user.id)
    val modifiedApp = Application(app.id, newName, UUID.randomUUID())

    //When
    applicationDao.add(app).futureValue
    val res = applicationDao.update(modifiedApp).futureValue

    //Then
    applicationDao.findById(app.id).futureValue should be ('defined)
    applicationDao.findById(app.id).futureValue shouldEqual Some(app)
    res shouldEqual None
  }


  it should "fetch only data belonging to user" in {
    //given
    val user1 = newRandomStoredUser()
    val app1 = newRandomStoredApplication(user1.id)

    //when
    val result = applicationDao.allForUser(user1.id).futureValue

    //then
    result.size shouldEqual 1
    result.head shouldEqual app1
  }
}
