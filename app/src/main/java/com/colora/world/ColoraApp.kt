package com.colora.world

import android.app.Application
import android.util.Log
import com.colora.world.audio.AudioEngine
import com.colora.world.content.ContentRepo
import com.colora.world.data.Entitlements
import com.colora.world.data.Library
import com.colora.world.data.Settings
import com.colora.world.render.ToyCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Process-wide object graph. Everything is local; nothing here touches the network. */
class ColoraApp : Application() {
    lateinit var graph: AppGraph; private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
        graph.appScope.launch(Dispatchers.IO) {
            val report = graph.library.recover()
            Log.i("Colora", "startup recovery: $report")
        }
    }
}

class AppGraph(val app: Application) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val content = ContentRepo(app)
    val settings = Settings(app)
    val library = Library(app.filesDir, content)
    val entitlements = Entitlements.create(app, settings)
    val audio = AudioEngine(app, settings)
    val toyCache = ToyCache(content)
}

val android.content.Context.graph: AppGraph get() = (applicationContext as ColoraApp).graph
