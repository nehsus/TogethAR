package io.agora.agoravideodemo.model

/**
 * Created by saiki on 04-06-2018.
 **/

data class IncomingCallEvent(val callerInfo: SignalMessage)

data class OutgoingCallEvent(val channelID: String, val receiverUserId: String, val receiverName: String, val receiverPhone: String)

class EndCallEvent

class RejectCallEvent

class LineBusyEvent

class ShowToastEvent(val message: String)

class ShowSnackEvent(val message: String)


class LoginFailedEvent(val error: Int)