package io.agora.agoravideodemo.utils

import io.agora.IAgoraAPI

/**
 * Created by saiki on 02-06-2018.
 */
interface BaseICallBack: IAgoraAPI.ICallBack{
    override fun onChannelQueryUserIsIn(p0: String?, p1: String?, p2: Int) {
        
    }

    override fun onMsg(p0: String?, p1: String?, p2: String?) {
        
    }

    override fun onInviteFailed(p0: String?, p1: String?, p2: Int, p3: Int, p4: String?) {
        
    }

    override fun onInviteMsg(p0: String?, p1: String?, p2: Int, p3: String?, p4: String?, p5: String?) {
        
    }

    override fun onLogout(ecode: Int) {
        
    }

    override fun onChannelUserLeaved(p0: String?, p1: Int) {
        
    }

    override fun onChannelAttrUpdated(p0: String?, p1: String?, p2: String?, p3: String?) {
        
    }

    override fun onLoginFailed(ecode: Int) {
        
    }

    override fun onUserAttrResult(p0: String?, p1: String?, p2: String?) {
        
    }

    override fun onInviteAcceptedByPeer(p0: String?, p1: String?, p2: Int, p3: String?) {
        
    }

    override fun onMessageSendError(messageID: String?, ecode: Int) {
        
    }

    override fun onUserAttrAllResult(p0: String?, p1: String?) {
        
    }

    override fun onReconnected(p0: Int) {
        
    }

    override fun onMessageAppReceived(p0: String?) {
        
    }

    override fun onBCCall_result(p0: String?, p1: String?, p2: String?) {
        
    }

    override fun onInvokeRet(p0: String?, p1: String?, p2: String?) {
        
    }

    override fun onChannelUserList(p0: Array<out String>?, p1: IntArray?) {
        
    }

    override fun onReconnecting(p0: Int) {
        
    }

    override fun onChannelJoinFailed(p0: String?, p1: Int) {
        
    }

    override fun onQueryUserStatusResult(p0: String?, p1: String?) {
        
    }

    override fun onMessageChannelReceive(p0: String?, p1: String?, p2: Int, p3: String?) {
        
    }

    override fun onInviteReceived(p0: String?, p1: String?, p2: Int, p3: String?) {
        
    }

    override fun onLog(p0: String?) {
        
    }

    override fun onInviteReceivedByPeer(p0: String?, p1: String?, p2: Int) {
        
    }

    override fun onDbg(p0: String?, p1: ByteArray?) {
        
    }

    override fun onInviteRefusedByPeer(p0: String?, p1: String?, p2: Int, p3: String?) {
        
    }

    override fun onInviteEndByPeer(p0: String?, p1: String?, p2: Int, p3: String?) {
        
    }

    override fun onChannelJoined(p0: String?) {
        
    }

    override fun onInviteEndByMyself(p0: String?, p1: String?, p2: Int) {
        
    }

    override fun onLoginSuccess(uid: Int, fd: Int) {
        
    }

    override fun onMessageSendSuccess(messageID: String?) {
        
    }

    override fun onMessageInstantReceive(account: String?, uid: Int, msg: String?) {
        
    }

    override fun onChannelUserJoined(p0: String?, p1: Int) {
        
    }

    override fun onChannelQueryUserNumResult(p0: String?, p1: Int, p2: Int) {
        
    }

    override fun onError(p0: String?, p1: Int, p2: String?) {
        
    }

    override fun onChannelLeaved(p0: String?, p1: Int) {
        
    }

    override fun onMessageSendProgress(p0: String?, p1: String?, p2: String?, p3: String?) {
        
    }

}
