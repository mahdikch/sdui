package com.yandex.divkit.demo.div

/**
 * Singleton instance of DynamicPatchGenerator to maintain state across action handler instances
 */
object DynamicPatchGeneratorSingleton {
    val instance = DynamicPatchGenerator()
}

