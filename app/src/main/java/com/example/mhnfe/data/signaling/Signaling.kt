package com.example.mhnfe.data.signaling

import com.example.mhnfe.data.signaling.model.Event


interface Signaling {
    fun onSdpOffer(event: Event)
    fun onSdpAnswer(event: Event)
    fun onIceCandidate(event: Event)
    fun onError(event: Event)
    fun onException(exception: Exception)
}