package com.auginte.transforamtion

/**
 * Directional reference with parameters.
 * 
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class Relation(target: Descendant, parameters: Map[String, String])
