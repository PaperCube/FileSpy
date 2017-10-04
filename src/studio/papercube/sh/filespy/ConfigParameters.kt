//TODO: FUCK THIS FUNCTIONALITY

package studio.papercube.sh.filespy

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream
import kotlin.reflect.KProperty

abstract class AbstractConfigParameters(val property: PropertyMap) {
    companion object {
        fun resolve(file: File): ConfigParameters {
            try {
                return resolve(FileReader(file).buffered(bufferSize = 8192).lines())
            } catch (e: IOException) {
                return ConfigParameters()
            }
        }

        fun resolve(stream: Stream<String>): ConfigParameters {
            val para = PropertyMap()
            stream.forEach { it.toLowerCase().split(delimiters = "=", limit = 2).let { split -> para.put(split[0], split[1]) } }
            return ConfigParameters(para)
        }

    }

    init {
        saveAsync()
    }

    @Volatile 
    private var lastSave: Long = System.currentTimeMillis()
    var saveIntervalThreshold = 3000L
    private val environment:Environment = Environment.environment

    operator open fun get(key: String) = property[key]

    operator open fun set(key: String, value: String) = { property[key] = value }

    fun computeIfAbsent(key: String, lazyValue: (String) -> String): String {
        return property.computeIfAbsent(key, lazyValue)
    }

    fun save() {
        lastSave = System.currentTimeMillis()
        val config = environment.getConfigFile()
        config.parentFile.mkdirs()
        FileWriter(config).buffered().use {
            property.forEach { key, value -> it.write("$key=$value") }
        }
    }

    fun saveAsync() {
        CompletableFuture.supplyAsync { save() }
    }

    open fun saveAsyncIfNecessary() {
        if (System.currentTimeMillis() - lastSave < saveIntervalThreshold) saveAsync()
    }


    fun propertyWithInitial(key: String, initial: String) = ParameterControl(key, initial, property)

    @Suppress("NOTHING_TO_INLINE")
    open class ParameterControl<R>(val key: String, val initial: R, val map: MutableMap<String, R>) {
        inline operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
            return map.computeIfAbsent(key) { initial }
        }

        inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: R) {
            map[key] = value
            (thisRef as? AbstractConfigParameters)?.saveAsyncIfNecessary()
        }
    }
}

open class ConfigParameters @JvmOverloads constructor(property: PropertyMap = PropertyMap()) : AbstractConfigParameters(property) {
    companion object {
        val instance by lazy {
            AbstractConfigParameters.resolve(Environment.environment.getConfigFile())
        }
    }

    @Deprecated("Use PatternManager instead.")
    var regex: String by propertyWithInitial("regex", DEFAULT_REGEX)

    var dataPath by propertyWithInitial("dataPath", Environment.environment.getDataStorage().absolutePath)
}