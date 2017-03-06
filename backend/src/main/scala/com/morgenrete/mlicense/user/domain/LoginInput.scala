package com.morgenrete.mlicense.user.domain

/**
  * Created by rwadowski on 3/4/17.
  */

case class LoginInput(login: String, password: String, rememberMe: Option[Boolean])
