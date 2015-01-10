package com.auginte.distribution.orientdb

/**
 * Element having link to node
 */
trait NodeWrapper {
  def node(implicit cache: Node.Cached = Node.defaultCache): Node

  def node_=(link: Node): Unit
}
