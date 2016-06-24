package com.auginte.server.helpers

import com.google.inject.TypeLiteral
import com.google.inject.matcher.{AbstractMatcher, Matcher}
import com.google.inject.spi.{InjectionListener, TypeEncounter, TypeListener}

import scala.language.existentials

/**
  * When class need to be exectued after Guice dependency injection is finished
  */
trait GuiceOnStart {
  protected def onStart[A](classDefinition: Class[A])(clousure: A => Unit): (Matcher[_ >: TypeLiteral[_]], TypeListener) = {
    val matcher = new AbstractMatcher[TypeLiteral[_]] {
      override def matches(typeLiteral: TypeLiteral[_]): Boolean = typeLiteral.getRawType match {
        case some if some == classDefinition =>
          true
        case other =>
          false
      }
    }
    val listener: TypeListener = new TypeListener {
      override def hear[I](typeLiteral: TypeLiteral[I], typeEncounter: TypeEncounter[I]): Unit = {
        typeEncounter.register(new InjectionListener[I] {
          override def afterInjection(i: I): Unit = clousure(i.asInstanceOf[A])
        })
      }
    }
    (matcher, listener)
  }
}
