package com.github.zjor.ext.guice

import com.google.inject.AbstractModule
import com.google.inject.matcher.Matchers
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.LoggerFactory

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Log


class LoggingMethodInterceptor : MethodInterceptor {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun invoke(invocation: MethodInvocation): Any? {

        val args = invocation.method.parameters
            .zip(invocation.arguments)
            .joinToString(", ") { (param, arg) ->
                "${param.name}=${arg}"
            }

        try {
            val result = invocation.proceed()
            if (invocation.method.returnType == Void.TYPE.kotlin.java) {
                logger.info("${invocation.method.name}($args): void")
            } else {
                logger.info("${invocation.method.name}($args): $result")
            }
            return result
        } catch (t: Throwable) {
            logger.info("${invocation.method.name}($args): thrown $t")
            throw t
        }
    }
}


class LoggingModule : AbstractModule() {
    override fun configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Log::class.java), LoggingMethodInterceptor())
    }
}
