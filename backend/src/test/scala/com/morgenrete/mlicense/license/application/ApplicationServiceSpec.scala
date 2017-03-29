package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.test.{FlatSpecWithDb, TestHelpersWithDb}
import com.morgenrete.mlicense.user.domain.User
import org.scalatest.Matchers

/**
  * Created by rwadowski on 21.03.17.
  */
class ApplicationServiceSpec extends FlatSpecWithDb with Matchers with TestHelpersWithDb {

  val user: User = newRandomUser()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    userDao.add(user).futureValue
    applicationDao.add(newApplication("app1", user.id)).futureValue
    applicationDao.add(newApplication("app2", user.id)).futureValue
  }

  "create" should "create app with name that not exists" in {
    //given
    val name = "app3"
    val appCreate = newCreateApplication(name)

    //when
    val result = applicationService.create(appCreate.toApplication(user.id)).futureValue

    //then
    result should be (CreateApplicationResult.Success)
    applicationDao.findByName(name).futureValue should be ('defined)
  }

  "create" should "not create app with name that not exists" in {
    //given
    val name = "app1"
    val appCreate = newCreateApplication(name)

    //when
    val result = applicationService.create(appCreate.toApplication(user.id)).futureValue

    //then
    result should be (CreateApplicationResult.ApplicationExists)
    applicationDao.findByName(name).futureValue should be ('defined)
  }

  "update" should "update existing applications" in {
    //given
    val newName = "app3_new"
    val application = newRandomStoredApplication(user.id)
    val updateApplication = newUpdateApplication(newName, Some(application.id))

    //when
    val result = applicationService.update(updateApplication.toApplication(user.id)).futureValue

    //then
    result should be (UpdateApplicationResult.Success)
    applicationDao.findByName(application.name).futureValue should not be ('defined)
    applicationDao.findByName(newName).futureValue should be ('defined)
    applicationDao.findById(application.id).futureValue should be ('defined)
  }

  "update" should "fail during update non existing applications" in {
    //given
    val newName = "app3_new"
    val application = newRandomStoredApplication(user.id)
    val updateApplication = newUpdateApplication(newName)

    //when
    val result = applicationService.update(updateApplication.toApplication(user.id)).futureValue

    //then
    result should be (UpdateApplicationResult.ApplicationNotExists)
    applicationDao.findByName(application.name).futureValue should be ('defined)
    applicationDao.findById(application.id).futureValue should be ('defined)
    applicationDao.findByName(newName).futureValue should not be ('defined)
    applicationDao.findById(updateApplication.id).futureValue should not be ('defined)
  }
}
