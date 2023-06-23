package com.example.chat_app.model

class User {
    var uid:String? = null
    var name:String? = null
    var lastname:String? = null
    var phoneNumber : String? =  null
    var email:String? = null
    var username:String? = null
    var profileImage:String? = null
    var lastMsg: String? = null

    constructor(){}
    constructor(
        uid:String?,
        name:String?,
        username:String?,
        lastname:String?,
        phoneNumber:String?,
        profileImage:String?,
        email:String?
    ) {
        this.uid = uid
        this.name = name
        this.username = username
        this.lastname = lastname
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
        this.email = email
    }

}