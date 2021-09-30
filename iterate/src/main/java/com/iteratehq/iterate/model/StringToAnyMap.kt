package com.iteratehq.iterate.model

import androidx.annotation.Keep

/**
 * A convenience class to create a Map of <String, Any> that can be aliased to other data type.
 * An example of instantiating this class with some key-value pairs:
 *
 * StringToAnyMap(
 *   "id" to 12345
 *   "message" to "Hello!"
 * )
 */
@Keep
class StringToAnyMap() : LinkedHashMap<String, Any>() {
    constructor(vararg pairs: Pair<String, Any>) : this() {
        pairs.toMap(this)
    }
}
