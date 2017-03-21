package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.license.domain.Application
import com.morgenrete.mlicense.test.{FlatSpecWithDb, TestHelpers}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext

/**
  * Created by rwadowski on 21.03.17.
  */
class ApplicationDaoSpec extends FlatSpecWithDb with StrictLogging with Matchers with TestHelpers {
  behavior of "ApplicationDao"

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val applicationDao = new ApplicationDao(sqlDatabase)

  override def beforeEach(): Unit = {
    super.beforeEach()
    for(i <- 1 to randomIds.size) {
      val name = "name_" + i
      applicationDao.add(Application(randomIds(i - 1), name)).futureValue
    }
  }

  it should "add new application" in {
    //Given
    val name = "AppName"
    val app = newApplication(name)

    //When
    applicationDao.add(app).futureValue

    //Then
    applicationDao.findById(app.id).futureValue should be ('defined)
  }

  it should "delete application by id" in {
    //Given
    val name = "AppName"
    val app = newApplication(name)

    //When
    applicationDao.add(app).futureValue

    //Then
    applicationDao.delete(app.id).futureValue
    applicationDao.findById(app.id).futureValue should not be 'defined
  }

  it should "update application" in {
    //Given
    val name = "AppName"
    val newName = "NewAppName"
    val app = newApplication(name)
    val modifiedApp = Application(app.id, newName)

    //When
    applicationDao.add(app).futureValue
    applicationDao.update(modifiedApp).futureValue

    //Then
    applicationDao.findById(app.id).futureValue should be ('defined)
    applicationDao.findById(app.id).futureValue shouldEqual Some(modifiedApp)
  }

}
