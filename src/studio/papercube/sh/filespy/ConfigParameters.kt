package studio.papercube.sh.filespy

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream
import kotlin.reflect.KProperty

class ConfigParameters @JvmOverloads constructor(val property: PropertyMap = PropertyMap()) {
    companion object {
        val instance by lazy {
            ConfigParameters.resolve(File(CONFIG_PATH))
        }

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

    @Volatile private var lastSave: Long = System.currentTimeMillis()
    private val SAVE_INTERVAL_THRESHOLD = 3000L

    operator fun get(key: String) = property[key]

    operator fun set(key: String, value: String) = { property[key] = value }

    fun computeIfAbsent(key: String, lazyValue: (String) -> String): String {
        return property.computeIfAbsent(key, lazyValue)
    }

    fun save() {
        lastSave = System.currentTimeMillis()
        val config = File(CONFIG_PATH)
        config.parentFile.mkdirs()
        FileWriter(config).buffered().use {
            property.forEach { key, value -> it.write("$key=$value") }
        }
    }

    fun saveAsync() {
        CompletableFuture.supplyAsync { save() }
    }

    fun saveAsyncIfNecessary() {
        if (System.currentTimeMillis() - lastSave < SAVE_INTERVAL_THRESHOLD) saveAsync()
    }

    var regex: String by propertyWithInitial("regex", DEFAULT_REGEX)

    var dataPath by propertyWithInitial("dataPath", DATA_PATH)


    fun propertyWithInitial(key: String, initial: String) = ParameterControl(key, initial, property)

    @Suppress("NOTHING_TO_INLINE")
    class ParameterControl<R>(val key: String, val initial: R, val map: MutableMap<String, R>) {
        inline operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
            return map.computeIfAbsent(key) { initial }
        }

        inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: R) {
            map[key] = value
            (thisRef as? ConfigParameters)?.saveAsyncIfNecessary()
        }
    }

}