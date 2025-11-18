package com.huanli233.bilizepam.utils

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

object ObjectBuilder {
    
    private val ABSENT_VALUE = Any()
    
    inline fun <reified T : Any> build(vararg params: Pair<String, Any?>): T {
        return build(T::class, *params)
    }
    
    fun <T : Any> build(kClass: KClass<T>, vararg params: Pair<String, Any?>): T {
        return buildInternal(kClass, params.toMap())
    }
    
    private fun getTypeDefaultValue(type: KType): Any? {
        return when (type.classifier) {
            String::class -> ""
            Boolean::class -> false
            Int::class -> 0
            Long::class -> 0L
            Float::class -> 0.0f
            Double::class -> 0.0
            Short::class -> 0.toShort()
            Byte::class -> 0.toByte()
            Char::class -> '\u0000'
            List::class -> emptyList<Any>()
            Set::class -> emptySet<Any>()
            Map::class -> emptyMap<Any, Any>()
            else -> {
                val kClass = type.classifier as? KClass<*> ?: return null
                if (kClass.java.isEnum) {
                    kClass.java.enumConstants?.firstOrNull()
                } else {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        buildInternal(kClass as KClass<Any>)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }
    
    private fun <T : Any> buildInternal(kClass: KClass<T>, paramMap: Map<String, Any?> = emptyMap()): T {
        val constructor = kClass.primaryConstructor
            ?: throw IllegalArgumentException("Class ${kClass.simpleName} has no primary constructor")
        
        constructor.isAccessible = true
        
        val constructorSize = constructor.parameters.size
        val values = Array<Any?>(constructorSize) { ABSENT_VALUE }
        var initializedAllParameters = true
        
        for (i in 0 until constructorSize) {
            val parameter = constructor.parameters[i]
            val paramName = parameter.name ?: continue
            
            if (paramMap.containsKey(paramName)) {
                values[i] = paramMap[paramName]
            } else if (parameter.isOptional) {
                initializedAllParameters = false
            } else if (parameter.type.isMarkedNullable) {
                values[i] = null
            } else {
                values[i] = getTypeDefaultValue(parameter.type)
            }
        }
        
        return if (initializedAllParameters) {
            constructor.call(*values)
        } else {
            constructor.callBy(IndexedParameterMap(constructor.parameters, values))
        }
    }
    
    private class IndexedParameterMap(
        private val parameterKeys: List<KParameter>,
        private val parameterValues: Array<Any?>,
    ) : AbstractMutableMap<KParameter, Any?>() {
        
        override fun put(key: KParameter, value: Any?): Any? = null
        
        override val entries: MutableSet<MutableMap.MutableEntry<KParameter, Any?>>
            get() {
                val allPossibleEntries = parameterKeys.mapIndexed { index, value ->
                    SimpleEntry<KParameter, Any?>(value, parameterValues[index])
                }
                return allPossibleEntries.filterTo(mutableSetOf()) {
                    it.value !== ABSENT_VALUE
                }
            }
        
        override fun containsKey(key: KParameter) = parameterValues[key.index] !== ABSENT_VALUE
        
        override fun get(key: KParameter): Any? {
            val value = parameterValues[key.index]
            return if (value !== ABSENT_VALUE) value else null
        }
    }
}
