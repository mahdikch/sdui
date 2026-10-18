package com.yandex.divkit.demo.debug

import android.content.Context
import com.facebook.stetho.Stetho

/** Debug-only Chrome DevTools bridge. Not packaged in release. */
object DebugStetho {
    fun init(context: Context) {
        val initializer = Stetho.newInitializerBuilder(context)
            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context))
            .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
            .build()
        Stetho.initialize(initializer)
    }
}
