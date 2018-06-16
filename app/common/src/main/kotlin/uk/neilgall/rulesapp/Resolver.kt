package uk.neilgall.rulesapp

fun RuleSet<String>.resolve(): RuleSet<Attribute> {

    val originalAttributesByName = attributes.associate({ it.name to it })

    val attributes = mutableMapOf<String, Attribute>()

    fun resolveAttribute(name: String): Attribute =
            attributes.getOrPut(name, {
                originalAttributesByName[name]?.map(::resolveAttribute) ?: throw NoSuchElementException(name)
            })

    return map(::resolveAttribute)
}
