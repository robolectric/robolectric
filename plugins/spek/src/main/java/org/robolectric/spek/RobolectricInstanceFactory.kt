package org.robolectric.spek

import org.robolectric.android.RobolectricManager
import org.robolectric.android.SandboxConfigurer
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.DefaultManifestFactory
import org.robolectric.internal.bytecode.*
import org.robolectric.manifest.AndroidManifest
import org.robolectric.pluginapi.Sdk
import org.robolectric.pluginapi.SdkProvider
import org.robolectric.pluginapi.config.Configuration
import org.robolectric.pluginapi.config.GlobalConfigProvider
import org.robolectric.plugins.ConfigurationImpl
import org.robolectric.util.inject.Injector
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.InstanceFactory
import java.util.*
import kotlin.reflect.KClass

class RobolectricInstanceFactory : InstanceFactory {
    val robolectricManager : RobolectricManager
    val defaultSdk : Sdk

    init {
        val injector = Injector.Builder()
                .bindDefault(pickInstrumentor())
                .bind<Properties>(System.getProperties())
                .bind(GlobalConfigProvider { Config.Builder().build() })
                .build()
        robolectricManager = injector.getInstance(RobolectricManager::class.java)
        val sdkProvider = injector.get<SdkProvider>()
        defaultSdk = sdkProvider.sdks.sorted().last { it.isSupported }
    }

    override fun <T : Spek> create(spek: KClass<T>): T {
        val manifestFactory = DefaultManifestFactory(DefaultManifestFactory.loadBuildSystemApiProperties())
        val config = Config.Builder.defaults().build()
        val manifestIdentifier = manifestFactory.identify(config)
        val androidManifest = robolectricManager.cachedCreateAppManifest(manifestIdentifier)
        val configuration = ConfigurationImpl()
                .put(defaultSdk)
                .put<SandboxConfigurer>(SpekConfigurer)
                .put(LooperMode.Mode.PAUSED)
                .put<Config>(config)
                .put<AndroidManifest>(androidManifest)

        val sandbox = robolectricManager.getSandbox(configuration)

        val bootstrappedSpekClass = sandbox.bootstrappedClass<T>(spek.java).kotlin
        val instance = bootstrappedSpekClass.objectInstance
                ?: bootstrappedSpekClass.constructors.first { it.parameters.isEmpty() }.call()

        runningSpec = SpecState(configuration, sandbox)
        return instance as T
//        val any = spek.objectInstance ?: spek.constructors.first { it.parameters.isEmpty() }.call()
    }

    companion object {
        private fun pickInstrumentor(): Class<out ClassInstrumentor> {
            return if (InvokeDynamic.ENABLED)
                InvokeDynamicClassInstrumentor::class.java
            else
                OldClassInstrumentor::class.java
        }
    }

    inline fun <reified T: Class<*>> Sandbox.bootstrappedClass(): Class<*> {
        return this.bootstrappedClass<T>(T::class.java)
    }

    inline fun <reified T> Injector.get(): T = this.getInstance(T::class.java)
    inline fun <reified T> Injector.Builder.bind(instance: T): Injector.Builder =
            this.bind(T::class.java, instance)

    inline fun <reified T> Injector.Builder.bindDefault(clazz: Class<out T>): Injector.Builder =
            this.bindDefault(T::class.java, clazz)

    inline fun <reified T> ConfigurationImpl.put(t: T): ConfigurationImpl = this.put(T::class.java, t)

    inner class SpecState(val configuration: Configuration, val sandbox: AndroidSandbox) {
        fun beforeTest() {
            robolectricManager.configure(sandbox, configuration)
            sandbox.testEnvironment.setUpApplicationState(configuration, "dunno")
        }

        fun afterTest() {
            try {
                sandbox.testEnvironment.tearDownApplication()
                sandbox.testEnvironment.checkStateAfterTestFailure(null)
            } finally {
                sandbox.testEnvironment.resetState()
            }
        }
    }

    object SpekConfigurer : SandboxConfigurer {
        override fun configure(builder: InstrumentationConfiguration.Builder) {
            builder.doNotAcquirePackage("org.spekframework.")
                    .doNotAcquirePackage("org.robolectric.spek.")
        }

        override fun configure(builder: ShadowMap.Builder?) {
        }
    }
}