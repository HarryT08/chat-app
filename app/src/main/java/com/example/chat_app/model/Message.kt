package com.example.chat_app.model

class Message {
    var mesaggeId: String? = null
    var mesagge: String? = null
    var senderId: String? = null
    var imageUrl: String? = null
    var timeStamp: Long = 0

    constructor(){}
    constructor(
        mesagge: String?,
        senderId: String?,
        timeStamp :Long
    ){
        this.mesagge = mesagge
        this.senderId = senderId
        this.timeStamp = timeStamp
    }
}