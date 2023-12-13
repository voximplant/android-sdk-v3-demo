package com.voximplant.sdk3demo.core.model.data

sealed class LoginError : Throwable() {
    data object InvalidUsername : LoginError()
    data object InvalidPassword : LoginError()
    data object AccountFrozen : LoginError()
    data object TimeOut : LoginError()
    data object NetworkIssue : LoginError()
    data object InternalError : LoginError()
    data object InvalidState : LoginError()
    data object Interrupted : LoginError()
    data object MauAccessDenied : LoginError()
    data object TokenExpired : LoginError()
}
