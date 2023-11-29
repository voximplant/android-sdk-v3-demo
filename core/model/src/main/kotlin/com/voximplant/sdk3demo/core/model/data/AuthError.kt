package com.voximplant.sdk3demo.core.model.data

sealed class AuthError : Throwable() {
    data object InvalidUsername : AuthError()
    data object InvalidPassword : AuthError()
    data object AccountFrozen : AuthError()
    data object TimeOut : AuthError()
    data object NetworkIssue : AuthError()
    data object InternalError : AuthError()
    data object InvalidState : AuthError()
    data object Interrupted : AuthError()
    data object MauAccessDenied : AuthError()
    data object TokenExpired : AuthError()
}
