package com.masterminds.pulsecast

import android.app.Application
import com.masterminds.pulsecast.core.StreamDestinationRepository
import com.masterminds.pulsecast.streaming.BroadcastDestinationStore

class PulseCastStudioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        BroadcastDestinationStore.initialize(StreamDestinationRepository(applicationContext))
    }
}
