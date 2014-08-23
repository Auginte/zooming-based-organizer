package com.auginte.distribution.data

import com.auginte.zooming.Node

/**
 * Place holder for [[com.auginte.zooming.Node]],
 * so relations could be updated after all elements are loaded.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class ImportedNode(override val x: Int, override val y: Int, override val storageId: String, val parentId: String) extends Node(x, y) {
  override def toString(): String = s"""ImportedNode($x,$y,"$storageId","$parentId")"""
}