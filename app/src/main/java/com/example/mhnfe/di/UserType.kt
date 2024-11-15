package com.example.mhnfe.di

enum class UserType {
    MASTER,
    VIEWER,
    CCTV;

    companion object {
        fun fromString(type: String?): UserType {
            return when (type?.lowercase()) {
                "master" -> MASTER
                "viewer" -> VIEWER
                "cctv" -> CCTV
                else -> VIEWER
            }
        }
    }
}